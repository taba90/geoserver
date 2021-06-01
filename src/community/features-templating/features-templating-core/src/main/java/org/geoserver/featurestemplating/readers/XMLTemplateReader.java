/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.readers;

import java.io.IOException;
import java.util.List;
import java.util.Stack;
import javax.xml.stream.events.StartElement;
import org.geoserver.featurestemplating.builders.TemplateBuilderMaker;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.geoserver.platform.resource.Resource;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * This class create a TemplateBuilder tree from an XML document. The XMLParser being used is not
 * namespaces aware.
 */
public class XMLTemplateReader implements TemplateReader {

    private RootBuilder rootBuilder;
    private TemplateBuilderMaker maker;
    private Stack<StartElement> elementsStack;
    private NamespaceSupport namespaceSupport;
    private List<StartElement> parsedElements;

    private static final String COLLECTION_ATTR = "gft:isCollection";

    private static final String FILTER_ATTR = "gft:filter";

    private static final String SOURCE_ATTR = "gft:source";

    private static final String FEATURE_COLL_ELEMENT = "wfs:FeatureCollection";

    private static final String NAMESPACE_PREFIX = "xmlns";

    private static final String SCHEMA_LOCATION_ATTR = "xsi:schemaLocation";

    private static final String INCLUDE_FLAT = "gft:includeFlat";

    private static final String INCLUDE = "$include";

    public XMLTemplateReader(Resource resource, NamespaceSupport namespaceSupport)
            throws IOException {
        this.rootBuilder = new RootBuilder();
        try (XMLRecursiveParser recursiveParser =
                new XMLRecursiveParser(resource, namespaceSupport)) {
            recursiveParser.iterateReader(rootBuilder);
        }
    }

    @Override
    public RootBuilder getRootBuilder() {
        return rootBuilder;
    }
}
