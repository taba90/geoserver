package org.geoserver.featurestemplating.web;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.featurestemplating.configuration.Template;
import org.geoserver.featurestemplating.configuration.TemplateConfiguration;
import org.geoserver.featurestemplating.configuration.TemplateEntry;
import org.geoserver.web.wicket.GeoServerDataProvider;

public class TemplateEntryProvider extends GeoServerDataProvider<TemplateEntry> {
    public static final Property<TemplateEntry> NAME = new BeanProperty<>("name", "templateName");
    public static final Property<TemplateEntry> REGEX_RULE =
            new BeanProperty<>("regex", "rule.regex");
    public static final Property<TemplateEntry> MIME = new BeanProperty<>("mime", "mimeType");
    public static final Property<TemplateEntry> SERVICE = new BeanProperty<>("service", "operation");
    public static final Property<TemplateEntry> OPERATION = new BeanProperty<>("operation", "operation");
    private TemplateConfiguration templateConfiguration;



    public TemplateEntryProvider(TemplateConfiguration templateConfiguration) {
        this.templateConfiguration = templateConfiguration;
    }

    @Override
    protected List<Property<TemplateEntry>> getProperties() {
        return Arrays.asList(NAME, REGEX_RULE, MIME,SERVICE,OPERATION);
    }

    @Override
    protected List<TemplateEntry> getItems() {
        List<TemplateEntry> entries;
        if (templateConfiguration != null) {
            entries = templateConfiguration.getEntriesAsList();
        } else {
            entries = Collections.emptyList();
        }
        return entries;
    }
}
