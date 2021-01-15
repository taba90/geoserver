/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2020, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geoserver.security.decorators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.collection.ClippedFeatureIterator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;

/**
 * A SimpleFeatureCollection that can filter features' geometries by a clip (crop) spatialFilter and
 * by an intersects spatialFilter. If a geometry is hit by both the result of the two filters is
 * merged.
 */
class ClipIntersectsFeatureIterator extends ClippedFeatureIterator {
    private Geometry intersects;

    /**
     * @param delegate delegate Iterator to be used as a delegate.
     * @param clip the geometry to be used to clip (crop features).
     * @param intersects the geometry to be used to intersects features.
     * @param schema the featureType
     * @param preserveZ flag to set to true if the clipping process should preserve the z dimension
     */
    ClipIntersectsFeatureIterator(
            SimpleFeatureIterator delegate,
            Geometry clip,
            Geometry intersects,
            SimpleFeatureType schema,
            boolean preserveZ) {
        super(delegate, clip, schema, preserveZ);
        this.intersects = intersects;
    }

    @Override
    public boolean hasNext() {

        while (next == null && delegate.hasNext()) {
            // try building the clipped feature out of the original feature, if the
            // default geometry is clipped out, skip it
            SimpleFeature f = delegate.next();

            boolean doTheClip = intersects == null ? true : false;

            Map<Name, Geometry> intersectedGeometries = null;
            if (intersects != null) {
                Map<Name, Geometry> geometryAttributes = extractGeometryAttributes(f);
                intersectedGeometries =
                        getIntersectingGeometries(geometryAttributes, f.getFeatureType());
                // if there is at least one geometryCollection or not all the geometry
                // attributes were intersected performs also the clip
                if (intersectedGeometries != null)
                    doTheClip =
                            intersectedGeometries
                                            .values()
                                            .stream()
                                            .anyMatch(g -> g instanceof GeometryCollection)
                                    || geometryAttributes.size() > intersectedGeometries.size();
            }

            boolean clippedOut = false;
            if (doTheClip) clippedOut = prepareBuilderForNextFeature(f);

            if (!clippedOut) {
                // build the next feature
                next = fb.buildFeature(f.getID());
                unionWithIntersected(intersectedGeometries);

            } else if (intersectedGeometries != null && !intersectedGeometries.isEmpty()) {
                next = fb.buildFeature(f.getID());
                for (Name name : intersectedGeometries.keySet()) {
                    next.setAttribute(name, intersectedGeometries.get(name));
                }
            }

            fb.reset();
        }

        return next != null;
    }

    // union the clipped geometries with the intersected one
    private void unionWithIntersected(Map<Name, Geometry> intersectedGeometries) {
        for (Name name : intersectedGeometries.keySet()) {
            Geometry intersected = intersectedGeometries.get(name);
            if (intersected != null && !intersected.isEmpty())
                next.setAttribute(name, ((Geometry) next.getAttribute(name)).union(intersected));
        }
    }

    private Map<Name, Geometry> getIntersectingGeometries(
            Map<Name, Geometry> geometryAttributes, SimpleFeatureType type) {
        Map<Name, Geometry> intersectedGeometries = new HashMap<>();
        for (Name name : geometryAttributes.keySet()) {
            Geometry geom = geometryAttributes.get(name);
            if (geom instanceof GeometryCollection) {
                Class binding = type.getDescriptor(name.getLocalPart()).getType().getBinding();
                Geometry intersected =
                        filtersGeometryCollection((GeometryCollection) geom, binding);
                if (intersected != null) intersectedGeometries.put(name, intersected);
            } else {
                if (geom.intersects(intersects)) {
                    intersectedGeometries.put(name, geom);
                }
            }
        }
        return intersectedGeometries;
    }

    // filters the geometry collection according to the intersects geometry
    private Geometry filtersGeometryCollection(GeometryCollection collection, Class<?> binding) {
        List<Geometry> geometries = new ArrayList<>();
        int size = collection.getNumGeometries();
        for (int i = 0; i < size; i++) {
            Geometry geom = collection.getGeometryN(i);
            if (geom.intersects(intersects)) {
                geometries.add(geom);
            }
        }
        if (!geometries.isEmpty()) return buildGeometryCollection(geometries, binding);
        return null;
    }

    private Geometry buildGeometryCollection(List<Geometry> geometries, Class<?> clazz) {
        GeometryFactory factory = new GeometryFactory();
        Geometry result = null;
        int size = geometries.size();
        if (clazz.isAssignableFrom(MultiPoint.class)) {
            Point[] points = new Point[size];
            for (int i = 0; i < size; i++) points[i] = (Point) geometries.get(i);
            result = factory.createMultiPoint(geometries.toArray(points));
        } else if (clazz.isAssignableFrom(MultiLineString.class)) {
            LineString[] lines = new LineString[size];
            for (int i = 0; i < size; i++) lines[i] = (LineString) geometries.get(i);
            result = factory.createMultiLineString(geometries.toArray(lines));
        } else if (clazz.isAssignableFrom(MultiPolygon.class)) {
            Polygon[] polygons = new Polygon[size];
            for (int i = 0; i < size; i++) polygons[i] = (Polygon) geometries.get(i);
            result = factory.createMultiPolygon(geometries.toArray(polygons));
        }
        return result;
    }

    private Map<Name, Geometry> extractGeometryAttributes(SimpleFeature f) {
        Map<Name, Geometry> geometryAttributes = new HashMap<>();
        for (AttributeDescriptor ad : f.getFeatureType().getAttributeDescriptors()) {
            Object attribute = f.getAttribute(ad.getName());
            if (ad instanceof GeometryDescriptor) {
                geometryAttributes.put(ad.getName(), (Geometry) attribute);
            }
        }
        return geometryAttributes;
    }
}
