package org.geoserver.featurestemplating.readers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.geoserver.featurestemplating.builders.impl.RootBuilder;
import org.junit.Test;
import org.xml.sax.helpers.NamespaceSupport;

public class XMLTemplateReaderTest {

    @Test
    public void testReader() throws FileNotFoundException, XMLStreamException {
        URL url = getClass().getResource("../response/MappedFeatureGML32.xml");
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        XMLEventReader reader =
                xmlInputFactory.createXMLEventReader(new FileInputStream(url.getFile()));
        XMLTemplateReader templateReader = new XMLTemplateReader(reader, new NamespaceSupport());
        RootBuilder builder = templateReader.getRootBuilder();
    }
}
