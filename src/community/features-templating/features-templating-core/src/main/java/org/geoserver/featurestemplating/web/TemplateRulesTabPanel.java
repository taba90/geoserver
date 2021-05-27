package org.geoserver.featurestemplating.web;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.featurestemplating.configuration.TemplateRule;
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
            FeatureTypeInfo fti = (FeatureTypeInfo) ri;
            add(new TemplateRulesTablePanel("rulesTable", fti));
            add(
                    new TemplateRuleConfigurationPanel(
                            "ruleConfiguration", new Model<>(new TemplateRule()),fti));
        }
    }
}
