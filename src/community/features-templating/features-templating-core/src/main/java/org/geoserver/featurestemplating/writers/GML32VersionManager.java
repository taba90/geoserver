package org.geoserver.featurestemplating.writers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;

public class GML32VersionManager extends GMLVersionManager {

    public GML32VersionManager(XMLStreamWriter streamWriter) {
        super(streamWriter, "posList", "exterior", "interior");
    }

    @Override
    public void writeNumberReturned(String numberReturned) throws XMLStreamException {
        streamWriter.writeAttribute("numberReturned", String.valueOf(numberReturned));
    }

    @Override
    public void writeNumberMatched(String numberMatched) throws XMLStreamException {
        streamWriter.writeAttribute("numberReturned", String.valueOf(numberMatched));
    }

    @Override
    public void writeBoundingBox(ReferencedEnvelope envelope) {}
}
