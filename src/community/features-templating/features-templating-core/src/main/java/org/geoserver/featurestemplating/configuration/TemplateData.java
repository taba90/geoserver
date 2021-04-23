package org.geoserver.featurestemplating.configuration;

import java.io.Serializable;

public class TemplateData implements Serializable {

    private String id;

    private String templateName;

    private String templateFileName;

    private String workspace;

    private String featureTypeInfo;

    public TemplateData(){

    }

    public TemplateData(String templateName, String templateFileName, String workspace, String featureTypeInfo){
        this.templateName=templateName;
        this.templateFileName = templateFileName;
        this.workspace=workspace;
        this.featureTypeInfo=featureTypeInfo;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    public void setTemplateFileName(String templateFileName) {
        this.templateFileName = templateFileName;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getFeatureTypeInfo() {
        return featureTypeInfo;
    }

    public void setFeatureTypeInfo(String featureTypeInfo) {
        this.featureTypeInfo = featureTypeInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
