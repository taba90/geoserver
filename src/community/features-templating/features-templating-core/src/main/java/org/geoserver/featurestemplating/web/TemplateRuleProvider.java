package org.geoserver.featurestemplating.web;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.geoserver.featurestemplating.configuration.TemplateLayerConfig;
import org.geoserver.featurestemplating.configuration.TemplateRule;
import org.geoserver.web.wicket.GeoServerDataProvider;

public class TemplateRuleProvider extends GeoServerDataProvider<TemplateRule> {
    public static final Property<TemplateRule> NAME = new BeanProperty<>("name", "templateName");
    public static final Property<TemplateRule> OUTPUT_FORMAT = new BeanProperty<>("outputFormat", "outputFormat");
    public static final Property<TemplateRule> SERVICE = new BeanProperty<>("service", "service");
    public static final Property<TemplateRule> OPERATION = new BeanProperty<>("operation", "operation");
    public static final Property<TemplateRule> SINGLE_FEATURE = new BeanProperty<>("singleFeatureTemplate", "singleFeatureTemplate");
    public static final Property<TemplateRule> CQL_FILTER = new BeanProperty<>("cqlFilter", "cqlFilter");
    public static final Property<TemplateRule> REGEX = new BeanProperty<>("regex", "regex");
    private TemplateLayerConfig layerConfig;



    public TemplateRuleProvider(TemplateLayerConfig layerConfig) {
        this.layerConfig = layerConfig;
    }

    @Override
    protected List<Property<TemplateRule>> getProperties() {
        return Arrays.asList(NAME, OUTPUT_FORMAT,SERVICE,OPERATION,SINGLE_FEATURE,CQL_FILTER,REGEX);
    }

    @Override
    protected List<TemplateRule> getItems() {
        List<TemplateRule> entries;
        if (layerConfig != null) {
            entries = layerConfig.getTemplateRules();
        } else {
            entries = Collections.emptyList();
        }
        return entries;
    }
}
