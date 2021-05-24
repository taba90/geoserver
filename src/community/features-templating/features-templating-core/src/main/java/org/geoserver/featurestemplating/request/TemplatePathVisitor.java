/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.request;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.geoserver.featurestemplating.builders.AbstractTemplateBuilder;
import org.geoserver.featurestemplating.builders.SourceBuilder;
import org.geoserver.featurestemplating.builders.TemplateBuilder;
import org.geoserver.featurestemplating.builders.impl.CompositeBuilder;
import org.geoserver.featurestemplating.builders.impl.DynamicValueBuilder;
import org.geoserver.featurestemplating.builders.impl.IteratingBuilder;
import org.geoserver.featurestemplating.builders.impl.StaticBuilder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;

/**
 * This visitor search for a Filter in {@link TemplateBuilder} tree using the path provided as a
 * guideline.
 */
public class TemplatePathVisitor extends DuplicatingFilterVisitor {

    protected int currentEl;
    protected String currentSource;
    static final FilterFactory2 FF = CommonFactoryFinder.getFilterFactory2();
    boolean isSimple;
    private List<Filter> filters = new ArrayList<>();

    public TemplatePathVisitor(FeatureType type) {
        this.isSimple = type instanceof SimpleFeatureType;
    }

    @Override
    public Object visit(PropertyName expression, Object extraData) {
        String propertyName = expression.getPropertyName();
        if (extraData instanceof TemplateBuilder) {
            TemplateBuilder builder = (TemplateBuilder) extraData;
            Object newExpression = mapPropertyThroughBuilder(propertyName, builder);
            if (newExpression != null) return newExpression;
        }
        return getFactory(extraData)
                .property(expression.getPropertyName(), expression.getNamespaceContext());
    }

    /**
     * Back maps a given property through the template, to find out if it originates via an
     * expression.
     *
     * @param propertyName
     * @param builder
     * @return
     */
    protected Expression mapPropertyThroughBuilder(String propertyName, TemplateBuilder builder) {
        String[] elements;
        if (propertyName.indexOf(".") != -1) {
            elements = propertyName.split("\\.");
        } else {
            elements = propertyName.split("/");
        }

        try {
            currentSource = null;
            currentEl = 0;
            Expression newExpression = findFunction(builder, Arrays.asList(elements));
            newExpression = (Expression) findXpathArg(newExpression);
            if (newExpression != null) {
                return newExpression;
            }
        } catch (Throwable ex) {
            throw new RuntimeException(
                    "Unable to evaluate template path against"
                            + "the template. Cause: "
                            + ex.getMessage());
        }
        return null;
    }

    private Object findXpathArg(Object newExpression) {
        DuplicatingFilterVisitor duplicatingFilterVisitor =
                new DuplicatingFilterVisitor() {
                    @Override
                    public Object visit(PropertyName filter, Object extraData) {
                        filter = (PropertyName) super.visit(filter, extraData);
                        if (filter instanceof AttributeExpressionImpl) {
                            AttributeExpressionImpl pn = (AttributeExpressionImpl) filter;
                            pn.setPropertyName(completeXPath(pn.getPropertyName()));
                            filter = pn;
                        }
                        return filter;
                    }
                };
        if (newExpression instanceof Expression) {
            return ((Expression) newExpression).accept(duplicatingFilterVisitor, null);
        } else if (newExpression instanceof Filter) {
            return ((Filter) newExpression).accept(duplicatingFilterVisitor, null);
        }
        return null;
    }

