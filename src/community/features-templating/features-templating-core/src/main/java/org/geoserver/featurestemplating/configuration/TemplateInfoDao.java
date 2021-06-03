package org.geoserver.featurestemplating.configuration;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.platform.GeoServerExtensions;

import java.util.List;

public interface TemplateInfoDao {

    static TemplateInfoDaoImpl get() {
        return GeoServerExtensions.bean(TemplateInfoDaoImpl.class);
    }

    public static final String TEMPLATE_DIR = "features-templating";

    public List<TemplateInfo> findAll();

    public List<TemplateInfo> findByFeatureTypeInfo(
            FeatureTypeInfo featureTypeInfo);

    public TemplateInfo findById(String id);

    public TemplateInfo saveOrUpdate(TemplateInfo templateData);

    public void deleteAll(List<TemplateInfo> templateInfos);

    public void delete(TemplateInfo templateData);


    public void fireTemplateUpdateEvent(TemplateInfo templateInfo);

    public void fireTemplateInfoRemoveEvent(TemplateInfo templateInfo);

    public void addTemplateListener(TemplateListener listener);
}
