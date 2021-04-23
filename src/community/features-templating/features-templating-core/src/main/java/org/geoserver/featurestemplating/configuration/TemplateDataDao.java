package org.geoserver.featurestemplating.configuration;

import java.util.List;

public interface TemplateDataDao {

    public static final String TEMPLATE_DIR="features-templating";
    public List<TemplateData> findAll();
    public TemplateData findByName(String templateName);
    public TemplateData saveOrUpdate (TemplateData templateData);
    public void delete (TemplateData templateData);
    public void delete (String templateName);
    public boolean templateDataExists(String templateName);
}
