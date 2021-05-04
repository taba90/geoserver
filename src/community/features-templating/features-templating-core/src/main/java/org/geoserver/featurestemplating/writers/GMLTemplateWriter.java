package org.geoserver.featurestemplating.writers;

import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

public class GMLTemplateWriter extends XMLTemplateWriter {

    public GMLTemplateWriter(XMLStreamWriter streamWriter, Map<String, String> namespaces) {
        super(streamWriter, namespaces);
    }

    @Override
    protected void writeGeometry(Geometry writeGeometry) throws XMLStreamException {
        if (writeGeometry instanceof Point) {
            writePoint((Point) writeGeometry);
        }
    }

    private void writePoint(Point point) throws XMLStreamException {
        streamWriter.writeStartElement("gml", "Point", "http://www.opengis.net/gml/3.2");
        streamWriter.writeStartElement("gml", "pos", "http://www.opengis.net/gml/3.2");
        double y = point.getY();
        double x = point.getX();
        if (axisOrder == CRS.AxisOrder.NORTH_EAST) {
            streamWriter.writeCharacters(y + " " + x);
        } else {
            streamWriter.writeCharacters(x + " " + y);
        }
    }
}
