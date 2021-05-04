package org.geoserver.featurestemplating.writers;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

public class JsonWriterTest {

    private SimpleFeature createSimpleFeature() throws URISyntaxException {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.add("geometry", Point.class);
        tb.add("string", String.class);
        tb.add("integer", Integer.class);
        tb.add("double", Double.class);
        tb.add("url", URI.class);
        tb.setName("schema");
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(tb.buildFeatureType());
        GeometryFactory factory = new GeometryFactory();
        Point point = factory.createPoint(new Coordinate(1, 1));
        fb.set("geometry", point);
        fb.set("string", "stringValue");
        fb.set("integer", 1);
        fb.set("double", 0.0);
        fb.set("url", new URI("http://some/url/to.test"));
        return fb.buildFeature("1");
    }

    @Test
    public void testJsonWriterEncodesURL() throws URISyntaxException, IOException {
        // test that values of URL types are correctly encoded
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SimpleFeature f = createSimpleFeature();
        JSONLDWriter writer =
                new JSONLDWriter(new JsonFactory().createGenerator(baos, JsonEncoding.UTF8));
        writer.writeStartObject();
        for (Property prop : f.getProperties()) {
            writer.writeFieldName(prop.getName().toString());
            writer.writeValue(prop.getValue());
        }
        writer.endObject(null);
        writer.close();
        String jsonString = new String(baos.toByteArray());
        JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonString);
        assertEquals(json.getString("url"), "http://some/url/to.test");
    }

    @Test
    public void testStaticArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GeoJSONWriter writer =
                new GeoJSONWriter(new JsonFactory().createGenerator(baos, JsonEncoding.UTF8));
        writer.startArray(null);
        writer.writeStaticContent(null, "abc", Collections.emptyMap());
        writer.writeStaticContent(null, 5, Collections.emptyMap());
        writer.endArray(null);
        writer.close();
        String jsonString = new String(baos.toByteArray());
        JSONArray json = (JSONArray) JSONSerializer.toJSON(jsonString);
        assertEquals("abc", json.get(0));
        assertEquals(Integer.valueOf(5), json.get(1));
    }
}
