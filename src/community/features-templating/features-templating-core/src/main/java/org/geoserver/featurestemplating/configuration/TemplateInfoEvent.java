package org.geoserver.featurestemplating.configuration;

public class TemplateInfoEvent {

    private TemplateInfo ti;

    public TemplateInfoEvent(TemplateInfo templateInfo) {
        this.ti = templateInfo;
    }

    public TemplateInfo getSource() {
        return ti;
    }
}
