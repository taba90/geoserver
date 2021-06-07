package org.geoserver.featurestemplating.configuration;

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
}
