package org.geoserver.featurestemplating.web;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.featurestemplating.configuration.TemplateConfiguration;
import org.geoserver.featurestemplating.configuration.TemplateData;
import org.geoserver.featurestemplating.configuration.TemplateEntry;
import org.geoserver.web.data.SelectionRemovalLink;
import org.geoserver.web.data.layer.NewLayerPage;
import org.geoserver.web.data.layergroup.LayerGroupEditPage;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;

public class TemplateDataTablePanel extends Panel {


    private GeoServerTablePanel<TemplateData> table;

    SelectionRemovalLink removal;
    GeoServerDialog dialog;


    public TemplateDataTablePanel(String id) {

        super(id);
        table = new TemplateDataTable("table", new TemplateDataProvider(), true);
        table.setOutputMarkupId(true);
        add(table);
        add(dialog = new GeoServerDialog("dialog"));
        // add(dialog = new GeoServerDialog("dialog"));
        // add(headerPanel());
    }
}
