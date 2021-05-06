package org.geoserver.featurestemplating.writers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;

public class GML2VersionManager extends GMLVersionManager {

    public GML2VersionManager(XMLStreamWriter streamWriter) {
        super(streamWriter, "coordinates", "outerBoundaryIs", "innerBoundaryIs");
    }

    @Override
    public void writeNumberReturned(String numberReturned) throws XMLStreamException {}

    @Override
    public void writeNumberMatched(String numberMatched) throws XMLStreamException {}

    @Override
    public void writeBoundingBox(ReferencedEnvelope envelope) throws XMLStreamException {}
}
