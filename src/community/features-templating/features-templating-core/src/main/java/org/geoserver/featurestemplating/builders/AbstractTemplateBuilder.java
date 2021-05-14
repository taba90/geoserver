/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.builders;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geoserver.featurestemplating.builders.impl.TemplateBuilderContext;
import org.geoserver.featurestemplating.expressions.TemplateCQLManager;
import org.geoserver.featurestemplating.writers.TemplateOutputWriter;
import org.geotools.filter.LiteralExpressionImpl;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * Abstract implementation of {@link TemplateBuilder} who groups some common attributes and methods.
 */
public abstract class AbstractTemplateBuilder implements TemplateBuilder {

    protected Expression key;

    protected Filter filter;

    protected int filterContextPos = 0;

    protected NamespaceSupport namespaces;

    protected List<TemplateBuilder> children;

    protected Map<String, Object> encodingHints;

    public AbstractTemplateBuilder(String key, NamespaceSupport namespaces) {
        this.key = getKeyAsExpression(key);
        this.namespaces = namespaces;
    }

    /**
     * Evaluate the filter against the provided template context
     *
     * @param context the current context
     * @return return the result of filter evaluation
     */
    protected boolean evaluateFilter(TemplateBuilderContext context) {
        if (filter == null) return true;
        TemplateBuilderContext evaluationContenxt = context;
        for (int i = 0; i < filterContextPos; i++) {
            evaluationContenxt = evaluationContenxt.getParent();
        }
        return filter.evaluate(evaluationContenxt.getCurrentObj());
    }

    public String getKey() {
        return key != null ? key.evaluate(null).toString() : null;
    }

    public void setKey(String key) {
        this.key = getKeyAsExpression(key);
    }

    /**
     * Get the filter if present
     *
     * @return
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Set the filter to the builder
     *
     * @param filter the filter to be setted
     * @throws CQLException
     */
    public void setFilter(String filter) {
        TemplateCQLManager cqlManager = new TemplateCQLManager(filter, namespaces);
        try {
            this.filter = cqlManager.getFilterFromString();
        } catch (CQLException e) {
            throw new RuntimeException(e);
        }
        this.filterContextPos = cqlManager.getContextPos();
    }

    /**
     * Get context position of the filter if present
     *
     * @return
     */
    public int getFilterContextPos() {
        return filterContextPos;
    }

    /**
     * Write the attribute key if present
     *
     * @param writer the template writer
     * @throws IOException
     */
    protected void writeKey(TemplateOutputWriter writer) throws IOException {
        if (key != null && !key.evaluate(null).equals(""))
            // key might be and EnvFunction or a Literal. In both cases
            // no argument is needed for the evaluation thus passing null.
            writer.writeElementName(key.evaluate(null), getEncodingHints());
    }

    public NamespaceSupport getNamespaces() {
        return namespaces;
    }

    private Expression getKeyAsExpression(String key) {
        Expression keyExpr;
        if (key != null) {
            if (key.startsWith("$${")) {
                TemplateCQLManager cqlManager = new TemplateCQLManager(key, null);
                keyExpr = cqlManager.getExpressionFromString();
            } else {
                keyExpr = new LiteralExpressionImpl(key);
            }
        } else {
            keyExpr = null;
        }
        return keyExpr;
    }

    @Override
    public List<TemplateBuilder> getChildren() {
        return children;
    }

    @Override
    public Map<String, Object> getEncodingHints() {
        if (this.encodingHints == null) this.encodingHints = new HashMap<>();
        return encodingHints;
    }

    @Override
    public void addEncodingHint(String key, Object value) {
        if (this.encodingHints == null) this.encodingHints = new HashMap<>();
        this.encodingHints.put(key, value);
    }
}
