package org.geoserver.featurestemplating.web;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.featurestemplating.configuration.TemplateConfiguration;
import org.geoserver.featurestemplating.configuration.TemplateEntry;
import org.geoserver.web.publish.PublishedEditTabPanel;

public class TemplateTabPanel extends PublishedEditTabPanel<LayerInfo> {


    /**
     * @param id The id given to the panel.
     * @param model The model for the panel which wraps a {@link LayerInfo} instance.
     */
    public TemplateTabPanel(String id, IModel<LayerInfo> model) {
        super(id, model);
        LayerInfo li = model.getObject();
        ResourceInfo ri = li.getResource();
        if (!(ri instanceof FeatureTypeInfo)) this.setEnabled(false);
        else {
            FeatureTypeInfo fti = (FeatureTypeInfo) ri;
            TemplateConfiguration comf=fti.getMetadata().get(TemplateConfiguration.METADATA_KEY,TemplateConfiguration.class);
            add(new TemplateEntryTablePanel("templatesTable", fti));
            add(
                    new TemplateEntryConfigurationPanel(
                            "entryConfiguration", new Model<>(new TemplateEntry()),comf));
        }
    }
}
