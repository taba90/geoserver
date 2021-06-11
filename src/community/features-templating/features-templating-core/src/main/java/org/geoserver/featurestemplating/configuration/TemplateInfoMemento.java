package org.geoserver.featurestemplating.configuration;

public class TemplateInfoMemento extends AbstractFeatureTemplateInfo {

    private String rawTemplate;

    public TemplateInfoMemento(TemplateInfo ti) {
        super(ti.getTemplateName(), ti.getWorkspace(), ti.getFeatureType(), ti.getExtension());
    }

    public TemplateInfoMemento(TemplateInfo ti, String rawTemplate) {
        this(ti);
        this.rawTemplate = rawTemplate;
    }

    public String getRawTemplate() {
        return rawTemplate;
    }

    public void setRawTemplate(String rawTemplate) {
        this.rawTemplate = rawTemplate;
    }
}
