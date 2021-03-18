/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.schemalessfeatures.mongodb.data;

import com.mongodb.ConnectionString;
import com.mongodb.DBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.geoserver.schemalessfeatures.data.SchemalessDataAccess;
import org.geotools.data.FeatureSource;
import org.geotools.filter.FilterCapabilities;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Id;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Within;

public class MongoSchemalessDataAccess extends SchemalessDataAccess {

    private MongoDatabase database;

    private MongoClient client;

    public MongoSchemalessDataAccess(String connectionString) {
        super();
        ConnectionString connection = getConnectionString(connectionString);
        this.client = createMongoClient(connection);
        MongoDatabase db = createMongoDatabase(client, connection.getDatabase());
        if (db == null) client.close();
        else this.database = db;
    }

    @Override
    protected List<Name> createTypeNames() {

        Set<String> collectionNames = new LinkedHashSet<>();
        database.listCollectionNames().forEach(n -> collectionNames.add(n));
        return collectionNames.stream().map(s -> name(s)).collect(Collectors.toList());
    }

    @Override
    public FeatureSource<FeatureType, Feature> getFeatureSource(Name typeName) throws IOException {
        MongoCollection<DBObject> collection =
                database.getCollection(typeName.getLocalPart(), DBObject.class);
        return new MongoSchemalessFeatureSource(typeName, collection, this);
    }

    private final ConnectionString getConnectionString(String dataStoreURI) {
        if (dataStoreURI == null) {
            throw new IllegalArgumentException("ConnectionString cannot be null");
        }
        if (!dataStoreURI.startsWith("mongodb://")) {
            throw new IllegalArgumentException(
                    "incorrect scheme for URI, expected to begin with \"mongodb://\", found URI of \""
                            + dataStoreURI
                            + "\"");
        }
        return new ConnectionString(dataStoreURI);
    }

    private final MongoClient createMongoClient(ConnectionString connectionString) {
        try {
            return MongoClients.create(connectionString);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Unknown mongodb host(s): " + connectionString.toString(), e);
        }
    }

    private final MongoDatabase createMongoDatabase(MongoClient mongoClient, String databaseName) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        if (database == null) {
            mongoClient.close(); // This smells bad...
            throw new IllegalArgumentException(
                    "Unknown mongodb database, \"" + databaseName + "\"");
        }
        return database;
    }

    @Override
    protected FilterCapabilities createFilterCapabilities() {
        {
            FilterCapabilities capabilities = new FilterCapabilities();
            capabilities.addAll(FilterCapabilities.LOGICAL_OPENGIS);

            capabilities.addAll(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);
            capabilities.addType(PropertyIsNull.class);
            capabilities.addType(PropertyIsBetween.class);
            capabilities.addType(PropertyIsLike.class);

            capabilities.addType(BBOX.class);
            capabilities.addType(Intersects.class);
            capabilities.addType(Within.class);

            capabilities.addType(Id.class);

            return capabilities;
        }
    }

    @Override
    public void dispose() {
        client.close();
    }
}
