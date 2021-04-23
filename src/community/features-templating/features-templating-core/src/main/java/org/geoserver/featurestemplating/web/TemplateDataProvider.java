package org.geoserver.featurestemplating.web;

import org.geoserver.featurestemplating.configuration.TemplateConfiguration;
import org.geoserver.featurestemplating.configuration.TemplateData;
import org.geoserver.featurestemplating.configuration.TemplateDataDaoImpl;
import org.geoserver.web.wicket.GeoServerDataProvider;

import java.util.Arrays;
import java.util.List;

public class TemplateDataProvider extends GeoServerDataProvider<TemplateData> {

    public static final Property<TemplateData> NAME = new BeanProperty<>("templateName", "templateName");
    public static final Property<TemplateData> TEMPLATE_FILE_NAME =
            new BeanProperty<>("templateFileName", "templateFileName");
    public static final Property<TemplateData> WORKSPACE = new BeanProperty<>("workspace", "workspace");
    public static final Property<TemplateData> FEATURE_TYPE_INFO = new BeanProperty<>("featureTypeInfo", "featureTypeInfo");

    

    @Override
    protected List<Property<TemplateData>> getProperties() {
        return Arrays.asList(NAME, TEMPLATE_FILE_NAME, WORKSPACE, FEATURE_TYPE_INFO);
    }

    @Override
    protected List<TemplateData> getItems() {
        return TemplateDataDaoImpl.get().findAll();
    }
}
