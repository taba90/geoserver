package org.geoserver.featurestemplating.writers;

import java.util.Date;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml3.MultiSurface;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public abstract class GMLVersionManager {

    protected XMLStreamWriter streamWriter;

    private CRS.AxisOrder axisOrder = CRS.AxisOrder.NORTH_EAST;

    private String coordElementName;

    private String exteriorElementName;

    private String interiorElementName;

    public GMLVersionManager(
            XMLStreamWriter streamWriter,
            String coorElementName,
            String exteriorElementName,
            String interiorElementName) {
        this.streamWriter = streamWriter;
        this.coordElementName = coorElementName;
        this.exteriorElementName = exteriorElementName;
        this.interiorElementName = interiorElementName;
    }

    public void writeGeometry(Geometry geometry) throws XMLStreamException {
        if (geometry instanceof Point) {
            writePoint((Point) geometry);
        } else if (geometry instanceof MultiPoint) {
            writeMultiPoint((MultiPoint) geometry);
        } else if (geometry instanceof LineString) {
            writeLineString((LineString) geometry);
        } else if (geometry instanceof MultiLineString) {
            writeMultiLineString((MultiLineString) geometry);
        } else if (geometry instanceof Polygon) {
            writePolygon((Polygon) geometry);
        } else if (geometry instanceof MultiPolygon) {
            writeMultiPolygon((MultiPolygon) geometry);
        }
    }

    private void writePoint(Point point) throws XMLStreamException {
        streamWriter.writeStartElement("gml", "Point", "http://www.opengis.net/gml/3.2");
        streamWriter.writeStartElement(
                "gml",
                coordElementName.equals("coordinates") ? coordElementName : "pos",
                "http://www.opengis.net/gml/3.2");
        double y = point.getY();
        double x = point.getX();
        if (axisOrder == CRS.AxisOrder.NORTH_EAST) {
            streamWriter.writeCharacters(y + " " + x);
        } else {
            streamWriter.writeCharacters(x + " " + y);
        }
        streamWriter.writeEndElement();
        streamWriter.writeEndElement();
    }

    private void writeMultiPoint(MultiPoint multiPoint) throws XMLStreamException {
        int nPoints = multiPoint.getNumPoints();
        streamWriter.writeStartElement("gml", "MultiPoint", "http://www.opengis.net/gml/3.2");
        for (int i = 0; i < nPoints; i++) {
            streamWriter.writeStartElement("gml", "pointMember", "http://www.opengis.net/gml/3.2");
            writePoint((Point) multiPoint.getGeometryN(i));
            streamWriter.writeEndElement();
        }
        streamWriter.writeEndElement();
    }

    private void writePolygon(Polygon polygon) throws XMLStreamException {
        LinearRing exteriorRing = polygon.getExteriorRing();
        Coordinate[] coordinates = exteriorRing.getCoordinates();
        streamWriter.writeStartElement("gml", "Polygon", "http://www.opengis.net/gml/3.2");
        writePolygonRing(exteriorElementName, coordinates);
        int numInterior = polygon.getNumInteriorRing();
        for (int i = 0; i < numInterior; i++) {
            coordinates = polygon.getInteriorRingN(i).getCoordinates();
            writePolygonRing(interiorElementName, coordinates);
        }
        streamWriter.writeEndElement();
    }

    private void writePolygonRing(String ringName, Coordinate[] coordinates)
            throws XMLStreamException {
        streamWriter.writeStartElement("gml", ringName, "http://www.opengis.net/gml/3.2");
        streamWriter.writeStartElement("gml", "LinearRing", "http://www.opengis.net/gml/3.2");
        streamWriter.writeStartElement("gml", coordElementName, "http://www.opengis.net/gml/3.2");
        writeCoordinates(coordinates);
        streamWriter.writeEndElement();
        streamWriter.writeEndElement();
        streamWriter.writeEndElement();
    }

    private void writeLineString(LineString lineString) throws XMLStreamException {
        Coordinate[] coordinates = lineString.getCoordinates();
        streamWriter.writeStartElement("gml", "LineString", "http://www.opengis.net/gml/3.2");
        streamWriter.writeStartElement("gml", coordElementName, "http://www.opengis.net/gml/3.2");
        writeCoordinates(coordinates);
        streamWriter.writeEndElement();
        streamWriter.writeEndElement();
    }

    private void writeMultiLineString(MultiLineString lineString) throws XMLStreamException {
        int numGeom = lineString.getNumGeometries();
        streamWriter.writeStartElement("gml", "MultiLineString", "http://www.opengis.net/gml/3.2");
        for (int i = 0; i < numGeom; i++) {
            streamWriter.writeStartElement(
                    "gml", "LineStringMember", "http://www.opengis.net/gml/3.2");
            writeLineString((LineString) lineString.getGeometryN(i));
            streamWriter.writeEndElement();
        }
        streamWriter.writeEndElement();
    }

    private void writeMultiPolygon(MultiPolygon multiPolygon) throws XMLStreamException {
        int numGeom = multiPolygon.getNumGeometries();
        boolean isMultiSurface = multiPolygon instanceof MultiSurface;
        streamWriter.writeStartElement(
                "gml",
                isMultiSurface ? "MultiSurface" : "MultiPolygon",
                "http://www.opengis.net/gml/3.2");
        for (int i = 0; i < numGeom; i++) {
            streamWriter.writeStartElement(
                    "gml",
                    isMultiSurface ? "surfaceMember" : "polygonMember",
                    "http://www.opengis.net/gml/3.2");
            writePolygon((Polygon) multiPolygon.getGeometryN(i));
            streamWriter.writeEndElement();
        }
        streamWriter.writeEndElement();
    }

    private void writeCoordinates(Coordinate[] coordinates) throws XMLStreamException {
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate coor = coordinates[i];
            double y = coor.getX();
            double x = coor.getY();
            String textString;
            if (axisOrder == CRS.AxisOrder.NORTH_EAST) {
                textString = y + " " + x;
            } else {
                textString = x + " " + y;
            }
            if (i != coordinates.length - 1) textString += " ";
            streamWriter.writeCharacters(textString);
        }
    }

    public void writeTimeStamp(Date date) {}

    public abstract void writeNumberReturned(String numberReturned) throws XMLStreamException;

    public abstract void writeNumberMatched(String numberMatched) throws XMLStreamException;

    public abstract void writeBoundingBox(ReferencedEnvelope envelope) throws XMLStreamException;

    public void startTemplateOutput() throws XMLStreamException {}

    public void endTemplateOutput() throws XMLStreamException {}
}
