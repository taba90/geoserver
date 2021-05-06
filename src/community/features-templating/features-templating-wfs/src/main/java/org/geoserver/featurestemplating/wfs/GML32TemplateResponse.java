package org.geoserver.featurestemplating.wfs;

import static org.geoserver.featurestemplating.builders.EncodingHints.ROOT_ELEMENT_ATTRIBUTES;

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
            Map<String, Object> encodingHints =
                    getRootsEncodingHints(featureCollection, outputFormat);
            Object rootAttributes = encodingHints.get(ROOT_ELEMENT_ATTRIBUTES);
            if (rootAttributes != null) {
                EncodingHints.RootElementAttributes attributes =
                        (EncodingHints.RootElementAttributes) rootAttributes;
                writer.setNamespaces(attributes.getNamespaces());
            }
            writer.startTemplateOutput(encodingHints);
            iterateFeatureCollection(writer, featureCollection, getFeature);
            writer.endTemplateOutput(encodingHints);
            ;
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

    private Map<String, Object> getRootsEncodingHints(
            FeatureCollectionResponse response, String outputFormat) throws ExecutionException {
        EncodingHints.RootElementAttributes attributes=null;
        List<FeatureCollection> collectionList = response.getFeature();
        for (FeatureCollection collection : collectionList) {
            FeatureTypeInfo fti = helper.getFeatureTypeInfo(collection);
            RootBuilder root = configuration.getTemplate(fti, outputFormat);
            EncodingHints.RootElementAttributes rattrs=(EncodingHints.RootElementAttributes)root.getEncodingHints().get(ROOT_ELEMENT_ATTRIBUTES);
            if (attributes==null)
                attributes= rattrs;
        }
        return encodingHints;
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
        return super.getMimeType(value, operation);
    }
}
