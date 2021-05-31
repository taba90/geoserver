package org.geoserver.featurestemplating.configuration;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.platform.GeoServerExtensions;

import java.util.List;
import java.util.Set;

public class FeatureTypeTemplateListener implements TemplateListener{

    private FeatureTypeInfo fti;
    public FeatureTypeTemplateListener(FeatureTypeInfo featureTypeInfo){
        this.fti=featureTypeInfo;
    }
    @Override
    public void handleDeleteEvent(TemplateInfoRemoveEvent removeEvent) {
        TemplateLayerConfig layerConfig=fti.getMetadata().get(TemplateLayerConfig.METADATA_KEY,TemplateLayerConfig.class);
        if (layerConfig!=null) {
            Set<TemplateRule> rules=layerConfig.getTemplateRules();
            if (!rules.isEmpty()){
                if(rules.removeIf(r->r.getTemplateIdentifier().equals(removeEvent.getSource().getIdentifier()))) {
                    fti.getMetadata().put(TemplateLayerConfig.METADATA_KEY,layerConfig);
                    Catalog catalog = (Catalog) GeoServerExtensions.bean("catalog");
                    catalog.save(fti);
                }

            }
        }
    }
}
