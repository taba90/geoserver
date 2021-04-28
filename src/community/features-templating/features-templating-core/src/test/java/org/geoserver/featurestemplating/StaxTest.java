package org.geoserver.featurestemplating;

import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.geoserver.featurestemplating.readers.XMLTemplateReader;
import org.junit.Test;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

public class StaxTest {

    @Test
    public void TestStax() throws FileNotFoundException, XMLStreamException {
        URL url=getClass().getResource("xml-template-mock.xml");
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(url.getFile()));
        XMLTemplateReader templateReader=new XMLTemplateReader(reader,null);
        RootBuilder builder = templateReader.getRootBuilder();
        builder.getChildren();
    }
}
