package org.geoserver.wfs.json;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;
import org.geotools.data.DataStoreFinder;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.ComplexType;

public class DefaultComplexGeoJsonWriterSettings implements ComplexGeoJsonWriterSettings {

    static final Logger LOGGER = Logging.getLogger(DefaultComplexGeoJsonWriterSettings.class);

    private static Class NON_FEATURE_TYPE_PROXY;

    static {
        try {
            NON_FEATURE_TYPE_PROXY =
                    Class.forName("org.geotools.data.complex.config.NonFeatureTypeProxy");
        } catch (ClassNotFoundException e) {
            // might be ok if the app-schema datastore is not around
            if (StreamSupport.stream(
                            Spliterators.spliteratorUnknownSize(
                                    DataStoreFinder.getAllDataStores(), Spliterator.ORDERED),
                            false)
                    .anyMatch(
                            f ->
                                    f != null
                                            && f.getClass()
                                                    .getSimpleName()
                                                    .equals("AppSchemaDataAccessFactory"))) {
                LOGGER.log(
                        Level.FINE,
                        "Could not find NonFeatureTypeProxy yet App-schema is around, probably the class changed name, package or does not exist anymore",
                        e);
            }
            NON_FEATURE_TYPE_PROXY = null;
        }
    }

    @Override
    public boolean encodeComplexAttributeType() {
        return true;
    }

    @Override
    public boolean encodeNestedFeatureAsProperty(ComplexType featureType) {
        return NON_FEATURE_TYPE_PROXY != null && NON_FEATURE_TYPE_PROXY.isInstance(featureType);
    }
}
