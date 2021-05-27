package org.geoserver.featurestemplating.configuration;

import java.util.List;

public interface TemplateInfoDao {

    public static final String TEMPLATE_DIR="features-templating";
    public List<TemplateInfo> findAll();
    public TemplateInfo findByName(String templateName);
    public TemplateInfo saveOrUpdate(TemplateInfo templateData);
    public void deleteAll(List<TemplateInfo> templateInfos);
    public void delete(TemplateInfo templateData);
    public void delete(String templateName);
    public boolean templateDataExists(String templateName);
}
