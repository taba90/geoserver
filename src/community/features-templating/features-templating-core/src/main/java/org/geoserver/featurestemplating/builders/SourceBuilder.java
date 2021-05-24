/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.builders;

import java.util.LinkedList;
import java.util.Optional;
import org.geoserver.featurestemplating.builders.impl.TemplateBuilderContext;
import org.geoserver.featurestemplating.expressions.TemplateCQLManager;
import org.geotools.filter.AttributeExpressionImpl;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.xml.sax.helpers.NamespaceSupport;

/** Abstract class for builders that can set the context for their children through source xpath */
public abstract class SourceBuilder extends AbstractTemplateBuilder {

    private Expression source;

    public SourceBuilder(String key, NamespaceSupport namespaces) {
        super(key, namespaces);
        this.children = new LinkedList<>();
    }

    /**
     * Evaluates the source against the provided template context
     *
     * @param context the current context to be used for evaluation
     * @return the new context produced by source evaluation
     */
    public TemplateBuilderContext evaluateSource(TemplateBuilderContext context) {

        if (source != null && !getStrSource().equals(context.getCurrentSource())) {
            Object o = evaluateSource(context.getCurrentObj());
            TemplateBuilderContext newContext = new TemplateBuilderContext(o, getStrSource());
            newContext.setParent(context);
            return newContext;
        }
        return context;
    }

    /**
     * Evaluate the source against the Object passed
     *
     * @param o the value against which evaluating the source
     * @return the result of the evaluation
     */
    public Object evaluateSource(Object o) {
        if (!(source instanceof AttributeExpressionImpl)) {
            AttributeExpressionImpl sourceToEval =
                    new AttributeExpressionImpl(source.evaluate(null).toString(), namespaces);
            return sourceToEval.evaluate(o);
        }
        return source.evaluate(o);
    }

    /**
     * Get the source as an Expression
     *
     * @return the source as an Expression
     */
    public Expression getSource() {
        if (!(source instanceof AttributeExpressionImpl) && source != null) {
            return new AttributeExpressionImpl(source.evaluate(null).toString(), namespaces);
        }
        return source;
    }

    /**
     * Get the source as string
     *
     * @return the source a string
     */
    public String getStrSource() {
        if (source == null) return null;

        if (source instanceof AttributeExpressionImpl)
            return ((AttributeExpressionImpl) source).getPropertyName();
        else return Optional.ofNullable(source.evaluate(null)).map(o -> o.toString()).orElse(null);
    }

    /**
     * Set the source to the builder
     *
     * @param source the string source
     */
    public void setSource(String source) {
        TemplateCQLManager cqlManager = new TemplateCQLManager(source, namespaces);
        Expression sourceExpr = cqlManager.getExpressionFromString();
        if (sourceExpr instanceof Literal)
            this.source =
                    new AttributeExpressionImpl(sourceExpr.evaluate(null).toString(), namespaces);
        else this.source = sourceExpr;
    }
}
