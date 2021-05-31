package org.geoserver.featurestemplating.web;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.MetadataMap;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.featurestemplating.configuration.FeatureTypeTemplateListener;
import org.geoserver.featurestemplating.configuration.TemplateInfoDao;
import org.geoserver.featurestemplating.configuration.TemplateInfoDaoImpl;
import org.geoserver.featurestemplating.configuration.TemplateRule;
import org.geoserver.web.publish.PublishedEditTabPanel;
import org.opengis.feature.type.Name;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TemplateRulesTabPanel extends PublishedEditTabPanel<LayerInfo> {

    public TemplateRuleConfigurationPanel configurationPanel;
    /**
     * @param id The id given to the panel.
     * @param model The model for the panel which wraps a {@link LayerInfo} instance.
     */
    public TemplateRulesTabPanel(String id, IModel<LayerInfo> model) {
        super(id, model);
        LayerInfo li = model.getObject();
        ResourceInfo ri = li.getResource();
        Name name=ri.getQualifiedNativeName();
        if (!(ri instanceof FeatureTypeInfo)) this.setEnabled(false);
        else {
            TemplateInfoDao infoDao=TemplateInfoDaoImpl.get();
            FeatureTypeTemplateListener listener=new FeatureTypeTemplateListener((FeatureTypeInfo)ri);
            infoDao.addTemplateListener(listener);
            PropertyModel<ResourceInfo> resource = new PropertyModel<>(model, "resource");
            PropertyModel<MetadataMap> metadata = new PropertyModel<>(resource, "metadata");
            TemplateRulesTablePanel tablePanel =
                    new TemplateRulesTablePanel("rulesTable", metadata);
            tablePanel.setOutputMarkupId(true);
            add(tablePanel);
            configurationPanel =
                    new TemplateRuleConfigurationPanel(
                            "ruleConfiguration",
                            new Model<>(new TemplateRule()),
                            false,
                            name);
            configurationPanel.setTemplateRuleTablePanel(tablePanel);
            configurationPanel.setOutputMarkupId(true);
            tablePanel.setConfigurationPanel(configurationPanel);
            add(configurationPanel);
        }
    }

    @Override
    public void beforeSave() {
        super.beforeSave();
    }

    @Override
    public void save() throws IOException {
        TemplateRule ruleModel=configurationPanel.templateRuleModel.getObject();
        Set<TemplateRule> rules=new HashSet<>(
                configurationPanel.tablePanel.getModel().getObject());
        rules.add(ruleModel);
        configurationPanel.tablePanel.getModel().setObject(rules);

        super.save();
    }
}
