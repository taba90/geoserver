package org.geoserver.featurestemplating.web;

import static org.geoserver.featurestemplating.web.TemplateRuleProvider.CQL_FILTER;
import static org.geoserver.featurestemplating.web.TemplateRuleProvider.NAME;
import static org.geoserver.featurestemplating.web.TemplateRuleProvider.OPERATION;
import static org.geoserver.featurestemplating.web.TemplateRuleProvider.OUTPUT_FORMAT;
import static org.geoserver.featurestemplating.web.TemplateRuleProvider.REGEX;
import static org.geoserver.featurestemplating.web.TemplateRuleProvider.SERVICE;
import static org.geoserver.featurestemplating.web.TemplateRuleProvider.SINGLE_FEATURE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.geoserver.catalog.MetadataMap;
import org.geoserver.featurestemplating.configuration.TemplateInfo;
import org.geoserver.featurestemplating.configuration.TemplateInfoDao;
import org.geoserver.featurestemplating.configuration.TemplateInfoDaoImpl;
import org.geoserver.featurestemplating.configuration.TemplateLayerConfig;
import org.geoserver.featurestemplating.configuration.TemplateRule;
import org.geoserver.web.CatalogIconFactory;
import org.geoserver.web.data.SelectionRemovalLink;
import org.geoserver.web.data.layer.NewLayerPage;
import org.geoserver.web.util.MapModel;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.LiveCollectionModel;
import org.geoserver.web.wicket.SimpleAjaxLink;

public class TemplateRulesTablePanel extends Panel {

    private static final String HEADER_PANEL = "headerPanel";

    private GeoServerTablePanel<TemplateRule> table;

    private AjaxLink<Object> remove;

    private TemplateRuleConfigurationPanel configurationPanel;
    GeoServerDialog dialog;

    private LiveCollectionModel<TemplateRule, Set<TemplateRule>> model;

    public TemplateRulesTablePanel(String id, IModel<MetadataMap> metadataModel) {

        super(id);
        MapModel<TemplateLayerConfig> mapModelLayerConf =
                new MapModel<>(metadataModel, TemplateLayerConfig.METADATA_KEY);
        if (mapModelLayerConf.getObject() == null)
            mapModelLayerConf.setObject(new TemplateLayerConfig());
        this.model =
                LiveCollectionModel.set(
                        new PropertyModel<Set<TemplateRule>>(mapModelLayerConf, "templateRules"));
        GeoServerDataProvider<TemplateRule> dataProvider = new TemplateRuleProvider(model);
        table = new TemplateRuleTable("table", dataProvider, true);
        table.setOutputMarkupId(true);
        add(
                remove =
                        new AjaxLink<Object>("removeSelected") {
                            private static final long serialVersionUID = 2421854498051377608L;

                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                Set<TemplateRule> rules=TemplateRulesTablePanel.this.getModel().getObject();
                                Set<TemplateRule> updated=new HashSet<>(rules);
                                updated.removeAll(table.getSelection());
                                TemplateRulesTablePanel.this.getModel().setObject(updated);
                                TemplateRulesTablePanel.this.modelChanged();
                                table.modelChanged();
                                target.add(table);
                            }
                        });
        add(table);
    }


    public class TemplateRuleTable extends GeoServerTablePanel<TemplateRule> {

        public TemplateRuleTable(
                String id, GeoServerDataProvider<TemplateRule> dataProvider, boolean selectable) {
            super(id, dataProvider, selectable);
        }

        @Override
        protected Component getComponentForProperty(
                String id,
                IModel<TemplateRule> itemModel,
                GeoServerDataProvider.Property<TemplateRule> property) {
            if (property.equals(NAME))
                return new SimpleAjaxLink<TemplateRule>(
                        id, itemModel, NAME.getModel(itemModel)) {

                    @Override
                    protected void onClick(AjaxRequestTarget target) {
                        TemplateRule rule=itemModel.getObject();
                        configurationPanel.form.getModel().setObject(rule);
                        configurationPanel.form.modelChanged();
                        target.add(configurationPanel.form);
                    }
                };
            else if (property.equals(OUTPUT_FORMAT))
                return new Label(id, OUTPUT_FORMAT.getModel(itemModel));
            else if (property.equals(REGEX)) return new Label(id, REGEX.getModel(itemModel));
            else if (property.equals(SERVICE)) return new Label(id, SERVICE.getModel(itemModel));
            else if (property.equals(OPERATION))
                return new Label(id, OPERATION.getModel(itemModel));
            else if (property.equals(CQL_FILTER))
                return new Label(id, CQL_FILTER.getModel(itemModel));
            else if (property.equals(SINGLE_FEATURE)) {
                return new Label(id,SINGLE_FEATURE.getModel(itemModel));
            }
            return null;
        }
    }

    public LiveCollectionModel<TemplateRule, Set<TemplateRule>> getModel() {
        return model;
    }

    public GeoServerTablePanel<TemplateRule> getTable() {
        return table;
    }

    public void setConfigurationPanel (TemplateRuleConfigurationPanel panel){
        this.configurationPanel=panel;
    }

}
