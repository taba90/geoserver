/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.schemalessfeatures.mongodb.mappers;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.geoserver.schemalessfeatures.SchemalessFeatureMapper;
import org.geoserver.schemalessfeatures.builders.SchemalessComplexTypeBuilder;
import org.geoserver.schemalessfeatures.builders.SchemalessComplexTypeFactory;
import org.geoserver.schemalessfeatures.type.SchemalessComplexType;
import org.geoserver.schemalessfeatures.type.SchemalessFeatureType;
import org.geotools.data.mongodb.MongoGeometryBuilder;
import org.geotools.feature.AttributeBuilder;
import org.geotools.feature.ComplexFeatureBuilder;
import org.geotools.feature.ValidatingFeatureFactoryImpl;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.PropertyDescriptor;

/**
 * A MongoDB implementation of SchemalessMapper. This class is responsible to build a Feature from a
 * DBObject source, and to create/update the AttrbiuteType along with the feature building process.
 */
public class SchemalessMongoToComplexMapper extends SchemalessFeatureMapper<DBObject> {

    private MongoGeometryBuilder geomBuilder;

    private SchemalessFeatureType type;

    public SchemalessMongoToComplexMapper(SchemalessFeatureType type) {
        super(
                new AttributeBuilder(new ValidatingFeatureFactoryImpl()),
                new SchemalessComplexTypeBuilder(new SchemalessComplexTypeFactory()));
        this.geomBuilder = new MongoGeometryBuilder();
        this.type = type;
    }

    @Override
    public Feature buildFeature(DBObject rootDBO) {
        attributeBuilder.init();
        List<Property> attributes = getNestedAttributes(rootDBO, type);
        ComplexFeatureBuilder featureBuilder = new ComplexFeatureBuilder(type);
        GeometryAttribute geometryAttribute = null;
        for (Property p : attributes) {
            if (p instanceof GeometryAttribute) {
                GeometryAttribute geom = (GeometryAttribute) p;
                if (p.getName().equals(type.getGeometryDescriptor().getName())) {
                    geometryAttribute = geom;
                    featureBuilder.append(p.getName(), geometryAttribute);
                }
            } else {
                featureBuilder.append(p.getName(), p);
            }
        }
        Feature f = featureBuilder.buildFeature(rootDBO.get("_id").toString());
        f.setDefaultGeometryProperty(geometryAttribute);
        return f;
    }

    private List<Property> getNestedAttributes(DBObject rootDBO, SchemalessComplexType parentType) {
        Set<String> keys = rootDBO.keySet();
        String namespaceURI = type.getName().getNamespaceURI();
        List<Property> attributes = new ArrayList<>();
        for (String key : keys) {
            Object value = rootDBO.get(key);
            if (value == null) continue;

            if (value instanceof BasicDBList) {
                BasicDBList list = (BasicDBList) value;
                attributes.addAll(
                        buildComplexAttributesUnbounded(namespaceURI, key, list, parentType));
            } else if (value instanceof DBObject) {
                DBObject dbObj = (DBObject) value;
                if (isGeometry(dbObj)) {
                    Geometry geom = geomBuilder.toGeometry(dbObj);
                    GeometryAttribute geometryAttribute =
                            buildGeometryAttribute(geom, namespaceURI, key, parentType);
                    attributes.add(geometryAttribute);
                    continue;
                }
                attributes.add(
                        buildComplexAttribute(
                                namespaceURI, key, (DBObject) value, parentType, false));
            } else {
                attributes.add(buildSimpleAttribute(namespaceURI, key, value, parentType));
            }
        }
        return attributes;
    }

    private Attribute buildComplexAttribute(
            String namespaceURI,
            String attrName,
            DBObject dbobject,
            SchemalessComplexType parentType,
            boolean isCollection) {
        PropertyDescriptor descriptorProperty = parentType.getDescriptor(attrName);
        if (descriptorProperty == null)
            descriptorProperty =
                    buildFullyObjectPropertyModelDescriptor(
                            parentType, namespaceURI, attrName, isCollection);
        ComplexType complexTypeProperty = (ComplexType) descriptorProperty.getType();
        PropertyDescriptor nestedFeatureDescriptor = extractFeatureDescriptor(descriptorProperty);
        SchemalessComplexType nestedFeatureType =
                (SchemalessComplexType) nestedFeatureDescriptor.getType();
        ComplexFeatureBuilder featureBuilder =
                new ComplexFeatureBuilder((AttributeDescriptor) nestedFeatureDescriptor);
        List<Property> attributes = getNestedAttributes(dbobject, nestedFeatureType);
        for (Property p : attributes) featureBuilder.append(p.getName(), p);
        Feature f = featureBuilder.buildFeature(null);
        ComplexAttribute propertyAttribute =
                attributeBuilder.createComplexAttribute(
                        Arrays.asList(f),
                        complexTypeProperty,
                        (AttributeDescriptor) descriptorProperty,
                        null);
        return propertyAttribute;
    }

    private List<Property> buildComplexAttributesUnbounded(
            String namespaceURI,
            String attrName,
            BasicDBList value,
            SchemalessComplexType parentType) {
        List<Property> attributes = new ArrayList<>();
        for (int i = 0; i < value.size(); i++) {
            Object obj = value.get(i);
            if (obj instanceof DBObject) {
                Attribute attribute =
                        buildComplexAttribute(
                                namespaceURI, attrName, (DBObject) obj, parentType, true);
                attributes.add(attribute);
            } else if (obj != null) {
                Attribute attribute = buildSimpleAttribute(namespaceURI, attrName, obj, parentType);
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    private GeometryAttribute buildGeometryAttribute(
            Geometry geom, String namespaceURI, String name, SchemalessComplexType parentType) {
        AttributeDescriptor descriptor = (AttributeDescriptor) parentType.getDescriptor(name);
        if (descriptor == null) {
            typeBuilder
                    .binding(geom.getClass())
                    .name(name)
                    .namespaceURI(namespaceURI)
                    .maxOccurs(1)
                    .minOccurs(0);
            GeometryType attrType = typeBuilder.buildGeometryType();
            descriptor = typeBuilder.buildDescriptor(attrType.getName(), attrType);
            GeometryDescriptor geometryDescriptor = type.getGeometryDescriptor();
            if (geometryDescriptor == null) {
                type.setGeometryDescriptor((GeometryDescriptor) descriptor);
                type.addPropertyDescriptor(descriptor);
            } else {
                parentType.addPropertyDescriptor(descriptor);
            }
        }
        attributeBuilder.setDescriptor(descriptor);
        geom = DouglasPeuckerSimplifier.simplify(geom, 0.333);
        return (GeometryAttribute) attributeBuilder.buildSimple(null, geom);
    }

    private boolean isGeometry(DBObject object) {
        Set keys = object.keySet();
        if (keys.size() != 2 || !keys.contains("coordinates") || !keys.contains("type")) {
            // is mongo db object but not a geometry
            return false;
        }
        return true;
    }

    // Get the nested feature descriptor from its property descriptor container
    private PropertyDescriptor extractFeatureDescriptor(PropertyDescriptor descriptorProperty) {
        if (descriptorProperty == null) return null;
        ComplexType typeFeature = (ComplexType) descriptorProperty.getType();
        return typeFeature.getDescriptors().iterator().next();
    }
}
