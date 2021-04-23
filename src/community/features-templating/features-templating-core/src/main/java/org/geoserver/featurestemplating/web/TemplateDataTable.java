package org.geoserver.featurestemplating.web;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.geoserver.featurestemplating.configuration.TemplateData;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerTablePanel;

import static org.geoserver.featurestemplating.web.TemplateDataProvider.FEATURE_TYPE_INFO;
import static org.geoserver.featurestemplating.web.TemplateDataProvider.NAME;
import static org.geoserver.featurestemplating.web.TemplateDataProvider.TEMPLATE_FILE_NAME;
import static org.geoserver.featurestemplating.web.TemplateDataProvider.WORKSPACE;

public class TemplateDataTable extends GeoServerTablePanel<TemplateData> {

    public TemplateDataTable(
            String id, GeoServerDataProvider<TemplateData> dataProvider, boolean selectable) {
        super(id, dataProvider, selectable);
    }

    @Override
    protected Component getComponentForProperty(
            String id,
            IModel<TemplateData> itemModel,
            GeoServerDataProvider.Property<TemplateData> property) {
        if (property.equals(NAME)) {
            return new Label(id, NAME.getModel(itemModel));
        } else if (property.equals(TEMPLATE_FILE_NAME)) return new Label(id, TEMPLATE_FILE_NAME.getModel(itemModel));
        else if (property.equals(WORKSPACE)) return new Label(id, WORKSPACE.getModel(itemModel));
        else if (property.equals(FEATURE_TYPE_INFO)) {
            return new Label(id, FEATURE_TYPE_INFO.getModel(itemModel));
        }
        return null;
    }
}
