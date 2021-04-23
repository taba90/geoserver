package org.geoserver.featurestemplating.web;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.catalog.Catalog;
import org.geoserver.featurestemplating.configuration.TemplateData;
import org.geoserver.featurestemplating.configuration.TemplateEntry;
import org.geoserver.web.wicket.CodeMirrorEditor;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.List;
import java.util.stream.Collectors;

public class TemplateDataConfigurationPanel extends Panel {

    private Catalog catalog;

    public TemplateDataConfigurationPanel(String id, Catalog catalog, Model<TemplateData> model) {
        super(id);
        this.catalog=catalog;
        initUI(model);
    }

    private void initUI(IModel<TemplateData> model) {
        add (new TextField<>("templateName", new PropertyModel<>(model, "templateName")));
        add (new TextField<>("templateFileName",new PropertyModel<>(model, "templateFileName")));
        DropDownChoice<String> wsDropDown =
                new DropDownChoice<>(
                        "workspace",
                        new PropertyModel<>(model, "workspace"),
                        getWorkspaces());
        DropDownChoice<String> ftiDropDown =
                new DropDownChoice<>(
                        "featureTypeInfo",
                        new PropertyModel<>(model, "featureTypeInfo"),
                        getFeatureTypesInfo());

        add(wsDropDown);
        add(ftiDropDown);
        add(new CodeMirrorEditor("templateEditor", "xml", new Model<>(getRawTemplate())));
    }

    private List<String> getWorkspaces(){
        return catalog.getWorkspaces().stream().map(w->w.getName()).collect(Collectors.toList());
    }

    private List<String> getFeatureTypesInfo(){
        return catalog.getFeatureTypes().stream().map(fti->fti.getName()).collect(Collectors.toList());
    }
    private String getRawTemplate() {
        return "";
    }

}
