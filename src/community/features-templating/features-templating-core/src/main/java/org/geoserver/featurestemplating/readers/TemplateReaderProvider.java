package org.geoserver.featurestemplating.readers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TemplateReaderProvider {
    enum SupportedExtension{
        JSON,
        XML,
        XHTML
    }

    public static TemplateReader findReader(String resourceExtension, InputStream is, TemplateReaderConfiguration configuration) throws IOException {
        TemplateReader reader;
        if (resourceExtension.equals(SupportedExtension.JSON.name())){
            ObjectMapper mapper =
                    new ObjectMapper(new JsonFactory().enable(JsonParser.Feature.ALLOW_COMMENTS));
            reader =
                    new JSONTemplateReader(mapper.readTree(is), configuration);
        } else if (resourceExtension.equals(SupportedExtension.XHTML.name())
                || resourceExtension.equals(SupportedExtension.XML.name())){
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
            XMLEventReader eventReader= null;
            try {
                eventReader = xmlInputFactory.createXMLEventReader(is);
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
            reader=new XMLTemplateReader(eventReader,configuration.getNamespaces());
        } else {
            throw new UnsupportedOperationException("Not a supported extension "+resourceExtension);
        }
        return reader;
    }
}
