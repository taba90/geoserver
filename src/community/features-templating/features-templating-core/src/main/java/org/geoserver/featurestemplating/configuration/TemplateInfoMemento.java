package org.geoserver.featurestemplating.configuration;

public class TemplateInfoMemento extends AbstractFeatureTemplateInfo {

    public TemplateInfoMemento(TemplateInfo ti){
        super(ti.getTemplateName(),ti.getWorkspace(),ti.getFeatureType());
    }
}
