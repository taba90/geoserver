package org.geoserver.featurestemplating.wfs;

import static org.geoserver.featurestemplating.builders.EncodingHints.NAMESPACES;
import static org.geoserver.featurestemplating.builders.EncodingHints.SCHEMA_LOCATION;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import net.opengis.wfs.GetFeatureType;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.featurestemplating.builders.EncodingHints;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.geoserver.featurestemplating.configuration.TemplateConfiguration;
import org.geoserver.featurestemplating.configuration.TemplateIdentifier;
import org.geoserver.featurestemplating.writers.GMLTemplateWriter;
import org.geoserver.featurestemplating.writers.TemplateOutputWriter;
import org.geoserver.featurestemplating.writers.XMLTemplateWriter;
import org.geoserver.platform.Operation;
import org.geoserver.platform.ServiceException;
import org.geoserver.wfs.request.FeatureCollectionResponse;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;

public class GML32TemplateResponse extends BaseTemplateGetFeatureResponse {

    public GML32TemplateResponse(
            GeoServer gs, TemplateConfiguration configuration, TemplateIdentifier identifier) {
        super(gs, configuration, identifier);
    }

    @Override
    protected void beforeFeatureIteration(
            TemplateOutputWriter writer, RootBuilder root, FeatureTypeInfo typeInfo) {}

    @Override
    protected void write(
            FeatureCollectionResponse featureCollection, OutputStream output, Operation getFeature)
            throws ServiceException {
        String outputFormat = getMimeType(null, getFeature);
        try (GMLTemplateWriter writer = getOutputWriter(output, outputFormat)) {
            setNamespacesAndSchemaLocations(featureCollection, writer, outputFormat);
            writer.startTemplateOutput(null);
            iterateFeatureCollection(writer, featureCollection, getFeature);
            writer.endTemplateOutput(null);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    protected void beforeEvaluation(
            TemplateOutputWriter writer, RootBuilder root, Feature feature) {}

    protected GMLTemplateWriter getOutputWriter(OutputStream output, String outputFormat)
            throws IOException {
        return (GMLTemplateWriter) helper.getOutputWriter(output, outputFormat);
    }

    private void setNamespacesAndSchemaLocations(
            FeatureCollectionResponse response, XMLTemplateWriter writer, String outputFormat) throws ExecutionException {
        List<FeatureCollection> collectionList = response.getFeature();
        for (FeatureCollection collection : collectionList) {
            FeatureTypeInfo fti = helper.getFeatureTypeInfo(collection);
            RootBuilder root = configuration.getTemplate(fti, outputFormat);
            Map<String, String> namespaces =(Map<String,String>) root.getEncodingHints().get(NAMESPACES);
            Map<String, String> schemaLocation= (Map<String,String>) root.getEncodingHints().get(SCHEMA_LOCATION);
            if (namespaces!=null)
                writer.addNamespaces(namespaces);
            if (schemaLocation!=null)
                writer.addSchemaLocations(schemaLocation);
        }
    }

    @Override
    public String getMimeType(Object value, Operation operation) throws ServiceException {
        if (operation != null) {
            Object[] parameters = operation.getParameters();
            if (parameters.length > 0) {
                Object param = parameters[0];
                if (param instanceof GetFeatureType) {
                    return ((GetFeatureType) param).getOutputFormat();
                }
            }
        }
        return TemplateIdentifier.GML32.getOutputFormat();
    }
}
