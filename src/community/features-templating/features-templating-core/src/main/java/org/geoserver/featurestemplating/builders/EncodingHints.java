package org.geoserver.featurestemplating.builders;

import java.util.HashMap;
import java.util.Map;

public class EncodingHints {

    public static final String NAMESPACES = "NAMESPACES";

    public static final String SCHEMA_LOCATION="SCHEMA_LOCATION";

    public static final String ENCODE_AS_ATTRIBUTE = "ENCODE_AS_ATTRIBUTE";

    public static final String ITERATE_KEY = "INTERATE_KEY";

    public static class RootElementAttributes {
        private Map<String, String> namespaces;
        private Map<String, String> schemaLocations;

        public RootElementAttributes() {
            this.namespaces = new HashMap<>();
            this.schemaLocations = new HashMap<>();
        }

        public Map<String, String> getNamespaces() {
            return namespaces;
        }

        public Map<String, String> getSchemaLocations() {
            return schemaLocations;
        }

        public void addNamespace(String prefix, String value) {
            namespaces.put(prefix, value);
        }

        public void addSchemaLocations(String prefix, String value) {
            schemaLocations.put(prefix, value);
        }
    }
}
