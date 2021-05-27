package org.geoserver.featurestemplating.configuration;

public class TemplateInfoRemoveEvent {

    private TemplateInfo ti;

    public TemplateInfoRemoveEvent(TemplateInfo templateInfo){
        this.ti=templateInfo;

    }
    public TemplateInfo getSource(){
        return ti;
    }
}
