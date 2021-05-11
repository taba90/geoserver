/* (c) 2020 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.featurestemplating.writers;

import static org.geoserver.featurestemplating.builders.EncodingHints.CONTEXT;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import org.geotools.filter.function.FilterFunction_toWKT;

/** Implements its superclass methods to write a valid json-ld output */
public class JSONLDWriter extends CommonJSONWriter {

    public JSONLDWriter(JsonGenerator generator) {
        super(generator);
    }

    @Override
    public void writeValue(Object value) throws IOException {
        writeString(String.valueOf(value));
    }

    @Override
    public void writeGeometry(Object value) throws IOException {
        FilterFunction_toWKT toWKT = new FilterFunction_toWKT();
        String wkt = (String) toWKT.evaluate(value);
        writeString(wkt);
    }

    @Override
    public void startTemplateOutput(Map<String, Object> encodingHints) throws IOException {
        writeStartObject();
        String contextName = "@context";
        JsonNode context = (JsonNode) encodingHints.get(CONTEXT);
        if (context.isArray()) writeArrayNode(contextName, context);
        else if (context.isObject()) writeObjectNode(contextName, context);
        else writeValueNode(contextName, context);
        writeFieldName("type");
        writeString("FeatureCollection");
        writeFieldName("features");
        writeStartArray();
    }
}
