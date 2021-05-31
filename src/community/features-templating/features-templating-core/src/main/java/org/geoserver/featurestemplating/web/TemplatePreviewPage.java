package org.geoserver.featurestemplating.web;


import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.featurestemplating.configuration.SupportedMimeType;
import org.geoserver.featurestemplating.configuration.TemplateIdentifier;
import org.geoserver.featurestemplating.configuration.TemplateInfo;
import org.geoserver.featurestemplating.configuration.TemplateLayerConfig;
import org.geoserver.featurestemplating.configuration.TemplateRule;
import org.geoserver.ows.URLMangler;
import org.geoserver.ows.util.ResponseUtils;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.GeoServerSecuredPage;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.Namespace;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class TemplatePreviewPage extends GeoServerSecuredPage {


    private String output;

    private FeatureTypeInfo featureType;

    private WorkspaceInfo ws;

    private TextArea<String> textArea;

    private String url;

    ExternalLink previewLink;

    DropDownChoice<FeatureTypeInfo> featureTypesDD;

    private OutputFormatsDropDown outputFormatsDropDown;

    private TemplateRule templateRule;

    private TemplateInfo templateInfo;
    public TemplatePreviewPage(TemplateInfo templateInfo){
        initUI(templateInfo);
    }

    private void initUI(TemplateInfo templateInfo){
        TemplateRule rule=new TemplateRule();
        rule.setTemplateIdentifier(templateInfo.getIdentifier());
        rule.setTemplateName(templateInfo.getFullName());
        this.templateInfo=templateInfo;
        Model model= Model.of(rule);
        outputFormatsDropDown=new OutputFormatsDropDown("outputFormats",model);
        outputFormatsDropDown.add(new OnChangeAjaxBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
                String link=buildWFSLink();
                if (link!=null){
                    url=link;
                    previewLink.modelChanged();
                    previewLink.setEnabled(true);
                    ajaxRequestTarget.add(previewLink);
                }
            }
        });
        add(outputFormatsDropDown);
        boolean hasFeatureType=templateInfo.getFeatureType()!=null;
        boolean hasWorkspace=templateInfo.getWorkspace()!=null;
        List<WorkspaceInfo> workspaces=getWorkspaces(getCatalog());
        DropDownChoice<WorkspaceInfo> workspaceInfoDropDownChoice = new DropDownChoice<>("workspaces",new PropertyModel<>(this,"ws"),workspaces);
        WorkspaceInfo wi=null;
        if (hasWorkspace){
            wi=workspaces.stream().filter(ws->ws.getName().equals(templateInfo.getWorkspace())).findFirst().get();
            this.ws=wi;
            workspaceInfoDropDownChoice.setEnabled(false);
        }

        workspaceInfoDropDownChoice.add(new OnChangeAjaxBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
                WorkspaceInfo wi=workspaceInfoDropDownChoice.getModelObject();
                featureTypesDD.setChoices(getFeatureTypes(getCatalog(),wi));
                featureTypesDD.setEnabled(true);
                String link=buildWFSLink();
                if (link!=null){
                    url=link;
                    previewLink.modelChanged();
                    previewLink.setEnabled(true);
                    ajaxRequestTarget.add(previewLink);
                }
                ajaxRequestTarget.add(featureTypesDD);
            }
        });
        add(workspaceInfoDropDownChoice);
        List<FeatureTypeInfo> featureTypes;
        if (hasWorkspace){
            featureTypes=getFeatureTypes(getCatalog(),wi);
            if (hasFeatureType){
                this.featureType=featureTypes.stream().filter(fti->fti.getName().equals(templateInfo.getFeatureType())).findFirst().get();
            }
        } else {
            featureTypes=Collections.emptyList();
        }
        featureTypesDD= new DropDownChoice<>("featureTypes",new PropertyModel<>(this,"featureType"),featureTypes);
        featureTypesDD.add(new OnChangeAjaxBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
                String link=buildWFSLink();
                if (link!=null){
                    url=link;
                    previewLink.modelChanged();
                    previewLink.setEnabled(true);
                    ajaxRequestTarget.add(previewLink);
                }
            }
        });
        featureTypesDD.setOutputMarkupId(true);
        if (hasFeatureType || !hasWorkspace) featureTypesDD.setEnabled(false);
        add(featureTypesDD);
        textArea= new TextArea<>("previewArea",new PropertyModel<>(this,"output"));
        textArea.setOutputMarkupId(true);
        add(textArea);
        previewLink= new ExternalLink("previewLink",new PropertyModel<>(this, "url"));
        previewLink.setOutputMarkupId(true);
        add(previewLink);

    }

    private List<WorkspaceInfo> getWorkspaces(Catalog catalog){
        return catalog.getWorkspaces();
    }

    private List<FeatureTypeInfo> getFeatureTypes(Catalog catalog, WorkspaceInfo ws){
        NamespaceInfo nsi=catalog.getNamespaceByPrefix(ws.getName());
        return catalog.getFeatureTypesByNamespace(nsi);
    }

    private String buildWFSLink (){
        String outputFormat = outputFormatsDropDown.getModelObject();
        boolean canBuildLink=outputFormat!=null && ws!=null && featureType!=null;
        if (canBuildLink){
            TemplateLayerConfig layerConfig=featureType.getMetadata().get(TemplateLayerConfig.METADATA_KEY,TemplateLayerConfig.class);
            TemplateRule rule=new TemplateRule();
            rule.setTemplateIdentifier(templateInfo.getIdentifier());
            rule.setTemplateName(templateInfo.getFullName());
            rule.setOutputFormat(outputFormat);
            if (layerConfig==null){
                layerConfig=new TemplateLayerConfig();
            }
            layerConfig.addTemplateRule(rule);
            featureType.getMetadata().put(TemplateLayerConfig.METADATA_KEY,layerConfig);
            getCatalog().save(featureType);
            Map<String,String> wfsParams=new HashMap<>();
            wfsParams.put("outputFormat",getOutputFormat(outputFormat));
            wfsParams.put("typeNames",ws.getName()+":"+featureType.getName());
            return buildWfsLink(wfsParams);
        }
        return null;
    }

    String buildWfsLink(Map<String, String> wfsParams) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("service", "WFS");
        params.put("version", "2.0.0");
        params.put("request", "GetFeature");
        params.put("typeNames", wfsParams.get("typeNames"));
        params.put("outputFormat", wfsParams.get("outputFormat"));

        return ResponseUtils.buildURL(
                getBaseURL(), getPath("ows", false), params, URLMangler.URLType.SERVICE);
    }

    private String getBaseURL() {
        HttpServletRequest req = GeoServerApplication.get().servletRequest();
        return ResponseUtils.baseURL(req);
    }

    String getPath(String service, boolean useGlobalRef) {
        String ws =this.ws.getName();
        if (ws == null || useGlobalRef) {
            // global reference
            return service;
        } else {
            return ws + "/" + service;
        }
    }

    private String getOutputFormat(String outputFormatName){
        String realOutputFormat=null;
        if (outputFormatName.equals(SupportedMimeType.JSON.name())){
            realOutputFormat=TemplateIdentifier.JSON.getOutputFormat();
        } else if (outputFormatName.equals(SupportedMimeType.JSONLD.name())){
            realOutputFormat=TemplateIdentifier.JSONLD.getOutputFormat();
        } else if (outputFormatName.equals(SupportedMimeType.GML.name())){
            realOutputFormat= TemplateIdentifier.GML32.getOutputFormat();
        }
        return realOutputFormat;
    }
}
