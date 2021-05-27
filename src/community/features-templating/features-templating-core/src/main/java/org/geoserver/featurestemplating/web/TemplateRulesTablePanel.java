package org.geoserver.featurestemplating.web;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.checkerframework.checker.units.qual.A;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.MetadataMap;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.featurestemplating.configuration.TemplateLayerConfig;
import org.geoserver.featurestemplating.configuration.TemplateRule;
import org.geoserver.web.data.SelectionRemovalLink;
import org.geoserver.web.data.layer.NewLayerPage;
import org.geoserver.web.util.MapModel;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.LiveCollectionModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.geoserver.featurestemplating.web.TemplateRuleProvider.CQL_FILTER;
import static org.geoserver.featurestemplating.web.TemplateRuleProvider.NAME;
import static org.geoserver.featurestemplating.web.TemplateRuleProvider.OPERATION;
import static org.geoserver.featurestemplating.web.TemplateRuleProvider.OUTPUT_FORMAT;
import static org.geoserver.featurestemplating.web.TemplateRuleProvider.REGEX;
import static org.geoserver.featurestemplating.web.TemplateRuleProvider.SERVICE;
import static org.geoserver.featurestemplating.web.TemplateRuleProvider.SINGLE_FEATURE;

public class TemplateRulesTablePanel extends Panel {

    private static final String HEADER_PANEL = "headerPanel";

    private GeoServerTablePanel<TemplateRule> table;

    SelectionRemovalLink removal;
    GeoServerDialog dialog;

    private LiveCollectionModel<TemplateRule, List<TemplateRule>> model;

    public TemplateRulesTablePanel(String id, IModel<MetadataMap> metadataModel) {

        super(id);
        MapModel<TemplateLayerConfig> mapModelLayerConf=new MapModel<>(metadataModel,TemplateLayerConfig.METADATA_KEY);
        if (mapModelLayerConf.getObject()==null)
            mapModelLayerConf.setObject(new TemplateLayerConfig());
        this.model= LiveCollectionModel.list(new PropertyModel<List<TemplateRule>>(mapModelLayerConf,"templateRules"));
        GeoServerDataProvider<TemplateRule> dataProvider = new TemplateRuleProvider(model);
        table = new TemplateRuleTable("table", dataProvider, true);
        table.setOutputMarkupId(true);
        add(table);
    }

    protected Component headerPanel() {
        Fragment header = new Fragment(HEADER_PANEL, "header", this);

        // the add button
        header.add(new BookmarkablePageLink<Void>("addNew", NewLayerPage.class));

        // the removal button
        // header.add(removal = new AjaxLink<Void>("removeSelected", table, dialog));
        removal.setOutputMarkupId(true);
        removal.setEnabled(false);

        return header;
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
                return new Label(id, NAME.getModel(itemModel));
            else if (property.equals(OUTPUT_FORMAT)) return new Label(id, OUTPUT_FORMAT.getModel(itemModel));
            else if (property.equals(REGEX)) return new Label(id, REGEX.getModel(itemModel));
            else if (property.equals(SERVICE))
                return new Label(id, SERVICE.getModel(itemModel));
            else if (property.equals(OPERATION))
                return new Label(id, OPERATION.getModel(itemModel));
            else if (property.equals(CQL_FILTER))
                return new Label(id,CQL_FILTER.getModel(itemModel));
            else if(property.equals(SINGLE_FEATURE)) {
                return new Label(id,SINGLE_FEATURE.getModel(itemModel));
            }
            return null;
        }
    }

    public LiveCollectionModel<TemplateRule, List<TemplateRule>> getModel() {
        return model;
    }

    public GeoServerTablePanel<TemplateRule> getTable (){
        return table;
    }
}
