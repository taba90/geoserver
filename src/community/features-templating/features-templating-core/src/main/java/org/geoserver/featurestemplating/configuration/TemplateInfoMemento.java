/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.configuration;

/**
 * Implementation of AbstractFeatureTemplateInfo used to keep the a reference to the
 * previous state of a TemplateInfo.
 */
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
