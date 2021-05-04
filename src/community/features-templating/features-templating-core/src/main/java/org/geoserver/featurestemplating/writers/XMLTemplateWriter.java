package org.geoserver.featurestemplating.writers;

import static org.geoserver.featurestemplating.builders.EncodingHints.ENCODE_AS_ATTRIBUTE;
import static org.geoserver.featurestemplating.builders.EncodingHints.ROOT_ELEMENT_ATTRIBUTES;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.geoserver.featurestemplating.readers.XMLTemplateReader;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;

public abstract class XMLTemplateWriter implements TemplateOutputWriter {

    protected XMLStreamWriter streamWriter;

    protected CRS.AxisOrder axisOrder = CRS.AxisOrder.NORTH_EAST;

    Map<String, String> namespaces;

    public XMLTemplateWriter(XMLStreamWriter streamWriter, Map<String, String> namespaces) {
        this.streamWriter = streamWriter;
        this.namespaces = namespaces;
    }

    @Override
    public void writeElementName(Object elementName, Map<String, Object> encodingHints)
            throws IOException {
        try {
            String elemName = elementName.toString();
            String[] elems = elemName.split(":");
            streamWriter.writeStartElement(elems[0], elems[1], namespaces.get(elems[0]));
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void writeElementValue(Object elementValue, Map<String, Object> encodingHints)
            throws IOException {

        writeElementNameAndValue(null, elementValue, encodingHints);
    }

    protected abstract void writeGeometry(Geometry writeGeometry) throws XMLStreamException;

    @Override
    public void writeStaticContent(
            String name, Object staticContent, Map<String, Object> encodingHints)
            throws IOException {
        try {
            streamWriter.writeStartElement(name);
            streamWriter.writeCharacters(staticContent.toString());
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void startObject(String name, Map<String,Object> encodingHints) throws IOException {
        writeElementName(name, encodingHints);
    }

    @Override
    public void endObject(String name, Map<String,Object> encodingHints) throws IOException {
        try {
            streamWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void startArray(String name,Map<String,Object> encodingHints) throws IOException {
        writeElementName(name, encodingHints);
    }

    @Override
    public void endArray(String name,Map<String,Object> encodingHints) throws IOException {
        try {
            streamWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void startTemplateOutput(Map<String, Object> encodingHints) throws IOException {
        try {
            streamWriter.writeStartDocument();
            streamWriter.writeStartElement("wfs", "FeatureCollection","http://www.opengis.net/wfs");
            Object attributes = encodingHints.get(ROOT_ELEMENT_ATTRIBUTES);
            if (attributes != null) {
                XMLTemplateReader.RootElementAttributes rootElementAttributes =
                        (XMLTemplateReader.RootElementAttributes) attributes;
                Map<String, String> namespaces = rootElementAttributes.getNamespaces();
                Map<String, String> xsi = rootElementAttributes.getSchemaLocations();
                Set<String> nsKeys = namespaces.keySet();
                for (String k : nsKeys) {
                    streamWriter.writeAttribute(k, namespaces.get(k));
                }
                Set<String> xsiKeys = xsi.keySet();
                for (String k : xsiKeys) {
                    streamWriter.writeAttribute(k, xsi.get(k));
                }
            }
            streamWriter.writeStartElement("gml", "featureMembers","http://www.opengis.net/gml/3.2");
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void endTemplateOutput(Map<String, Object> encodingHints) throws IOException {

        try {
            streamWriter.writeEndElement();
            streamWriter.writeEndElement();
            streamWriter.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            streamWriter.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    public void writeElementNameAndValue(
            String key, Object elementValue, Map<String, Object> encodingHints) throws IOException {
        Object encodeAsAttribute = encodingHints.get(ENCODE_AS_ATTRIBUTE);
        if (encodeAsAttribute != null
                && Boolean.valueOf(encodeAsAttribute.toString()).booleanValue())
            writeAsAttribute(key, elementValue, encodingHints);
        boolean repeatName = elementValue instanceof List && ((List) elementValue).size() > 1;
        if (key != null && !repeatName) writeElementName(key, encodingHints);
        try {
            if (elementValue instanceof String
                    || elementValue instanceof Number
                    || elementValue instanceof Boolean) {
                streamWriter.writeCharacters(String.valueOf(elementValue));
            } else if (elementValue instanceof Geometry) {
                writeGeometry((Geometry) elementValue);
            } else if (elementValue instanceof Date) {
                Date timeStamp = (Date) elementValue;
                String formatted = new StdDateFormat().withColonInTimeZone(true).format(timeStamp);
                streamWriter.writeCharacters(formatted);
            } else if (elementValue instanceof ComplexAttribute) {
                ComplexAttribute attr = (ComplexAttribute) elementValue;
                writeElementNameAndValue(null, attr.getValue(), encodingHints);
            } else if (elementValue instanceof Attribute) {
                Attribute attr = (Attribute) elementValue;
                writeElementNameAndValue(null, attr.getValue(), encodingHints);
            } else if (elementValue instanceof List) {
                List list = (List) elementValue;
                if (!repeatName) {
                    writeElementNameAndValue(null, list.get(0), encodingHints);
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        writeElementName(key, encodingHints);
                        writeElementNameAndValue(null, list.get(i), encodingHints);
                        endObject(key,encodingHints);
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
        if (key != null && !repeatName) {
            writeElementName(key, encodingHints);
        }
    }

    private void writeAsAttribute(
            String key, Object elementValue, Map<String, Object> encodingHints) throws IOException {
        try {
            if (key.indexOf(":") != -1) {
                String[] splitKey = key.split(":");
                streamWriter.writeAttribute(splitKey[1], elementValue.toString(), splitKey[0]);
            } else streamWriter.writeAttribute(key, elementValue.toString());
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }
}
