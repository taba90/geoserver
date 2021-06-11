package org.geoserver.featurestemplating.configuration;

import java.util.Arrays;
import java.util.List;

public enum SupportedFormat {
    JSONLD("JSON-LD"),
    GML("GML"),
    GEOJSON("GeoJSON");

    private String format;

    SupportedFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return this.format;
    }

    public static List<SupportedFormat> getByExtension(String extensions) {
        if (extensions == null) {
            return Arrays.asList(SupportedFormat.values());
        } else if (extensions.equals("xml")) {
            return Arrays.asList(GML);
        } else return Arrays.asList(JSONLD, GEOJSON);
    }
}
