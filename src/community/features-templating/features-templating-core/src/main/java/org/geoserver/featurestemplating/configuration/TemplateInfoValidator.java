package org.geoserver.featurestemplating.configuration;

import org.geoserver.featurestemplating.web.TemplateConfigurationException;

public class TemplateInfoValidator {
    TemplateInfo info;
    String rawTemplate;

    public TemplateInfoValidator(TemplateInfo info) {
        this.info = info;
    }

    public TemplateInfoValidator(TemplateInfo info, String rawTemplate) {
        this(info);
        this.rawTemplate = rawTemplate;
    }

    public void validate() throws TemplateConfigurationException {
        String templateName = info.getTemplateName();
        TemplateConfigurationException e = null;
        if (templateName == null || templateName.equals("")) {
            e = new TemplateConfigurationException();
            e.setId(TemplateConfigurationException.MISSING_TEMPLATE_NAME);
        } else if (info.getExtension() == null) {
            e = new TemplateConfigurationException();
            e.setId(TemplateConfigurationException.MISSING_FILE_EXTENSION);
        } else if (rawTemplate == null || rawTemplate.equals("")) {
            e = new TemplateConfigurationException();
            e.setId(TemplateConfigurationException.MISSING_TEMPLATE_CONTENT);
        }
        if (e != null) throw e;
    }
}
