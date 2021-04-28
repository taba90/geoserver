package org.geoserver.featurestemplating.writers;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.geoserver.featurestemplating.readers.XMLTemplateReader;
import org.locationtech.jts.geom.Geometry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class XMLTemplateWriter implements TemplateOutputWriter{

    private XMLStreamWriter streamWriter;

    public XMLTemplateWriter(XMLStreamWriter streamWriter){
        this.streamWriter=streamWriter;
    }
    @Override
    public void writeElementName(Object elementName, Map<String, Object> encodingHints) throws IOException {
        try {
            String elemName=elementName.toString();
            String[] elems=elemName.split(":");
            streamWriter.writeStartElement(elems[0],elems[1]);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void writeElementValue(Object elementValue, Map<String, Object> encodingHints) throws IOException {
        try {
            if (elementValue instanceof Geometry){
                writeGeometry((Geometry)elementValue);
            } else {
                streamWriter.writeCharacters(elementValue.toString());
            }
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private void writeValue (Object elementValue){
        if (elementValue instanceof Geometry){
            writeGeometry((Geometry) elementValue);
        } else if (elementValue instanceof Date){
            Date timeStamp = (Date) elementValue;
            String formatted = new StdDateFormat().withColonInTimeZone(true).format(timeStamp);
        }

    }

    private void writeGeometry(Geometry writeGeometry){

    }

    @Override
    public void writeStaticContent(String name, Object staticContent, Map<String, Object> encodingHints) throws IOException {
        try {
            streamWriter.writeStartElement(name);
            streamWriter.writeCharacters(staticContent.toString());
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void startObject(String name) throws IOException {
        writeElementName(name,null);
    }

    @Override
    public void endObject(String name) throws IOException {
        try {
            streamWriter.writeEndElement();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startArray(String name) throws IOException {
        writeElementName(name,null);
    }

    @Override
    public void endArray(String name) throws IOException {
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
            streamWriter.writeStartElement("wfs","FeatureCollection");
            Object attributes=encodingHints.get(XMLTemplateReader.ROOT_ELEMENT_ATTRIBUTES);
            if (attributes!=null){
                XMLTemplateReader.RootElementAttributes rootElementAttributes=(XMLTemplateReader.RootElementAttributes) attributes;
                Map<String,String> namespaces=rootElementAttributes.getNamespaces();
                Map<String,String> xsi=rootElementAttributes.getSchemaLocations();
                Set<String> nsKeys=namespaces.keySet();
                for (String k:nsKeys){
                    streamWriter.writeAttribute(k,namespaces.get(k));
                }
                Set<String>xsiKeys=xsi.keySet();
                for (String k:xsiKeys){
                    streamWriter.writeAttribute(k,xsi.get(k));
                }
            }
        }catch(XMLStreamException e){
            throw new IOException(e);
        }
    }

    @Override
    public void endTemplateOutput(Map<String, Object> encodingHints) throws IOException {

        try {
            streamWriter.writeEndElement();
        streamWriter.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            streamWriter.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeElementNameAndValue(Object result, String key, Map<String, Object> encodingHints){
    }
}
