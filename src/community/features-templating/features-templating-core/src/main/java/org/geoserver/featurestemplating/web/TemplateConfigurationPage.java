package org.geoserver.featurestemplating.web;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.featurestemplating.configuration.TemplateInfo;
import org.geoserver.featurestemplating.configuration.TemplateInfoDaoImpl;
import org.geoserver.featurestemplating.configuration.TemplateInfoValidator;
import org.geoserver.ows.util.ResponseUtils;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.exception.GeoServerException;
import org.geoserver.platform.resource.Resource;
import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.wicket.CodeMirrorEditor;
import org.geoserver.web.wicket.ParamResourceModel;

public class TemplateConfigurationPage extends GeoServerSecuredPage {

    private boolean isNew;

    CodeMirrorEditor editor;

    TextField templateName;

    private Form form;

    protected FileUploadField fileUploadField;

    IModel<TemplateInfo> templateInfoModel;

    DropDownChoice<String> templateExtension;

    String rawTemplate;

    AjaxSubmitLink uploadLink;

    public TemplateConfigurationPage(IModel<TemplateInfo> model, boolean isNew) {
        this.isNew = isNew;
        this.templateInfoModel = model;
        initUI(model);
    }

    private void initUI(IModel<TemplateInfo> model) {

        form =
                new Form<TemplateInfo>("theForm", model) {
                    @Override
                    protected void onSubmit() {
                        super.onSubmit();
                        TemplateInfo templateInfo = (TemplateInfo) form.getModelObject();
                        String rawTemplate = getRawTemplate();
                        TemplateInfoValidator validator= new TemplateInfoValidator(templateInfo,rawTemplate);
                        if (!validateAndReport(validator))
                            return;
                        File destDir = templateInfo.getTemplateLocation();
                        try {
                            File file =
                                    new File(
                                            destDir,
                                            templateInfo.getTemplateName()
                                                    + "."
                                                    + templateInfo.getExtension());
                            file.createNewFile();
                            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                                fos.write(rawTemplate.getBytes());
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        TemplateInfoDaoImpl.get().saveOrUpdate(templateInfo);
                    }
                };

        templateName = new TextField<>("templateName", new PropertyModel<>(model, "templateName"));
        templateName.setRequired(true);
        form.add(templateName);
        // form.add (new TextField<>("templateLocation",new PropertyModel<>(model,
        // "templateLocation")));
        templateExtension =
                new DropDownChoice<>(
                        "extension", new PropertyModel<>(model, "extension"), getExtensions());
        DropDownChoice<String> wsDropDown =
                new DropDownChoice<>(
                        "workspace", new PropertyModel<>(model, "workspace"), getWorkspaces());
        wsDropDown.setNullValid(true);
        DropDownChoice<String> ftiDropDown =
                new DropDownChoice<>(
                        "featureTypeInfo",
                        new PropertyModel<>(model, "featureType"),
                        Collections.emptyList());
        form.add(wsDropDown);
        if (wsDropDown.getValue() == null || wsDropDown.getValue() == "-1")
            ftiDropDown.setEnabled(false);
        else ftiDropDown.setChoices(getFeatureTypesInfo(wsDropDown.getModelObject()));
        ftiDropDown.setOutputMarkupId(true);
        ftiDropDown.setNullValid(true);
        form.add(ftiDropDown);
        wsDropDown.add(
                new OnChangeAjaxBehavior() {
                    private static final long serialVersionUID = 732177308220189475L;

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        ftiDropDown.setChoices(getFeatureTypesInfo(wsDropDown.getConvertedInput()));
                        ftiDropDown.modelChanged();
                        target.add(ftiDropDown);
                        ftiDropDown.setEnabled(true);
                    }
                });
        this.rawTemplate = getStringTemplate(model.getObject());
        String mode;
        if (!isNew && model.getObject().getExtension().equals("json")) mode = "javascript";
        else mode = "xml";
        editor =
                new CodeMirrorEditor(
                        "templateEditor", mode, new PropertyModel<>(this, "rawTemplate"));
        if (mode.equals("javascript"))
            editor.setModeAndSubMode(mode, model.getObject().getExtension());
        editor.setOutputMarkupId(true);
        editor.setRequired(true);
        templateExtension.add(
                new OnChangeAjaxBehavior() {
                    @Override
                    protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
                        String mode = templateExtension.getConvertedInput();
                        if (mode!=null && (mode.equals("xml") || mode.equals("xhtml"))) editor.setMode("xml");
                        else editor.setModeAndSubMode("javascript", mode);
                        ajaxRequestTarget.add(editor);
                    }
                });
        templateExtension.setRequired(true);
        form.add(templateExtension);
        form.add(editor);
        editor.setTextAreaMarkupId("editor");
        editor.setMarkupId("templateEditor");
        editor.setOutputMarkupId(true);
        form.add(editor);
        add(form);
        fileUploadField = new FileUploadField("filename");
        // Explicitly set model so this doesn't use the form model
        fileUploadField.setDefaultModel(new Model<>(""));
        form.add(fileUploadField);
        uploadLink = uploadLink();
        form.add(uploadLink);
        add(getSubmit());
        add(
                new Link<TemplateInfoPage>("cancel") {
                    @Override
                    public void onClick() {
                        doReturn(TemplateInfoPage.class);
                    }
                });
    }

    private List<String> getWorkspaces() {
        Catalog catalog = (Catalog) GeoServerExtensions.bean("catalog");
        return catalog.getWorkspaces().stream().map(w -> w.getName()).collect(Collectors.toList());
    }

    private List<String> getExtensions() {
        return Arrays.asList("xml", "xhtml", "json");
    }

    private List<String> getFeatureTypesInfo(String workspaceName) {
        Catalog catalog = (Catalog) GeoServerExtensions.bean("catalog");
        NamespaceInfo namespaceInfo = catalog.getNamespaceByPrefix(workspaceName);
        return catalog.getFeatureTypesByNamespace(namespaceInfo)
                .stream()
                .map(fti -> fti.getName())
                .collect(Collectors.toList());
    }

    private String getStringTemplate(TemplateInfo templateInfo) {
        String rawTemplate = "";
        if (!isNew) {
            Resource resource = templateInfo.getTemplateResource();
            try {
                rawTemplate = FileUtils.readFileToString(resource.file(), Charset.forName("UTF-8"));
            } catch (IOException io) {
                throw new RuntimeException(io);
            }
        }
        return rawTemplate;
    }

    public void setRawTemplate(Reader in) throws IOException {
        try (BufferedReader bin =
                in instanceof BufferedReader ? (BufferedReader) in : new BufferedReader(in)) {
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = bin.readLine()) != null) {
                builder.append(line).append("\n");
            }

            this.rawTemplate = builder.toString();
            editor.setModelObject(rawTemplate);
        }
    }

    private AjaxSubmitLink getSubmit() {
        AjaxSubmitLink submitLink =
                new AjaxSubmitLink("save", form) {
                    @Override
                    protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
                        if (form.hasError()) {
                            addFeedbackPanels(target);
                        } else {
                            doReturn(TemplateInfoPage.class);
                        }
                    }
                };
        return submitLink;
    }

    public void setRawTemplate(String rawTemplate) {
        this.rawTemplate = rawTemplate;
    }

    public String getRawTemplate() {
        return rawTemplate;
    }

    AjaxSubmitLink uploadLink() {
        return new ConfirmOverwriteSubmitLink("upload", this.form) {

            private static final long serialVersionUID = 658341311654601761L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                FileUpload upload = fileUploadField.getFileUpload();
                if (upload == null) {
                    warn("No file selected.");
                    return;
                }
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                try {
                    IOUtils.copy(upload.getInputStream(), bout);
                    getTemplateConfPage().editor.reset();
                    getTemplateConfPage()
                            .setRawTemplate(
                                    new InputStreamReader(
                                            new ByteArrayInputStream(bout.toByteArray()), "UTF-8"));
                    upload.getContentType();
                } catch (IOException e) {
                    throw new WicketRuntimeException(e);
                } catch (Exception e) {
                    getTemplateConfPage()
                            .error(
                                    "Errors occurred uploading the '"
                                            + upload.getClientFileName()
                                            + "' template");
                    LOGGER.log(
                            Level.WARNING,
                            "Errors occurred uploading the '"
                                    + upload.getClientFileName()
                                    + "' template",
                            e);
                }

                TemplateInfo templateInfo =
                        getTemplateConfPage().getTemplateInfoModel().getObject();
                    // set it
                    String fileName = upload.getClientFileName();
                if (templateInfo.getTemplateName() == null
                        || "".equals(templateInfo.getTemplateName().trim())) {
                    templateName.setModelValue(
                            new String[]{ResponseUtils.stripExtension(fileName)});
                }
                    int index = fileName.lastIndexOf(".");
                    String extension = fileName.substring(index+1);
                    templateInfo.setExtension(extension);
                    if (!extension.equals("xml")) {
                        editor.setModeAndSubMode("javascript", "json");
                    } else {
                        editor.setMode(extension);
                    }
                    editor.modelChanged();
                    editor.get(editor.getTextAreaMarkupId());
                    templateName.modelChanged();
                    templateExtension.modelChanged();
                target.add(getTemplateConfPage());
                target.add(editor);
            }
        };
    }

    class ConfirmOverwriteSubmitLink extends AjaxSubmitLink {

        private static final long serialVersionUID = 2673499149884774636L;

        public ConfirmOverwriteSubmitLink(String id) {
            super(id);
        }

        public ConfirmOverwriteSubmitLink(String id, Form<?> form) {
            super(id, form);
        }

        @Override
        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
            super.updateAjaxAttributes(attributes);
            attributes
                    .getAjaxCallListeners()
                    .add(
                            new AjaxCallListener() {
                                /** serialVersionUID */
                                private static final long serialVersionUID = 8637613472102572505L;

                                @Override
                                public CharSequence getPrecondition(Component component) {
                                    CharSequence message =
                                            new ParamResourceModel(
                                                            "confirmOverwrite",
                                                            getTemplateConfPage())
                                                    .getString();
                                    message = JavaScriptUtils.escapeQuotes(message);
                                    return "var val = attrs.event.view.document.gsEditors ? "
                                            + "attrs.event.view.document.gsEditors."
                                            + getTemplateConfPage().editor.getTextAreaMarkupId()
                                            + ".getValue() : "
                                            + "attrs.event.view.document.getElementById(\""
                                            + getTemplateConfPage().editor.getTextAreaMarkupId()
                                            + "\").value; "
                                            + "if(val != '' &&"
                                            + "!confirm('"
                                            + message
                                            + "')) return false;";
                                }
                            });
        }

        @Override
        public boolean getDefaultFormProcessing() {
            return false;
        }
    }

    TemplateConfigurationPage getTemplateConfPage() {
        return this;
    }

    public IModel<TemplateInfo> getTemplateInfoModel() {
        return templateInfoModel;
    }

    private boolean validateAndReport(TemplateInfoValidator validator){
        try {
            validator.validate();
        }catch (GeoServerException e){
            form.error(e.getMessage());
            return false;
        }
        return true;
    }
}
