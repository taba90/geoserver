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

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.collection.ClippedFeatureCollection;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * A SimpleFeatureCollection that can filter features' geometries by a clip (crop) spatialFilter and
 * by an intersects spatialFilter.
 */
class ClipIntersectsFeatureCollection extends ClippedFeatureCollection {

    Geometry intersects;

    /**
     * @param delegate delegate Iterator to be used as a delegate.
     * @param clip the geometry to be used to clip (crop features).
     * @param intersects the geometry to be used to intersects features.
     * @param preserveZ flag to set to true if the clipping process should preserve the z dimension
     */
    ClipIntersectsFeatureCollection(
            SimpleFeatureCollection delegate,
            Geometry clip,
            Geometry intersects,
            boolean preserveZ) {
        super(delegate, clip, preserveZ);
        this.intersects = intersects;
    }

    /**
     * @param delegate delegate Iterator to be used as a delegate.
     * @param clip the geometry to be used to clip (crop features).
     * @param intersects the geometry to be used to intersects features.
     */
    ClipIntersectsFeatureCollection(
            SimpleFeatureCollection delegate, Geometry clip, Geometry intersects) {
        super(delegate, clip, false);
        this.intersects = intersects;
    }

    @Override
    public SimpleFeatureType getSchema() {
        return delegate.getSchema();
    }

    @Override
    public SimpleFeatureIterator features() {
        return new ClipIntersectsFeatureIterator(
                delegate.features(), clip, intersects, getSchema(), preserveZ);
    }

    @Override
    public int size() {
        try (SimpleFeatureIterator fi = features()) {
            int count = 0;
            while (fi.hasNext()) {
                fi.next();
                count++;
            }
            return count;
        }
    }
}
