package org.geoserver.featurestemplating.web;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.featurestemplating.configuration.TemplateInfo;
import org.geoserver.featurestemplating.configuration.TemplateInfoDao;
import org.geoserver.featurestemplating.configuration.TemplateRule;

public class TemplateRuleConfigurationPanel extends Panel {

    CompoundPropertyModel<TemplateRule> templateRuleModel;
    TemplateRulesTablePanel tablePanel;
    Form<TemplateRule> theForm;
    DropDownChoice<TemplateInfo> templateInfoDropDownChoice;
    DropDownChoice<String> mimeTypeDropDown;
    TextArea<String> cqlFilterArea;
    FeedbackPanel ruleFeedbackPanel;
    LayerInfo layer;
    public TemplateRuleConfigurationPanel(
            String id, CompoundPropertyModel<TemplateRule> model, boolean isUpdate, LayerInfo layer) {
        super(id, model);
        this.layer = layer;
        this.templateRuleModel = model;
        initUI(templateRuleModel, isUpdate);
    }

    private void initUI(CompoundPropertyModel<TemplateRule> model, boolean isUpdate) {
        this.theForm = new Form<>("theForm", model);
        theForm.setOutputMarkupId(true);
        theForm.add(ruleFeedbackPanel =new FeedbackPanel("ruleFeedback"));
        ruleFeedbackPanel.setOutputMarkupId(true);
        add(theForm);


        ChoiceRenderer<TemplateInfo> templateInfoChoicheRenderer =
                new ChoiceRenderer<>("fullName", "identifier");
        templateInfoDropDownChoice =
                new DropDownChoice<>(
                        "templateIdentifier",
                        model.bind("templateInfo"),
                        getTemplateInfoList(),
                        templateInfoChoicheRenderer);
        templateInfoDropDownChoice.setOutputMarkupId(true);
        theForm.add(templateInfoDropDownChoice);

        mimeTypeDropDown =
                new OutputFormatsDropDown("outputFormats", model.bind("outputFormat"));
        mimeTypeDropDown.setOutputMarkupId(true);
        theForm.add(mimeTypeDropDown);

        cqlFilterArea=new TextArea<>("cqlFilter", model.bind("cqlFilter"));
        cqlFilterArea.setOutputMarkupId(true);
        theForm.add(cqlFilterArea);
        AjaxSubmitLink submitLink =
                new AjaxSubmitLink("save") {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        super.onSubmit(target, form);
                        TemplateRule rule = theForm.getModelObject();
                        if (!validateAndReport(rule))
                            return;
                        updateModelRules(rule);
                        target.add(tablePanel);
                        target.add(tablePanel.getTable());
                        clearForm(target);
                    }

                    @Override
                    protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
                        if (theForm.hasError())
                            target.add(ruleFeedbackPanel);
                    }
                };
        theForm.add(submitLink);
        theForm.add(
                new AjaxSubmitLink("cancel") {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        clearForm(target);
                    }
                });
    }

    private boolean validateAndReport(TemplateRule rule){
        boolean result=true;
        try {
            TemplateModelsValidator validator = new TemplateModelsValidator();
            validator.validate(rule);
        } catch(TemplateConfigurationException e){
            theForm.error(e.getMessage());
            result=false;
        }
        return result;
    }

    protected List<TemplateInfo> getTemplateInfoList() {
        ResourceInfo resourceInfo=layer.getResource();
        return TemplateInfoDao.get().findByFeatureTypeInfo((FeatureTypeInfo)resourceInfo);
    }

    void setTemplateRuleTablePanel(TemplateRulesTablePanel panel) {
        this.tablePanel = panel;
    }

    private void updateModelRules(TemplateRule rule) {
        Set<TemplateRule> rules = new HashSet<>(tablePanel.getModel().getObject());
        rules.removeIf(r->r.getRuleId().equals(rule.getRuleId()));
        rules.add(rule);
        tablePanel.getModel().setObject(rules);
        tablePanel.modelChanged();
        tablePanel.getTable().modelChanged();
    }

    private void clearForm(AjaxRequestTarget target){
        theForm.clearInput();
        theForm.setModelObject(new TemplateRule());
        theForm.modelChanged();
        templateInfoDropDownChoice.modelChanged();
        mimeTypeDropDown.modelChanged();
        cqlFilterArea.modelChanged();
        target.add(theForm);
        target.add(templateInfoDropDownChoice);
        target.add(mimeTypeDropDown);
        target.add(cqlFilterArea);
    }
}
