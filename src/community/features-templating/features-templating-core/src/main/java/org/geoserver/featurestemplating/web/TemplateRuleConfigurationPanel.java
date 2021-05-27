package org.geoserver.featurestemplating.web;

import java.util.Arrays;
import java.util.List;
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
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.event.CatalogListener;
import org.geoserver.featurestemplating.configuration.SupportedMimeType;
import org.geoserver.featurestemplating.configuration.Template;
import org.geoserver.featurestemplating.configuration.TemplateLayerConfig;
import org.geoserver.featurestemplating.configuration.TemplateRule;
import org.geoserver.platform.GeoServerExtensions;

public class TemplateRuleConfigurationPanel extends Panel {

    IModel<TemplateRule> templateRuleModel;
    FeatureTypeInfo featureTypeInfo;
    public TemplateRuleConfigurationPanel(String id, IModel<TemplateRule> model, FeatureTypeInfo featureTypeInfo) {
        super(id, model);
        this.templateRuleModel =model;
        this.featureTypeInfo=featureTypeInfo;
        initUI(templateRuleModel);
    }

    private void initUI(IModel<TemplateRule> model) {
        Form<TemplateRule> form=new Form<>("theForm",model);
        add(form);
        form.add(new TextField<>("templateName", new PropertyModel<>(model, "templateName")));
        DropDownChoice<String> mimeTypeDropDown =
                new DropDownChoice<>(
                        "outputFormats",
                        new PropertyModel<>(model, "outputFormat"),
                        getSupportedOutputFormats());
        DropDownChoice<String> serviceDropDown =
                new DropDownChoice<>(
                        "services",
                        new PropertyModel<>(model, "service"),
                        getSupportedServices());
        DropDownChoice<String> operationsDropDown =
                new DropDownChoice<>(
                        "operations",
                        new PropertyModel<>(model, "operation"),
                        getSupportedOperations());
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
                TemplateLayerConfig config=featureTypeInfo.getMetadata().get(TemplateLayerConfig.METADATA_KEY,TemplateLayerConfig.class);
                config.addTemplateRule(rule);
                featureTypeInfo.getMetadata().put(TemplateLayerConfig.METADATA_KEY,config);
                getCatalog().save(featureTypeInfo);
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

}
