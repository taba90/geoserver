package org.geoserver.featurestemplating.configuration;

import java.io.Serializable;

public class TemplateEntry implements Serializable {

    private String templateName;
    private TemplateRule rule;
    private String mimeType;
    private String workspace;
    private String layerName;
    private String service;
    private String operation;

    public TemplateEntry() {}

    public TemplateEntry(
            String templateName, TemplateRule rule, String mimeType, String workspace, String layerName,String service, String operation) {
        this.templateName = templateName;
        this.rule = rule;
        this.mimeType = mimeType;
        this.service=service;
        this.operation=operation;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public TemplateRule getRule() {
        return rule;
    }

    public void setRule(TemplateRule rule) {
        this.rule = rule;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
