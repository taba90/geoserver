package org.geoserver.featurestemplating.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTargetListenerCollection;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.featurestemplating.configuration.SupportedMimeType;
import org.geoserver.featurestemplating.configuration.Template;
import org.geoserver.featurestemplating.configuration.TemplateInfo;
import org.geoserver.featurestemplating.configuration.TemplateInfoDaoImpl;
import org.geoserver.featurestemplating.configuration.TemplateLayerConfig;
import org.geoserver.featurestemplating.configuration.TemplateRule;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.web.wicket.LiveCollectionModel;
import org.opengis.feature.type.Name;

public class TemplateRuleConfigurationPanel extends Panel {

    IModel<TemplateRule> templateRuleModel;
    TemplateRulesTablePanel tablePanel;
    Form<TemplateRule> form;
    DropDownChoice<TemplateInfo> templateInfoDropDownChoice;

    private Name featureTypeInfoName;

    public TemplateRuleConfigurationPanel(
            String id,
            IModel<TemplateRule> model,
            boolean isUpdate,
            Name name) {
        super(id, model);
        this.featureTypeInfoName=name;
        this.templateRuleModel = model;
        initUI(templateRuleModel, isUpdate);
    }

    private void initUI(IModel<TemplateRule> model, boolean isUpdate) {
        this.form =
                new Form<TemplateRule>("theForm", model){
                    @Override
                    protected void onSubmit() {
                        FeatureTypeInfo fti=getCatalog().getFeatureTypeByName(featureTypeInfoName);
                        TemplateLayerConfig layerConfig=fti.getMetadata().get(TemplateLayerConfig.METADATA_KEY,TemplateLayerConfig.class);
                        TemplateRule rule=model.getObject();
                        TemplateInfo ti=templateInfoDropDownChoice.getConvertedInput();
                        if (ti!=null){
                            if(layerConfig!=null&& layerConfig.getTemplateRules()!=null && !layerConfig.getTemplateRules().contains(rule)) {
                                layerConfig.getTemplateRules().add(rule);
                                fti.getMetadata().put(TemplateLayerConfig.METADATA_KEY,layerConfig);
                                getCatalog().save(fti);
                            }
                        }
                    }
                };
        form.setOutputMarkupId(true);
        add(form);
        ChoiceRenderer<TemplateInfo> templateInfoChoicheRenderer=new ChoiceRenderer<>("fullName","identifier");
        templateInfoDropDownChoice =
                new DropDownChoice<>(
                        "templateIdentifier",
                        new PropertyModel<>(model, "templateInfo"),
                        getTemplateInfoList(),
                        templateInfoChoicheRenderer);
        templateInfoDropDownChoice.setNullValid(true);
        form.add(templateInfoDropDownChoice);
        DropDownChoice<String> mimeTypeDropDown = new OutputFormatsDropDown("outputFormats",
                new PropertyModel<>(model,"outputFormat"));
        mimeTypeDropDown.setOutputMarkupId(true);
        DropDownChoice<String> serviceDropDown =
                new DropDownChoice<>(
                        "services", new PropertyModel<>(model, "service"), getSupportedServices());
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
        form.add(
                new CheckBox("singleFeature", new PropertyModel<>(model, "singleFeatureTemplate")));
        form.add(new TextArea<>("cqlFilter", new PropertyModel<>(model, "cqlFilter")));
        form.add(new TextField<>("regex", new PropertyModel<>(model, "regex")));
        AjaxSubmitLink submitLink =
                new AjaxSubmitLink("save") {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        super.onSubmit(target, form);
                        TemplateRule rule = (TemplateRule) form.getModelObject();
                        TemplateInfo ti=templateInfoDropDownChoice.getConvertedInput();
                        if (ti!=null){
                            rule.setTemplateName(ti.getTemplateName());
                            rule.setTemplateIdentifier(ti.getIdentifier());
                        }
                        updateModelRules(rule);
                        target.add(tablePanel);
                        target.add(tablePanel.getTable());
                        form.clearInput();
                        target.add(form);
                    }
                };
        form.add(submitLink);
        form.add(
                new AjaxSubmitLink("cancel") {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        Form<TemplateRule> ruleForm=(Form<TemplateRule>)  form;
                        ruleForm.clearInput();
                        ruleForm.setModel(new Model<>(new TemplateRule()));
                        target.add(ruleForm);
                        target.add(this);
                    }
                });
    }

    private List<String> getSupportedServices() {
        return Arrays.asList("WFS");
    }

    private List<String> getSupportedOperations() {
        return Arrays.asList("GetFeature");
    }

    private void setModel(Model<TemplateRule> model) {
        this.templateRuleModel = model;
        super.setDefaultModel(model);
    }

    protected List<TemplateInfo> getTemplateInfoList() {
        return TemplateInfoDaoImpl.get().findAll();
    }

    private Catalog getCatalog() {
        return (Catalog) GeoServerExtensions.bean("catalog");
    }

    void setTemplateRuleTablePanel(TemplateRulesTablePanel panel) {
        this.tablePanel = panel;
    }

    private void updateModelRules(TemplateRule rule){
        Set<TemplateRule> rules =
                new HashSet<>(tablePanel.getModel().getObject());
        rules.add(rule);
        tablePanel.getModel().setObject(rules);
        tablePanel.modelChanged();
        tablePanel.getTable().modelChanged();
    }


}
