package org.geoserver.featurestemplating.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.event.CatalogListener;
import org.geoserver.featurestemplating.configuration.SupportedMimeType;
import org.geoserver.featurestemplating.configuration.Template;
import org.geoserver.featurestemplating.configuration.TemplateLayerConfig;
import org.geoserver.featurestemplating.configuration.TemplateRule;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.web.wicket.LiveCollectionModel;

public class TemplateRuleConfigurationPanel extends Panel {

    IModel<TemplateRule> templateRuleModel;
    TemplateRulesTablePanel tablePanel;

    public TemplateRuleConfigurationPanel(String id, IModel<TemplateRule> model, boolean isUpdate, Supplier<LiveCollectionModel<TemplateRule,List<TemplateRule>>> templateRuleSupplier) {
        super(id, model);
        this.templateRuleModel =model;
        initUI(templateRuleModel,isUpdate);
    }

    private void initUI(IModel<TemplateRule> model, boolean isUpdate) {
        Form<TemplateRule> form=new Form<TemplateRule>("theForm",model){
            @Override
            protected void onSubmit() {
                super.onSubmit();
            }
        };
        form.setOutputMarkupId(true);
        add(form);
        form.add(new TextField<>("templateName", new PropertyModel<>(model, "templateName")));
        DropDownChoice<String> mimeTypeDropDown =
                new DropDownChoice<>(
                        "outputFormats",
                        new PropertyModel<>(model, "outputFormat"),
                        getSupportedOutputFormats());
        mimeTypeDropDown.setOutputMarkupId(true);
        DropDownChoice<String> serviceDropDown =
                new DropDownChoice<>(
                        "services",
                        new PropertyModel<>(model, "service"),
                        getSupportedServices());
        serviceDropDown.setOutputMarkupId(true);
        DropDownChoice<String> operationsDropDown =
                new DropDownChoice<>(
                        "operations",
                        new PropertyModel<>(model, "operation"),
                        getSupportedOperations());
        operationsDropDown.setOutputMarkupId(true);
        form.add(mimeTypeDropDown);
        form.add(serviceDropDown);
        form.add(operationsDropDown);
        form.add(new CheckBox("singleFeature",new PropertyModel<>(model,"singleFeatureTemplate")));
        form.add(new TextArea<>("cqlFilter",new PropertyModel<>(model,"cqlFilter")));
        form.add(new TextField<>("regex", new PropertyModel<>(model, "regex")));
        AjaxSubmitLink submitLink=new AjaxSubmitLink("save"){
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                TemplateRule rule=(TemplateRule)form.getModelObject();
                List<TemplateRule> rules=new ArrayList<>(tablePanel.getModel().getObject());
                rules.add(rule);
                tablePanel.getModel().setObject(rules);
                tablePanel.modelChanged();
                tablePanel.getTable().modelChanged();
                target.add(tablePanel);
                target.add(tablePanel.getTable());
                form.clearInput();
                target.add(form);
            }
        };
        form.add(submitLink);
        form.add(new Button("cancel") {
            @Override
            public void onSubmit() {
                form.clearInput();
            }
        });
    }

    private List<String> getSupportedOutputFormats() {
        return Stream.of(SupportedMimeType.values())
                .map(smt -> smt.name())
                .collect(Collectors.toList());
    }

    private List<String> getSupportedServices() {
        return Arrays.asList("WFS");
    }

    private List<String> getSupportedOperations() {
        return Arrays.asList("GetFeature");
    }

    private void setModel(Model<TemplateRule> model){
        this.templateRuleModel =model;
        super.setDefaultModel(model);
    }

    private Catalog getCatalog(){
       return (Catalog) GeoServerExtensions.bean("catalog");
    }

    void setTemplateRuleTablePanel(TemplateRulesTablePanel panel){
        this.tablePanel=panel;
    }
}