    /**
     * Find the corresponding function to which the template path is pointing, by iterating over
     * builder's tree
     */
    public Expression findFunction(TemplateBuilder builder, List<String> pathElements) {
        int lastElI = pathElements.size() - 1;
        String lastEl = pathElements.get(lastElI);
        char[] charArr = lastEl.toCharArray();
        int index = extractArrayIndexIfPresent(charArr);
        // we might have a path like path.to.an.array1 pointing
        // to a template array attribute eg "array":["${xpath}","$${xpath}", "static value"]
        if (index != 0) {
            lastEl = String.valueOf(charArr);
            pathElements.set(lastElI, lastEl.substring(0, charArr.length - 1));
        }
        // find the builder to which the path is pointing
        TemplateBuilder jb = findBuilder(builder, pathElements);
        if (jb != null) {
            if (jb instanceof IteratingBuilder && index != 0) {
                // retrieve the builder based on the position
                IteratingBuilder itb = (IteratingBuilder) jb;
                jb = getChildFromIterating(itb, index - 1);
            }

            if (jb instanceof DynamicValueBuilder) {
                DynamicValueBuilder dvb = (DynamicValueBuilder) jb;
                addFilter(dvb.getFilter());
                if (dvb.getXpath() != null) return (PropertyName) super.visit(dvb.getXpath(), null);
                else {
                    return super.visit(dvb.getCql(), null);
                }
            } else if (jb instanceof StaticBuilder) {
                StaticBuilder staticBuilder = (StaticBuilder) jb;
                addFilter(staticBuilder.getFilter());
                Expression retExpr;
                if (staticBuilder.getStaticValue() != null) {
                    JsonNode staticNode = staticBuilder.getStaticValue();
                    while (currentEl < pathElements.size()) {
                        JsonNode child = staticNode.get(pathElements.get(currentEl - 1));
                        staticNode = child != null ? child : staticNode;
                        currentEl++;
                    }
                    retExpr = ff.literal(staticNode.asText());
                } else {
                    retExpr = ff.literal(staticBuilder.getStrValue());
                }
                return retExpr;
            }
        }
        return null;
    }

    private int extractArrayIndexIfPresent(char[] charArr) {
        int lastIdx = charArr.length - 1;
        char lastElem = charArr[lastIdx];
        if (Character.isDigit(lastElem)) {
            return Character.getNumericValue(lastElem);
        }
        return 0;
    }

    /**
     * Find the corresponding function to which the template path is pointing, by iterating over
     * builder's tree
     */
    private TemplateBuilder findBuilder(TemplateBuilder parent, List<String> pathElements) {
        List<TemplateBuilder> children = parent.getChildren();
        int length = pathElements.size();
        if (children != null) {
            for (TemplateBuilder jb : children) {
                String key = ((AbstractTemplateBuilder) jb).getKey();
                if (keyMatched(jb, key, pathElements)) {
                    boolean isLastEl = currentEl == length;
                    if (isLastEl || jb instanceof StaticBuilder) {
                        return jb;
                    } else if (jb instanceof SourceBuilder) {
                        pickSourceAndFilter((SourceBuilder) jb);
                        TemplateBuilder result = findBuilder(jb, pathElements);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    // In case a path specifying an element in an array has been specified
    // eg. path.to.array.1
    private TemplateBuilder getChildFromIterating(IteratingBuilder itb, int position) {
        List<TemplateBuilder> children = itb.getChildren();
        if (position < children.size()) return children.get(position);
        return null;
    }

    private boolean keyMatched(TemplateBuilder jb, String key, List<String> pathElements) {
        // CompositeBuilder (if children of an Iterating builder) have null key
        boolean allowNullKey = jb instanceof CompositeBuilder && key == null;
        // the key matched one element of the path
        boolean keyMatchedOtherBuilder = (key != null && key.equals(pathElements.get(currentEl)));

        if (keyMatchedOtherBuilder) currentEl++;

        return allowNullKey || keyMatchedOtherBuilder;
    }

    // checks whether the parent builder key and the lastMatchedKey are equals
    // to avoid to continue iterating over a builder branch unnecessarily
    private boolean parentKeyEqualsLastMatchedKey(TemplateBuilder parent, String lastMatchedKey) {
        String parentKey = null;
        if (parent instanceof AbstractTemplateBuilder)
            parentKey = ((AbstractTemplateBuilder) parent).getKey();

        if (lastMatchedKey != null && parentKey != null && !parentKey.equals(lastMatchedKey))
            return false;
        return true;
    }

    // takes source and filter from the SourceBuilder
    private void pickSourceAndFilter(SourceBuilder sb) {
        String source = sb.getStrSource();
        if (source != null) {
            if (currentSource != null) {
                source = "/" + source;
                currentSource += source;
            } else {
                currentSource = source;
            }
        }
        addFilter(sb.getFilter());
    }

    /**
     * Add to the xpath, xpath parts taken from the $source attribute. This is done for Complex
     * Features only
     */
    private String completeXPath(String xpath) {
        if (currentSource != null && !isSimple) xpath = currentSource + "/" + xpath;
        return xpath;
    }

    private void addFilter(Filter filter) {
        if (filter != null) {
            filter = (Filter) findXpathArg(filter);
            filters.add(filter);
        }
    }

    public List<Filter> getFilters() {
        return filters;
    }
}
