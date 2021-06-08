package org.geoserver.featurestemplating.web;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.web.publish.LayerEditTabPanelInfo;

public class FeatureTypeTabPanelInfo extends LayerEditTabPanelInfo {
    @Override
    public boolean supports(PublishedInfo pi) {
        boolean result = false;
        if (super.supports(pi)) {
            LayerInfo layerInfo = (LayerInfo) pi;
            if (layerInfo.getResource() instanceof FeatureTypeInfo) result = true;
        }
        return result;
    }
}
