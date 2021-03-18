package org.geoserver.wfs.json;

import java.util.List;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.type.ComplexType;

public interface ComplexGeoJsonWriterSettings {

    default boolean areSettingsFor(List<FeatureCollection> features) {
        return true;
    }

    boolean encodeComplexAttributeType();

    boolean encodeNestedFeatureAsProperty(ComplexType featureType);
}
