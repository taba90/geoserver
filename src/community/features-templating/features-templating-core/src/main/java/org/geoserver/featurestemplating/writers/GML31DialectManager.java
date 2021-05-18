/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.featurestemplating.writers;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

class GML31DialectManager extends GMLDialectManager {

    GML31DialectManager(XMLStreamWriter streamWriter) {
        super(streamWriter, "posList");
    }

    @Override
    void writeNumberReturned(String numberReturned) throws XMLStreamException {
        streamWriter.writeAttribute("numberOfFeature", numberReturned);
    }

    @Override
    void writeNumberMatched(String numberMatched) throws XMLStreamException {}

    @Override
    void writeBoundingBox(ReferencedEnvelope envelope, CoordinateReferenceSystem crs)
            throws IOException {
        super.writeBoundingBox(envelope, crs, false);
    }

    @Override
    String getWfsNsUri() {
        return "http://www.opengis.net/wfs";
    }

    @Override
    String getGmlNsUri() {
        return "http://www.opengis.net/gml";
    }
}
