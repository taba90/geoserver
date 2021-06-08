/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.readers;

import java.io.IOException;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.geoserver.platform.resource.Resource;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * This class create a TemplateBuilder tree from an XML document. The XMLParser being used is not
 * namespaces aware.
 */
public class XMLTemplateReader implements TemplateReader {

    private RootBuilder rootBuilder;

    public XMLTemplateReader(Resource resource, NamespaceSupport namespaceSupport)
            throws IOException {
        this.rootBuilder = new RootBuilder();
        try (XMLRecursiveReader recursiveParser =
                new XMLRecursiveReader(resource, namespaceSupport)) {
            recursiveParser.iterateReader(rootBuilder);
        }
    }

    @Override
    public RootBuilder getRootBuilder() {
        return rootBuilder;
    }
}
