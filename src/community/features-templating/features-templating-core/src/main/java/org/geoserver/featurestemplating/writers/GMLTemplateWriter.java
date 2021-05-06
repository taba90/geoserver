package org.geoserver.featurestemplating.writers;

import static org.geoserver.featurestemplating.builders.EncodingHints.ROOT_ELEMENT_ATTRIBUTES;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.geoserver.featurestemplating.builders.EncodingHints;
import org.geoserver.featurestemplating.configuration.TemplateIdentifier;
import org.locationtech.jts.geom.Geometry;

public class GMLTemplateWriter extends XMLTemplateWriter {

    private GMLVersionManager versionManager;

    public GMLTemplateWriter(XMLStreamWriter streamWriter, String outputFormat) {
        super(streamWriter);
        if (outputFormat == null
                || outputFormat.equalsIgnoreCase(TemplateIdentifier.GML32.getOutputFormat()))
            this.versionManager = new GML32VersionManager(streamWriter);
        else if (outputFormat.equalsIgnoreCase(TemplateIdentifier.GML31.getOutputFormat()))
            this.versionManager = new GML31VersionManager(streamWriter);
        else if (TemplateIdentifier.GML2.getOutputFormat().contains(outputFormat))
            this.versionManager = new GML2VersionManager(streamWriter);
    }

    @Override
    public void startTemplateOutput(Map<String, Object> encodingHints) throws IOException {
        try {
            streamWriter.writeStartDocument();
            streamWriter.writeStartElement(
                    "wfs", "FeatureCollection", "http://www.opengis.net/wfs");
            Object attributes = encodingHints.get(ROOT_ELEMENT_ATTRIBUTES);
            if (attributes != null) {
                EncodingHints.RootElementAttributes rootElementAttributes =
                        (EncodingHints.RootElementAttributes) attributes;
                Map<String, String> namespaces = rootElementAttributes.getNamespaces();
                Map<String, String> xsi = rootElementAttributes.getSchemaLocations();
                Set<String> nsKeys = namespaces.keySet();
                for (String k : nsKeys) {
                    streamWriter.writeNamespace(k, namespaces.get(k));
                }
                Set<String> xsiKeys = xsi.keySet();
                for (String k : xsiKeys) {
                    streamWriter.writeAttribute(k, xsi.get(k));
                }
            }
            versionManager.startTemplateOutput();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void endTemplateOutput(Map<String, Object> encodingHints) throws IOException {

        try {
            versionManager.endTemplateOutput();
            streamWriter.writeEndElement();
            streamWriter.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void writeGeometry(Geometry writeGeometry) throws XMLStreamException {
        versionManager.writeGeometry(writeGeometry);
    }
}
