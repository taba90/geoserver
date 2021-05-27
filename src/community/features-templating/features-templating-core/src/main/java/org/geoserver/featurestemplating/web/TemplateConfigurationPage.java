package org.geoserver.featurestemplating.web;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.featurestemplating.configuration.TemplateInfo;
import org.geoserver.featurestemplating.configuration.TemplateInfoDaoImpl;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;
import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.wicket.CodeMirrorEditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateConfigurationPage extends GeoServerSecuredPage {

    private boolean isNew;

    CodeMirrorEditor editor;

    private Form form;

    String rawTemplate;

    public TemplateConfigurationPage (IModel<TemplateInfo> model, boolean isNew){
        this.isNew=isNew;
        initUI(model);
    }

    private void initUI(IModel<TemplateInfo> model) {

        form = new Form<TemplateInfo>("theForm",model){
            @Override
            protected void onSubmit() {
                super.onSubmit();
                TemplateInfo templateInfo = (TemplateInfo) form.getModelObject();
                String rawTemplate=getRawTemplate();
                File destDir=templateInfo.getTemplateLocation();
                try {
                    File file = new File(destDir, templateInfo.getTemplateName() + "." + templateInfo.getExtension());
                    file.createNewFile();
                    try (FileOutputStream fos = new FileOutputStream(file, false)) {
                        fos.write(rawTemplate.getBytes());
                    }
                }catch (IOException e){
                    throw new RuntimeException(e);
                }
                TemplateInfoDaoImpl.get().saveOrUpdate(templateInfo);
            }
        };

        form.add (new TextField<>("templateName", new PropertyModel<>(model, "templateName")));
        //form.add (new TextField<>("templateLocation",new PropertyModel<>(model, "templateLocation")));
        DropDownChoice<String> templateExtension =
                new DropDownChoice<>(
                        "extension",
                        new PropertyModel<>(model, "extension"),
                        getExtensions());
        DropDownChoice<String> wsDropDown =
                new DropDownChoice<>(
                        "workspace",
                        new PropertyModel<>(model, "workspace"),
                        getWorkspaces());
        DropDownChoice<String> ftiDropDown =
                new DropDownChoice<>(
                        "featureTypeInfo",
                        new PropertyModel<>(model, "featureType"),
                        Collections.emptyList());
        form.add(wsDropDown);
        ftiDropDown.setEnabled(false);
        ftiDropDown.setOutputMarkupId(true);
        form.add(ftiDropDown);
        wsDropDown.add(new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 732177308220189475L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                ftiDropDown.setChoices(
                        getFeatureTypesInfo(wsDropDown.getConvertedInput()));
                ftiDropDown.modelChanged();
                target.add(ftiDropDown);
                ftiDropDown.setEnabled(true);
            }
        });
        this.rawTemplate=getStringTemplate(model.getObject());
        String mode;
        if (!isNew && model.getObject().getExtension().equals("json"))
            mode="javascript";
        else
            mode="xml";
        editor=new CodeMirrorEditor("templateEditor", mode, new PropertyModel<>(this,"rawTemplate"));
        editor.setOutputMarkupId(true);
        templateExtension.add(new OnChangeAjaxBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
                String mode=templateExtension.getConvertedInput();
                if (mode.equals("xml") || mode.equals("xhtml"))
                    editor.setMode("xml");
                else
                    editor.setMode("javascript");
                ajaxRequestTarget.add(editor);

            }
        });
        form.add(templateExtension);
        form.add(editor);
        editor.setTextAreaMarkupId("editor");
        editor.setMarkupId("templateEditor");
        editor.setOutputMarkupId(true);
        form.add(editor);
        add(form);
        add(getSubmit());
        add(new Link<TemplateInfoPage>("cancel") {
            @Override
            public void onClick() {
                doReturn(TemplateInfoPage.class);
            }
        });
    }

    private List<String> getWorkspaces(){
        Catalog catalog= (Catalog) GeoServerExtensions.bean("catalog");
        return catalog.getWorkspaces().stream().map(w->w.getName()).collect(Collectors.toList());
    }

    private List<String> getExtensions(){
        return Arrays.asList("xml","xhtml","json");
    }

    private List<String> getFeatureTypesInfo(String workspaceName){
        Catalog catalog= (Catalog) GeoServerExtensions.bean("catalog");
        NamespaceInfo namespaceInfo=catalog.getNamespaceByPrefix(workspaceName);
        return catalog.getFeatureTypesByNamespace(namespaceInfo).stream().map(fti->fti.getName()).collect(Collectors.toList());
    }
    private String getStringTemplate(TemplateInfo templateInfo) {
        String rawTemplate="";
        if(!isNew){
            Resource resource=templateInfo.getTemplateResource();
            try {
                rawTemplate = FileUtils.readFileToString(resource.file(), Charset.forName("UTF-8"));
            }catch (IOException io){
                throw new RuntimeException(io);
            }
        }
        return rawTemplate;
    }

    private AjaxSubmitLink getSubmit(){
        AjaxSubmitLink submitLink=new AjaxSubmitLink("save",form){
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
            }
        };
        return submitLink;
    }

    public void setRawTemplate(String rawTemplate) {
        this.rawTemplate=rawTemplate;
    }

    public String getRawTemplate() {
        return rawTemplate;
    }
}
