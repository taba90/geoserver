package org.geoserver.featurestemplating.web;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.featurestemplating.configuration.TemplateInfo;
import org.geoserver.featurestemplating.configuration.TemplateInfoDaoImpl;
import org.geoserver.featurestemplating.configuration.TemplateLayerConfig;
import org.geoserver.featurestemplating.configuration.TemplateRule;
import org.geoserver.platform.GeoServerExtensions;
import org.opengis.feature.type.Name;

public class TemplateRuleConfigurationPanel extends Panel {

    CompoundPropertyModel<TemplateRule> templateRuleModel;
    TemplateRulesTablePanel tablePanel;
    Form<TemplateRule> theForm;
    DropDownChoice<TemplateInfo> templateInfoDropDownChoice;

    private Name featureTypeInfoName;

    public TemplateRuleConfigurationPanel(
            String id,
            CompoundPropertyModel<TemplateRule> model,
            boolean isUpdate,
            Name name) {
        super(id, model);
        this.featureTypeInfoName=name;
        this.templateRuleModel = model;
        initUI(templateRuleModel, isUpdate);
    }

    private void initUI(CompoundPropertyModel<TemplateRule> model, boolean isUpdate) {
        this.theForm =
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
        theForm.setOutputMarkupId(true);
        add(theForm);
        ChoiceRenderer<TemplateInfo> templateInfoChoicheRenderer=new ChoiceRenderer<>("fullName","identifier");
        templateInfoDropDownChoice =
                new DropDownChoice<>(
                        "templateIdentifier",
                        model.bind( "templateInfo"),
                        getTemplateInfoList(),
                        templateInfoChoicheRenderer);
        theForm.add(templateInfoDropDownChoice);
        DropDownChoice<String> mimeTypeDropDown = new OutputFormatsDropDown("outputFormats",
                model.bind("outputFormat"));
        mimeTypeDropDown.setOutputMarkupId(true);
        theForm.add(mimeTypeDropDown);
        theForm.add(
                new CheckBox("singleFeature", model.bind("singleFeatureTemplate")));
        theForm.add(new TextArea<>("cqlFilter", model.bind("cqlFilter")));
        theForm.add(new TextField<>("regex", model.bind("regex")));
        AjaxSubmitLink submitLink =
                new AjaxSubmitLink("save") {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        super.onSubmit(target, form);
                        TemplateRule rule = theForm.getModelObject();
                        updateModelRules(rule);
                        target.add(tablePanel);
                        target.add(tablePanel.getTable());
                        theForm.clearInput();
                        target.add(theForm);
                    }
                };
        theForm.add(submitLink);
        theForm.add(
                new AjaxSubmitLink("cancel") {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        theForm.clearInput();
                        theForm.setModel(new Model<>(new TemplateRule()));
                        theForm.modelChanged();
                        target.add(theForm);
                    }
                });
    }

    private List<String> getSupportedServices() {
        return Arrays.asList("WFS");
    }

    private List<String> getSupportedOperations() {
        return Arrays.asList("GetFeature");
    }

    private void setModel(CompoundPropertyModel<TemplateRule> model) {
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
