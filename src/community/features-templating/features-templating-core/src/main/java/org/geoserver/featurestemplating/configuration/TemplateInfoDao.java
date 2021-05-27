package org.geoserver.featurestemplating.configuration;

import org.opengis.feature.type.Name;

import java.util.List;

public interface TemplateInfoDao {

    public static final String TEMPLATE_DIR = "features-templating";

    public List<TemplateInfo> findAll();

    public List<TemplateInfo> findByWorkspaceAndFeatureTypeInfo(String workspace, String featureTypeInfo);

    public TemplateInfo findByName(String templateName);

    public TemplateInfo findById(String id);

    public TemplateInfo saveOrUpdate(TemplateInfo templateData);

    public void deleteAll(List<TemplateInfo> templateInfos);

    public void delete(TemplateInfo templateData);

    public void delete(String templateName);

    public boolean templateDataExists(String templateName);

    public void fireTemplateInfoRemoveEvent(TemplateInfo templateInfo);

    public void addTemplateListener(TemplateListener listener);
}
