package org.geoserver.featurestemplating.web;

import freemarker.core.TemplateCombinedMarkupOutputModel;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.featurestemplating.configuration.TemplateConfiguration;
import org.geoserver.featurestemplating.configuration.TemplateEntry;
import org.geoserver.web.data.SelectionRemovalLink;
import org.geoserver.web.data.layer.NewLayerPage;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;

public class TemplateEntryTablePanel extends Panel {

    private static final String HEADER_PANEL = "headerPanel";

    private GeoServerTablePanel<TemplateEntry> table;

    SelectionRemovalLink removal;
    GeoServerDialog dialog;

    public TemplateEntryTablePanel(String id, FeatureTypeInfo featureTypeInfo) {

        super(id);
        TemplateConfiguration configuration=featureTypeInfo.getMetadata().get(TemplateConfiguration.METADATA_KEY,TemplateConfiguration.class);
        if (configuration==null){
            configuration=new TemplateConfiguration();
            featureTypeInfo.getMetadata().put(TemplateConfiguration.METADATA_KEY,configuration);
        }
        table = new TemplateEntryTable("table", new TemplateEntryProvider(configuration), true);
        table.setOutputMarkupId(true);
        add(table);
        // add(dialog = new GeoServerDialog("dialog"));
        // add(headerPanel());
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
}
