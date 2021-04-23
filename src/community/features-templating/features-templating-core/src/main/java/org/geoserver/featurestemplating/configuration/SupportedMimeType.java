package org.geoserver.featurestemplating.configuration;

public enum SupportedMimeType {
    JSON("application/json"),
    GEOJSON("application/geo+json"),
    HTML("text/html");

    private String mimeType;

    SupportedMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}
