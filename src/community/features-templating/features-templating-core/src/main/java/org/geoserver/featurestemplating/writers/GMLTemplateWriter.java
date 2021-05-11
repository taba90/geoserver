package org.geoserver.featurestemplating.writers;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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

            Set<String> nsKeys = namespaces.keySet();
            for (String k : nsKeys) {
                streamWriter.writeNamespace(k, namespaces.get(k));
            }
            Set<String> xsiKeys = schemaLocations.keySet();
            for (String k : xsiKeys) {
                streamWriter.writeAttribute(k, schemaLocations.get(k));
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
