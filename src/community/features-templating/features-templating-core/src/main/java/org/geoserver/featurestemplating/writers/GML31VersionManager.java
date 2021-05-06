package org.geoserver.featurestemplating.writers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;

public class GML31VersionManager extends GMLVersionManager {

    public GML31VersionManager(XMLStreamWriter streamWriter) {
        super(streamWriter, "posList", "exterior", "interior");
    }

    @Override
    public void writeNumberReturned(String numberReturned) throws XMLStreamException {
        streamWriter.writeAttribute("numberOfFeature", numberReturned);
    }

    @Override
    public void writeNumberMatched(String numberMatched) throws XMLStreamException {}

    @Override
    public void writeBoundingBox(ReferencedEnvelope envelope) throws XMLStreamException {}

    @Override
    public void startTemplateOutput() throws XMLStreamException {
        streamWriter.writeStartElement("gml", "featureMembers", "http://www.opengis.net/gml/3.2");
    }

    @Override
    public void endTemplateOutput() throws XMLStreamException {
        streamWriter.writeEndElement();
    }
}
