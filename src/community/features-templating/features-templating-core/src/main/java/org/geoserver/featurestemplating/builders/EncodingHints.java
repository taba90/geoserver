/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.builders;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.HashMap;
import java.util.Optional;

/**
 * This class represent a Map of encoding hints. An encoding hint is a value giving additional
 * instructions to the writing process of a template output. The class also defines the constants
 * for the currently available EncodingHints.
 */
public class EncodingHints extends HashMap<String, Object> {

    public static final String CONTEXT = "@context";

    public static final String NAMESPACES = "NAMESPACES";

    public static final String SCHEMA_LOCATION = "SCHEMA_LOCATION";

    public static final String ENCODE_AS_ATTRIBUTE = "ENCODE_AS_ATTRIBUTE";

    public static final String ITERATE_KEY = "INTERATE_KEY";

    public static final String CHILDREN_EVALUATION = "CHILDREN_EVALUATION";

    public static final String IS_SINGLE_FEATURE="IS_SINGLE_FEATURE";

    public static final String SKIP_IF_SINGLE_FEATURE="SKIP_IF_SINGLE_FEATURE";

    /**
     * Check if the hint is present.
     *
     * @param hint the name of the hint.
     * @return true if present false otherwise.
     */
    public boolean hasHint(String hint) {
        return get(hint) != null;
    }

    /**
     * Get the hint value with the requested type.
     *
     * @param key the hint name.
     * @param cast the type requested.
     * @return the value of the hint if found, otherwise null.
     */
    public <T> T get(String key, Class<T> cast) {
        return cast.cast(get(key));
    }

    public static boolean isSingleFeatureRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .map(
                        att ->
                                (String)
                                        att.getAttribute(
                                                "OGCFeatures:ItemId",
                                                RequestAttributes.SCOPE_REQUEST)).isPresent();
    }
}
