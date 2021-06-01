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
import org.apache.wicket.model.Model;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.featurestemplating.configuration.TemplateInfo;
import org.geoserver.featurestemplating.configuration.TemplateInfoDao;
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
    DropDownChoice<String> mimeTypeDropDown;
    TextArea<String> cqlFilterArea;
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
                        updateModelRules(rule);
                        target.add(tablePanel);
                        target.add(tablePanel.getTable());
                        clearForm(target);
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

    protected List<TemplateInfo> getTemplateInfoList() {
        WorkspaceInfo wi=layer.getResource().getStore().getWorkspace();
        return TemplateInfoDao.get().findByWorkspaceAndFeatureTypeInfo(wi.getName(),layer.getResource().getNativeName());
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
