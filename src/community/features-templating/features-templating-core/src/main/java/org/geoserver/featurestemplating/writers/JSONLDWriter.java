/* (c) 2020 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.featurestemplating.writers;

import static org.geoserver.featurestemplating.builders.EncodingHints.CONTEXT;
import static org.geoserver.featurestemplating.builders.EncodingHints.isSingleFeatureRequest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.math.BigInteger;
import org.geoserver.featurestemplating.builders.EncodingHints;
import org.geoserver.featurestemplating.configuration.TemplateIdentifier;
import org.geotools.filter.function.FilterFunction_toWKT;
import org.geotools.geometry.jts.ReferencedEnvelope;

/** Implements its superclass methods to write a valid json-ld output */
public class JSONLDWriter extends CommonJSONWriter {

    public JSONLDWriter(JsonGenerator generator) {
        super(generator, TemplateIdentifier.JSONLD);
    }

    @Override
    public void writeValue(Object value) throws IOException {
        generator.writeString(String.valueOf(value));
    }

    @Override
    public void writeGeometry(Object value) throws IOException {
        FilterFunction_toWKT toWKT = new FilterFunction_toWKT();
        String wkt = (String) toWKT.evaluate(value);
        generator.writeString(wkt);
    }

    @Override
    public void startTemplateOutput(EncodingHints encodingHints) throws IOException {
        writeStartObject();
        String contextName = "@context";
        JsonNode context = getEncodingHintIfPresent(encodingHints, CONTEXT, JsonNode.class);
        if (context != null) {
            if (context.isArray()) writeArrayNode(contextName, context);
            else if (context.isObject()) writeObjectNode(contextName, context);
            else writeValueNode(contextName, context);
        }
        if (!isSingleFeatureRequest()) {
            generator.writeFieldName("type");
            generator.writeString("FeatureCollection");
            generator.writeFieldName("features");
            writeStartArray();
        }
    }

    @Override
    public void writeCollectionCounts(BigInteger featureCount) throws IOException {
        // do nothing
    }

    @Override
    public void writeCrs() throws IOException {
        // do nothing
    }

    @Override
    public void writeCollectionBounds(ReferencedEnvelope bounds) throws IOException {
        // do nothing
    }

    @Override
    public void writeTimeStamp() throws IOException {
        // do nothing
    }

    @Override
    public void writeNumberReturned() throws IOException {
        // do nothing
    }

    @Override
    public void startObject(String name, EncodingHints encodingHints) throws IOException {
        if (!skipObjectWriting(encodingHints)) super.startObject(name, encodingHints);
    }

    @Override
    public void endObject(String name, EncodingHints encodingHints) throws IOException {
        if (!skipObjectWriting(encodingHints)) super.endObject(name, encodingHints);
    }
}
