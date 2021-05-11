/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.readers;

import org.geoserver.featurestemplating.builders.TemplateBuilderMaker;
import org.xml.sax.helpers.NamespaceSupport;

/** Provides a configuration to setup a {@link JSONTemplateReader} */
public class TemplateReaderConfiguration {

    private NamespaceSupport namespaces;

    public TemplateReaderConfiguration(NamespaceSupport namespaces) {
        this.namespaces = namespaces;
    }

    public NamespaceSupport getNamespaces() {
        return namespaces;
    }

    public TemplateBuilderMaker getBuilderMaker(String rootCollectionName) {
        return new TemplateBuilderMaker(rootCollectionName);
    }

    public TemplateBuilderMaker getBuilderMaker() {
        return new TemplateBuilderMaker("features");
    }
}
