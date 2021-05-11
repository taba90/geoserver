package org.geoserver.featurestemplating.builders;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import org.geoserver.featurestemplating.builders.flat.FlatCompositeBuilder;
import org.geoserver.featurestemplating.builders.flat.FlatDynamicBuilder;
import org.geoserver.featurestemplating.builders.flat.FlatIteratingBuilder;
import org.geoserver.featurestemplating.builders.flat.FlatStaticBuilder;
import org.geoserver.featurestemplating.builders.impl.CompositeBuilder;
import org.geoserver.featurestemplating.builders.impl.DynamicValueBuilder;
import org.geoserver.featurestemplating.builders.impl.IteratingBuilder;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.geoserver.featurestemplating.builders.impl.StaticBuilder;
import org.geoserver.featurestemplating.readers.TemplateReader;
import org.xml.sax.helpers.NamespaceSupport;

public class TemplateBuilderMaker {

    private boolean rootBuilder;

    private boolean semanticValidation;

    private String rootCollectionName;

    private JsonNode jsonNode;

    private String textContent;

    private String name;

    private String filter;

    private String source;

    private boolean isCollection;

    private boolean flatOutput;

    private boolean rootCollection;

    private Map<String, Object> encondingHints;

    private Map<String, String> vendorOptions;

    private NamespaceSupport namespaces;

    private String separator = "_";

    public TemplateBuilderMaker() {
        this.encondingHints = new HashMap<>();
        this.vendorOptions = new HashMap<>();
    }

    public TemplateBuilderMaker(String rootCollectionName) {
        this();
        this.rootCollectionName = rootCollectionName;
    }

    public TemplateBuilderMaker textContent(String textContent) {
        this.textContent = textContent;
        return this;
    }

    public TemplateBuilderMaker jsonNode(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
        return this;
    }

    public TemplateBuilderMaker content(Object content) {
        if (content instanceof String) textContent(content.toString());
        else if (content instanceof JsonNode) jsonNode((JsonNode) content);
        else
            throw new UnsupportedOperationException(
                    "Unsupported content for builders. Content is of type " + content.getClass());
        return this;
    }

    public TemplateBuilderMaker name(String name) {
        this.name = name;
        return this;
    }

    public TemplateBuilderMaker filter(String filter) {
        this.filter = filter;
        return this;
    }

    public TemplateBuilderMaker source(String source) {
        this.source = source;
        return this;
    }

    public TemplateBuilderMaker collection(boolean collection) {
        isCollection = collection;
        return this;
    }

    public TemplateBuilderMaker flatOutput(boolean flatOutput) {
        this.flatOutput = flatOutput;
        return this;
    }

    public TemplateBuilderMaker rootCollection(boolean root) {
        rootCollection = root;
        return this;
    }

    public TemplateBuilderMaker encodingOption(String name, Object value) {
        this.encondingHints.put(name, value);
        return this;
    }

    public TemplateBuilderMaker namespaces(NamespaceSupport namespaceSupport) {
        this.namespaces = namespaceSupport;
        return this;
    }

    public TemplateBuilderMaker separator(String separator) {
        this.separator = separator;
        return this;
    }

    public TemplateBuilderMaker rootBuilder(boolean rootBuilder) {
        this.rootBuilder = rootBuilder;
        return this;
    }

    public TemplateBuilderMaker semanticValidation(boolean semanticValidation) {
        this.semanticValidation = semanticValidation;
        return this;
    }

    public void globalReset() {
        localReset();
        this.namespaces = null;
        this.separator = null;
        this.flatOutput = false;
    }

    public void localReset() {
        this.encondingHints = new HashMap<>();
        this.vendorOptions = new HashMap<>();
        this.semanticValidation = false;
        this.filter = null;
        this.isCollection = false;
        this.rootCollection = false;
        this.name = null;
        this.source = null;
        this.textContent = null;
        this.jsonNode = null;
        this.rootBuilder = false;
    }

    public RootBuilder buildRootBuilder() {
        RootBuilder rootBuilder = new RootBuilder();
        if (!encondingHints.isEmpty()) rootBuilder.getEncodingHints().putAll(encondingHints);
        if (!vendorOptions.isEmpty()) {
            rootBuilder.addVendorOptions(vendorOptions);
        }
        rootBuilder.setSemanticValidation(semanticValidation);
        localReset();
        return rootBuilder;
    }

    private IteratingBuilder buildIteratingBuilder() {
        IteratingBuilder iteratingBuilder;
        if (flatOutput) iteratingBuilder = new FlatIteratingBuilder(name, namespaces, separator);
        else iteratingBuilder = new IteratingBuilder(name, namespaces);
        if (source != null) iteratingBuilder.setSource(source);
        if (filter != null) iteratingBuilder.setFilter(filter);
        if (!encondingHints.isEmpty()) iteratingBuilder.getEncodingHints().putAll(encondingHints);
        if (name != null && rootCollectionName != null && rootCollectionName.equals(name))
            rootCollection = true;
        iteratingBuilder.setRootCollection(rootCollection);
        return iteratingBuilder;
    }

    private CompositeBuilder buildCompositeBuilder() {
        CompositeBuilder compositeBuilder;
        if (flatOutput) compositeBuilder = new FlatCompositeBuilder(name, namespaces, separator);
        else compositeBuilder = new CompositeBuilder(name, namespaces);

        if (source != null) compositeBuilder.setSource(source);
        if (filter != null) compositeBuilder.setFilter(filter);
        if (!encondingHints.isEmpty()) compositeBuilder.getEncodingHints().putAll(encondingHints);

        return compositeBuilder;
    }

    private DynamicValueBuilder buildDynamicBuilder() {
        DynamicValueBuilder dynamicValueBuilder;
        if (flatOutput)
            dynamicValueBuilder = new FlatDynamicBuilder(name, textContent, namespaces, separator);
        else dynamicValueBuilder = new DynamicValueBuilder(name, textContent, namespaces);
        if (filter != null) dynamicValueBuilder.setFilter(filter);
        if (!encondingHints.isEmpty())
            dynamicValueBuilder.getEncodingHints().putAll(encondingHints);
        return dynamicValueBuilder;
    }

    private StaticBuilder buildStaticBuilder() {
        StaticBuilder staticBuilder;
        boolean hasJsonNode = jsonNode != null;
        boolean hasFilter = filter != null;
        if (flatOutput) {
            if (hasJsonNode && !hasFilter)
                staticBuilder = new FlatStaticBuilder(name, jsonNode, namespaces, separator);
            else staticBuilder = new FlatStaticBuilder(name, textContent, namespaces, separator);
        } else {

            if (hasJsonNode && !hasFilter)
                staticBuilder = new StaticBuilder(name, jsonNode, namespaces);
            else staticBuilder = new StaticBuilder(name, textContent, namespaces);
        }

        if (filter != null) staticBuilder.setFilter(filter);
        if (!encondingHints.isEmpty()) staticBuilder.getEncodingHints().putAll(encondingHints);

        return staticBuilder;
    }

    public TemplateBuilder build() {
        TemplateBuilder result;
        if (rootBuilder) result = buildRootBuilder();
        else if (textContent == null && jsonNode == null) {
            if (isCollection) result = buildIteratingBuilder();
            else result = buildCompositeBuilder();
        } else {
            if (textContent != null && textContent.contains(TemplateReader.EXPRSTART))
                result = buildDynamicBuilder();
            else result = buildStaticBuilder();
        }
        localReset();
        return result;
    }
}
