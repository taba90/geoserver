package org.geoserver.featurestemplating.web;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.featurestemplating.configuration.SupportedMimeType;
import org.geoserver.featurestemplating.configuration.Template;
import org.geoserver.featurestemplating.configuration.TemplateConfiguration;
import org.geoserver.featurestemplating.configuration.TemplateEntry;
import org.geoserver.web.wicket.CodeMirrorEditor;

public class TemplateEntryConfigurationPanel extends Panel {

    IModel<TemplateEntry> templateEntryModel;
    TemplateConfiguration configuration;
    public TemplateEntryConfigurationPanel(String id, IModel<TemplateEntry> model, TemplateConfiguration templateConfiguration) {
        super(id, model);
        this.templateEntryModel=model;
        this.configuration=templateConfiguration;
        initUI(templateEntryModel);
    }

    private void initUI(IModel<TemplateEntry> model) {
        Form<TemplateEntry> form=new Form<TemplateEntry>("theForm",model);
        form.add(new TextField<>("templateName", new PropertyModel<>(model, "templateName")));
        DropDownChoice<String> mimeTypeDropDown =
                new DropDownChoice<>(
                        "mimeTypes",
                        new PropertyModel<>(model, "mimeType"),
                        getSupportedMimeTypes());
        form.add(mimeTypeDropDown);
        form.add(new TextField<>("regexRule", new PropertyModel<>(model, "rule.regex")));
        AjaxSubmitLink submitLink=new AjaxSubmitLink("save"){
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                TemplateEntry entry=(TemplateEntry)form.getModelObject();
                configuration.addOrUpdateEntry(entry);
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

    private List<String> getSupportedMimeTypes() {
        return Stream.of(SupportedMimeType.values())
                .map(smt -> smt.getMimeType())
                .collect(Collectors.toList());
    }

    private void setModel(Model<TemplateEntry> model){
        this.templateEntryModel=model;
        super.setDefaultModel(model);
    }

}
