package org.nopancho.persistence;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.nopancho.config.ConfigManager;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.palladian.helper.StopWatch;
import ws.palladian.helper.ThreadHelper;
import ws.palladian.helper.collection.CollectionHelper;
import ws.palladian.helper.io.FileHelper;
import ws.palladian.persistence.json.JsonArray;
import ws.palladian.persistence.json.JsonException;
import ws.palladian.persistence.json.JsonObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

/**
 * Created by Sebastian on 24.11.2017.
 */
public class MongoDBManager {


    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBManager.class);
    private static MongoDBManager INSTANCE = new MongoDBManager();

    private MongoClient client;
    private MongoDatabase database;

    public MongoDBManager() {
        resetDatabase();
    }

    public void resetDatabase() {
        // Creating a Mongo client
        StopWatch stopWatch = new StopWatch();
        LOGGER.info("initialising database");

        PropertiesConfiguration config = ConfigManager.getConfig();
        String mongoDbUri = config.getString("mongodb.uri");
        if (mongoDbUri == null || mongoDbUri.isEmpty()) {
            LOGGER.error("Db url not configured");
            System.exit(0);
        }
        MongoClientURI mongoClientURI = new MongoClientURI(mongoDbUri);
        this.client = new MongoClient(mongoClientURI);
        String dbname = ConfigManager.getConfig().getString("dbname");
        if (dbname == null) {
            LOGGER.error("Db name not configured");
            System.exit(0);
        }
        LOGGER.info("trying to initialize database for "+mongoDbUri+" "+dbname);
        this.database = client.getDatabase(dbname);
        LOGGER.info("database initialized in " + stopWatch.getElapsedTimeString());
    }

    public static MongoDBManager getInstance() {
        return INSTANCE;
    }


    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoCollection<Document> getCollection(String collection) {
        return database.getCollection(collection);
    }
}
