/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.schemalessfeatures.mongodb.filter;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.util.Set;
import org.bson.Document;

/** Utility class to find the type of an attribute in a schemaless context. */
public class MongoDataTypeFinder {
    MongoCollection<DBObject> collection;

    public MongoDataTypeFinder(MongoCollection<DBObject> collection) {
        this.collection = collection;
    }

    /**
     * Get the type of the attribute specified as a string PropertyName
     *
     * @param attribute the string PropertyName
     * @return the type of the attribute
     */
    public Class<?> getAttributeTypeResult(String attribute) {
        Document projection = new Document();
        projection.put(getJsonPathFromPropertyPath(attribute), 1);
        projection.put("_id", 0);
        Document query = new Document(attribute, new Document("$ne", "null"));
        try (MongoCursor<DBObject> cursor =
                collection.find(query).projection(projection).limit(1).cursor()) {
            Class<?> result = null;
            if (cursor.hasNext()) {
                DBObject dbRes = cursor.next();
                result = getFieldType(dbRes);
            }

            return result;
        }
    }

    private String getJsonPathFromPropertyPath(String propertyName) {
        String[] splittedPn = propertyName.split("/");
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < splittedPn.length; i++) {
            String xpathStep = splittedPn[i];
            if (xpathStep.indexOf(":") != -1) xpathStep = xpathStep.split(":")[1];
            int index = xpathStep.indexOf("Index");
            if (index != -1) {
                xpathStep = xpathStep.substring(0, index);
            }
            sb.append(xpathStep);
            if (i != splittedPn.length - 1) sb.append(".");
        }
        return sb.toString();
    }

    private Class<?> getFieldType(Object document) {
        if (document instanceof BasicDBList) {
            BasicDBList list = (BasicDBList) document;
            for (int i = 0; i < list.size(); i++) {
                Object element = list.get(i);
                if (element != null) {
                    Class<?> result = getFieldType(element);
                    if (result != null) return result;
                }
            }
        } else if (document instanceof BasicDBObject) {
            DBObject object = (DBObject) document;
            Set<String> keys = object.keySet();
            for (String k : keys) {
                Object value = object.get(k);
                if (value != null) return getFieldType(value);
            }
        } else {
            return document.getClass();
        }

        return null;
    }
}
