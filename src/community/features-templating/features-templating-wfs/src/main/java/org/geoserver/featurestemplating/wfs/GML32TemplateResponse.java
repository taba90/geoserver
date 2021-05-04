package org.geoserver.featurestemplating.wfs;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.featurestemplating.builders.geojson.GeoJSONRootBuilder;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.geoserver.featurestemplating.configuration.TemplateConfiguration;
import org.geoserver.featurestemplating.configuration.TemplateIdentifier;
import org.geoserver.featurestemplating.writers.GMLTemplateWriter;
import org.geoserver.featurestemplating.writers.GeoJSONWriter;
import org.geoserver.featurestemplating.writers.TemplateOutputWriter;
import org.geoserver.platform.Operation;
import org.geoserver.platform.ServiceException;
import org.geoserver.wfs.request.FeatureCollectionResponse;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GML32TemplateResponse extends BaseTemplateGetFeatureResponse {

    public GML32TemplateResponse(
            GeoServer gs, TemplateConfiguration configuration, TemplateIdentifier identifier) {
        super(gs, configuration, identifier);
    }

    @Override
    protected void beforeFeatureIteration(
            TemplateOutputWriter writer, RootBuilder root, FeatureTypeInfo typeInfo) {
        GeoJSONRootBuilder rb = (GeoJSONRootBuilder) root;
        GeoJSONWriter jsonWriter = (GeoJSONWriter) writer;
        String strFlatOutput =
                rb.getVendorOption(RootBuilder.VendorOption.FLAT_OUTPUT.getVendorOptionName());
        boolean flatOutput = strFlatOutput != null ? Boolean.valueOf(strFlatOutput) : false;
        jsonWriter.setFlatOutput(flatOutput);
    }

    @Override
    protected void write(
            FeatureCollectionResponse featureCollection, OutputStream output, Operation getFeature)
            throws ServiceException {
        try (GMLTemplateWriter writer = getOutputWriter(output)) {
            Map<String,Object> encodingHints=getRootsEncodingHints(featureCollection);
            writer.startTemplateOutput(encodingHints);
            iterateFeatureCollection(writer, featureCollection);
            writer.endTemplateOutput(encodingHints);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    protected void beforeEvaluation(
            TemplateOutputWriter writer, RootBuilder root, Feature feature) {}

    protected GMLTemplateWriter getOutputWriter(OutputStream output) throws IOException {
        return (GMLTemplateWriter) helper.getOutputWriter(output);
    }

    private Map<String,Object> getRootsEncodingHints(FeatureCollectionResponse response) throws ExecutionException {
        Map<String,Object> encodingHints=new HashMap<>();
        List<FeatureCollection> collectionList=response.getFeature();
        for (FeatureCollection collection:collectionList){
            FeatureTypeInfo fti = helper.getFeatureTypeInfo(collection);
            RootBuilder root = configuration.getTemplate(fti, getMimeType(null, null));
            encodingHints.putAll(root.getEncodingHints());
        }
        return encodingHints;
    }
}
