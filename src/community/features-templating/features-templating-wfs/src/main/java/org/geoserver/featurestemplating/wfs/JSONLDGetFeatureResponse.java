/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.wfs;

import static org.geoserver.featurestemplating.builders.EncodingHints.CONTEXT;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.*;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.featurestemplating.builders.EncodingHints;
import org.geoserver.featurestemplating.builders.TemplateBuilder;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.geoserver.featurestemplating.configuration.TemplateIdentifier;
import org.geoserver.featurestemplating.configuration.TemplateLoader;
import org.geoserver.featurestemplating.validation.JSONLDContextValidation;
import org.geoserver.featurestemplating.writers.JSONLDWriter;
import org.geoserver.featurestemplating.writers.TemplateOutputWriter;
import org.geoserver.ows.Dispatcher;
import org.geoserver.ows.Request;
import org.geoserver.platform.Operation;
import org.geoserver.platform.ServiceException;
import org.geoserver.wfs.request.FeatureCollectionResponse;
import org.geoserver.wfs.request.GetFeatureRequest;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;

/**
 * Encodes features in json-ld output format by means of a ${@link TemplateBuilder} tree obtained by
 * a JSON-LD template
 */
public class JSONLDGetFeatureResponse extends BaseTemplateGetFeatureResponse {

    private TemplateLoader configuration;

    public JSONLDGetFeatureResponse(GeoServer gs, TemplateLoader configuration) {
        super(gs, configuration, TemplateIdentifier.JSONLD);
        this.configuration = configuration;
    }

    @Override
    protected void write(
            FeatureCollectionResponse featureCollection, OutputStream output, Operation getFeature)
            throws ServiceException {
        //  Multiple FeatureType encoding for json-ld has not be implemented.
        //  Is missed a strategy for multiple context in template. Probably we should merge them
        // before
        //  writing the context to the output.
        //  This is thus working only for one featureType and so the RootBuilder is being got before
        // iteration.

        JSONLDContextValidation validator = null;
        try {
            FeatureTypeInfo info =
                    helper.getFirstFeatureTypeInfo(
                            GetFeatureRequest.adapt(getFeature.getParameters()[0]));
            RootBuilder root =
                    configuration.getTemplate(info, TemplateIdentifier.JSONLD.getOutputFormat());
            boolean validate = isSematincValidation();
            // setting it back to false
            if (validate) {
                validate(featureCollection, root);
            }
            try (JSONLDWriter writer = (JSONLDWriter) helper.getOutputWriter(output)) {
                write(featureCollection, root, writer);
            } catch (Exception e) {
                throw new ServiceException(e);
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        } finally {
            if (validator != null) {
                validator.validate();
            }
        }
    }

    private void validate(FeatureCollectionResponse featureCollection, RootBuilder root)
            throws IOException {
        JSONLDContextValidation validator = new JSONLDContextValidation();
        try (JSONLDWriter writer =
                (JSONLDWriter) helper.getOutputWriter(new FileOutputStream(validator.init()))) {
            write(featureCollection, root, writer);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        validator.validate();
    }

    private void write(
            FeatureCollectionResponse featureCollection, RootBuilder root, JSONLDWriter writer)
            throws IOException {
        EncodingHints encondingHints = new EncodingHints(root.getEncodingHints());
        if (encondingHints.get(CONTEXT) == null) {
            JsonNode context = root.getVendorOptions().get(CONTEXT, JsonNode.class);
            if (context != null) encondingHints.put(CONTEXT, context);
        }
        writer.startTemplateOutput(encondingHints);
        iterateFeatureCollection(writer, featureCollection, root);
        writer.endTemplateOutput(encondingHints);
    }

    @Override
    protected void beforeFeatureIteration(
            TemplateOutputWriter writer, RootBuilder root, FeatureTypeInfo typeInfo) {}

    protected void iterateFeatureCollection(
            TemplateOutputWriter writer,
            FeatureCollectionResponse featureCollection,
            RootBuilder root)
            throws IOException {
        List<FeatureCollection> collectionList = featureCollection.getFeature();

        for (FeatureCollection collection : collectionList) {
            iterateFeatures(root, writer, collection);
        }
    }

    @Override
    protected void beforeEvaluation(
            TemplateOutputWriter writer, RootBuilder root, Feature feature) {}

    @Override
    protected void writeAdditionalFieldsInternal(
            TemplateOutputWriter writer,
            FeatureCollectionResponse featureCollection,
            Operation getFeature,
            BigInteger featureCount,
            ReferencedEnvelope bounds)
            throws IOException {
        // do nothing
    }

    @Override
    public String getMimeType(Object value, Operation operation) throws ServiceException {
        return TemplateIdentifier.JSONLD.getOutputFormat();
    }

    private boolean isSematincValidation() {
        Request request = Dispatcher.REQUEST.get();
        Map rawKvp = request.getRawKvp();
        Object value = rawKvp != null ? rawKvp.get("validation") : null;
        boolean result = false;
        if (value != null) {
            result = Boolean.valueOf(value.toString());
        }
        return result;
    }
}
