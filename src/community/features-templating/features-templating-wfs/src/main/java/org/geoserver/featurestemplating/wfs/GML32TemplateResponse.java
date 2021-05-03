package org.geoserver.featurestemplating.wfs;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.geoserver.featurestemplating.configuration.TemplateConfiguration;
import org.geoserver.featurestemplating.configuration.TemplateIdentifier;
import org.geoserver.featurestemplating.writers.TemplateOutputWriter;
import org.opengis.feature.Feature;

public class GML32TemplateResponse extends BaseTemplateGetFeatureResponse{


    public GML32TemplateResponse(GeoServer gs, TemplateConfiguration configuration, TemplateIdentifier identifier) {
        super(gs, configuration, identifier);
    }

    @Override
    protected void beforeFeatureIteration(TemplateOutputWriter writer, RootBuilder root, FeatureTypeInfo typeInfo) {

    }

    @Override
    protected void beforeEvaluation(TemplateOutputWriter writer, RootBuilder root, Feature feature) {

    }
}
