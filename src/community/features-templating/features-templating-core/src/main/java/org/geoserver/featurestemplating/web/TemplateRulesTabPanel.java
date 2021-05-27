package org.geoserver.featurestemplating.web;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.MetadataMap;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.featurestemplating.configuration.TemplateRule;
import org.geoserver.web.data.layer.Resource;
import org.geoserver.web.publish.PublishedEditTabPanel;

public class TemplateRulesTabPanel  extends PublishedEditTabPanel<LayerInfo> {

    /**
     * @param id The id given to the panel.
     * @param model The model for the panel which wraps a {@link LayerInfo} instance.
     */
    public TemplateRulesTabPanel(String id, IModel<LayerInfo> model) {
        super(id, model);
        LayerInfo li = model.getObject();
        ResourceInfo ri = li.getResource();
        if (!(ri instanceof FeatureTypeInfo)) this.setEnabled(false);
        else {
            PropertyModel<ResourceInfo> resource=new PropertyModel<>(model,"resource");
            PropertyModel<MetadataMap> metadata = new PropertyModel<>(resource, "metadata");
            TemplateRulesTablePanel tablePanel=new TemplateRulesTablePanel("rulesTable", metadata);
            tablePanel.setOutputMarkupId(true);
            add(tablePanel);
            TemplateRuleConfigurationPanel confPanel=
                    new TemplateRuleConfigurationPanel(
                            "ruleConfiguration", new Model<>(new TemplateRule()),false,()->tablePanel.getModel());
            confPanel.setTemplateRuleTablePanel(tablePanel);
            confPanel.setOutputMarkupId(true);
            add(confPanel);
        }
    }
}
