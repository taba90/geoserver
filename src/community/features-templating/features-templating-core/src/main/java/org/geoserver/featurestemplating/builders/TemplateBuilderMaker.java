package org.geoserver.featurestemplating.builders;

import java.util.HashMap;
import java.util.Map;
import org.geoserver.featurestemplating.builders.flat.FlatCompositeBuilder;
import org.geoserver.featurestemplating.builders.flat.FlatDynamicBuilder;
import org.geoserver.featurestemplating.builders.flat.FlatIteratingBuilder;
import org.geoserver.featurestemplating.builders.flat.FlatStaticBuilder;
import org.geoserver.featurestemplating.builders.impl.CompositeBuilder;
import org.geoserver.featurestemplating.builders.impl.DynamicValueBuilder;
import org.geoserver.featurestemplating.builders.impl.IteratingBuilder;
import org.geoserver.featurestemplating.builders.impl.StaticBuilder;
import org.geoserver.featurestemplating.readers.TemplateReader;
import org.xml.sax.helpers.NamespaceSupport;

public class TemplateBuilderMaker {

    private String textContent;

    private String name;

    private String filter;

    private String source;

    private boolean isCollection;

    private boolean flatOutput;

    private boolean isRoot;

    private Map<String, Object> encodingOptions;

    private NamespaceSupport namespaces;

    private String separator = "_";

    public TemplateBuilderMaker() {
        this.encodingOptions = new HashMap<>();
    }

    public TemplateBuilderMaker textContent(String textContent) {
        this.textContent = textContent;
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

    public TemplateBuilderMaker root(boolean root) {
        isRoot = root;
        return this;
    }

    public TemplateBuilderMaker encodingOption(String name, Object value) {
        this.encodingOptions.put(name, value);
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

    public void reset() {
        this.encodingOptions = new HashMap<>();
        this.filter = null;
        this.flatOutput = false;
        this.isCollection = false;
        this.isRoot = false;
        this.name = null;
        this.source = null;
        this.textContent = null;
        this.namespaces = null;
        this.separator = "_";
    }

    private IteratingBuilder buildIteratingBuilder() {
        IteratingBuilder iteratingBuilder;
        if (flatOutput) iteratingBuilder = new FlatIteratingBuilder(name, namespaces, separator);
        else iteratingBuilder = new IteratingBuilder(name, namespaces);
        if (source != null) iteratingBuilder.setSource(source);
        if (filter != null) iteratingBuilder.setFilter(filter);
        if (!encodingOptions.isEmpty()) iteratingBuilder.getEncodingHints().putAll(encodingOptions);

        iteratingBuilder.setRootCollection(isRoot);
        return iteratingBuilder;
    }

    private CompositeBuilder buildCompositeBuilder() {
        CompositeBuilder compositeBuilder;
        if (flatOutput) compositeBuilder = new FlatCompositeBuilder(name, namespaces, separator);
        else compositeBuilder = new CompositeBuilder(name, namespaces);

        if (source != null) compositeBuilder.setSource(source);
        if (filter != null) compositeBuilder.setFilter(filter);
        if (!encodingOptions.isEmpty()) compositeBuilder.getEncodingHints().putAll(encodingOptions);

        return compositeBuilder;
    }

    private DynamicValueBuilder buildDynamicBuilder() {
        DynamicValueBuilder dynamicValueBuilder;
        if (flatOutput)
            dynamicValueBuilder = new FlatDynamicBuilder(name, textContent, namespaces, separator);
        else dynamicValueBuilder = new DynamicValueBuilder(name, textContent, namespaces);
        if (filter != null) dynamicValueBuilder.setFilter(filter);
        if (!encodingOptions.isEmpty())
            dynamicValueBuilder.getEncodingHints().putAll(encodingOptions);
        return dynamicValueBuilder;
    }

    private StaticBuilder buildStaticBuilder() {
        StaticBuilder staticBuilder;
        if (flatOutput)
            staticBuilder = new FlatStaticBuilder(name, textContent, namespaces, separator);
        else staticBuilder = new StaticBuilder(name, textContent, namespaces);

        if (filter != null) staticBuilder.setFilter(filter);
        if (!encodingOptions.isEmpty()) staticBuilder.getEncodingHints().putAll(encodingOptions);

        return staticBuilder;
    }

    public TemplateBuilder build() {
        TemplateBuilder result;
        if (textContent == null) {
            if (isCollection) result = buildIteratingBuilder();
            else result = buildCompositeBuilder();
        } else {
            if (textContent.contains(TemplateReader.EXPRSTART)) result = buildDynamicBuilder();
            else result = buildStaticBuilder();
        }
        reset();
        return result;
    }
}
