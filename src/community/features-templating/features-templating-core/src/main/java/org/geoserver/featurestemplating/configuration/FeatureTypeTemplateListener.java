package org.geoserver.featurestemplating.configuration;

import java.util.Optional;
import java.util.Set;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.platform.GeoServerExtensions;

public class FeatureTypeTemplateListener implements TemplateListener {

    private FeatureTypeInfo fti;

    public FeatureTypeTemplateListener(FeatureTypeInfo featureTypeInfo) {
        this.fti = featureTypeInfo;
    }

    @Override
    public void handleDeleteEvent(TemplateInfoEvent removeEvent) {
        TemplateLayerConfig layerConfig =
                fti.getMetadata().get(TemplateLayerConfig.METADATA_KEY, TemplateLayerConfig.class);
        TemplateInfo ti = removeEvent.getSource();
        if (layerConfig != null) {
            Set<TemplateRule> rules = layerConfig.getTemplateRules();
            if (!rules.isEmpty()) {
                if (rules.removeIf(
                        r ->
                                r.getTemplateIdentifier()
                                        .equals(removeEvent.getSource().getIdentifier()))) {
                    fti.getMetadata().put(TemplateLayerConfig.METADATA_KEY, layerConfig);
                    saveFeatureTypeInfo();
                    updateCache(ti);
                }
            }
        }
    }

    @Override
    public void handleUpdateEvent(TemplateInfoEvent updateEvent) {
        TemplateLayerConfig layerConfig =
                fti.getMetadata().get(TemplateLayerConfig.METADATA_KEY, TemplateLayerConfig.class);
        if (layerConfig != null) {
            Set<TemplateRule> rules = layerConfig.getTemplateRules();
            if (!rules.isEmpty()) {
                TemplateInfo info = updateEvent.getSource();
                Optional<TemplateRule> rule =
                        rules.stream()
                                .filter(r -> r.getTemplateIdentifier().equals(info.getIdentifier()))
                                .findFirst();
                if (rule.isPresent()) {
                    TemplateRule r = rule.get();
                    if (!r.getTemplateName().equals(info.getFullName()))
                        r.setTemplateName(info.getFullName());
                    rules.removeIf(tr -> tr.getTemplateIdentifier().equals(info.getIdentifier()));
                    rules.add(r);
                    layerConfig.setTemplateRules(rules);
                    fti.getMetadata().put(TemplateLayerConfig.METADATA_KEY, layerConfig);
                    saveFeatureTypeInfo();
                }
            }
        }
    }

    private void saveFeatureTypeInfo() {
        Catalog catalog = (Catalog) GeoServerExtensions.bean("catalog");
        catalog.save(fti);
    }

    private void updateCache(TemplateInfo info) {
        TemplateLoader loader = GeoServerExtensions.bean(TemplateLoader.class);
        loader.cleanCache(fti, info.getIdentifier());
    }
}
