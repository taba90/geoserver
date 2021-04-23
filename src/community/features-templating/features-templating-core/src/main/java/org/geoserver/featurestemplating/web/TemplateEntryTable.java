package org.geoserver.featurestemplating.web;

import static org.geoserver.featurestemplating.web.TemplateEntryProvider.MIME;
import static org.geoserver.featurestemplating.web.TemplateEntryProvider.NAME;
import static org.geoserver.featurestemplating.web.TemplateEntryProvider.OPERATION;
import static org.geoserver.featurestemplating.web.TemplateEntryProvider.REGEX_RULE;
import static org.geoserver.featurestemplating.web.TemplateEntryProvider.SERVICE;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.geoserver.featurestemplating.configuration.TemplateEntry;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerTablePanel;

public class TemplateEntryTable extends GeoServerTablePanel<TemplateEntry> {

    public TemplateEntryTable(
            String id, GeoServerDataProvider<TemplateEntry> dataProvider, boolean selectable) {
        super(id, dataProvider, selectable);
    }

    @Override
    protected Component getComponentForProperty(
            String id,
            IModel<TemplateEntry> itemModel,
            GeoServerDataProvider.Property<TemplateEntry> property) {
        if (property.equals(NAME)) {
            return new Label(id, NAME.getModel(itemModel));
        } else if (property.equals(MIME)) return new Label(id, MIME.getModel(itemModel));
        else if (property.equals(REGEX_RULE)) return new Label(id, REGEX_RULE.getModel(itemModel));
        else if (property.equals(SERVICE)) {
            return new Label(id, SERVICE.getModel(itemModel));
        }else if (property.equals(OPERATION)) {
            return new Label(id, OPERATION.getModel(itemModel));
        }
        return null;
    }
}
