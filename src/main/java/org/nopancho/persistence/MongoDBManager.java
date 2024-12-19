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

    public MongoClient getClient() {
        return client;
    }

    public void setClient(MongoClient client) {
        this.client = client;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    /**
     * Generic crud
     */
    /**
     * read operation
     */
    public MongoCollection<Document> getCollection(String collection) {
        return database.getCollection(collection);
    }

    public Document read(String collection, String id) {
        MongoCollection<Document> col = database.getCollection(collection);
        FindIterable<Document> documents = col.find(eq("_id", new ObjectId(id)));
        return documents.first();
    }

    public Document readOne(String collection, String id) {
        MongoCollection<Document> col = database.getCollection(collection);
        FindIterable<Document> documents = col.find(eq("_id", new ObjectId(id)));
        return documents.first();
    }

    public Document readOneByCriteria(String collection, String field, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(field, value);
        return readOneByCriteria(collection, map);
    }

    public Document readOneByCriteria(String collection, Map<String, Object> criteria) {
        BasicDBObject query = getQuery(criteria);
        MongoCollection<Document> col = database.getCollection(collection);
        FindIterable<Document> users = col.find(query);
        Document first = CollectionHelper.getFirst(users);
        return first;
    }

    public List<Document> readAllByCriteria(String collection, String field, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(field, value);
        return readAllByCriteria(collection, map);
    }

    public List<Document> readAllByCriteria(String collection, Map<String, Object> criteria) {
        BasicDBObject query = getQuery(criteria);
        MongoCollection<Document> col = database.getCollection(collection);
        ArrayList<Document> documents = col.find(query).into(new ArrayList<Document>());
        return documents;
    }

    public List<Document> readAll(String collection) {
        MongoCollection<Document> col = database.getCollection(collection);
        ArrayList<Document> documents = col.find().into(new ArrayList<Document>());
        return documents;
    }

    public List<Document> readAll(String collection, BasicDBObject query) {
        MongoCollection<Document> col = database.getCollection(collection);
        ArrayList<Document> documents = col.find(query).into(new ArrayList<Document>());
        return documents;
    }

    public List<Document> readAllWhereFieldIn(String collection, String field, List<String> list) {
        MongoCollection<Document> col = database.getCollection(collection);
        ArrayList<Document> documents = col.find(in(field, list)).into(new ArrayList<Document>());
        return documents;
    }


    /**
     * Create operations
     */
    /**
     * @param collection
     * @param document
     */
    public ObjectId createOrUpdateOne(String collection, Document document) {
        MongoCollection<Document> col = database.getCollection(collection);
        ObjectId objectId = document.getObjectId("_id");
        if (objectId == null) {
            // new user
            col.insertOne(document);
            ObjectId createdId = document.getObjectId("_id");
            return createdId;
        } else {
            col.findOneAndReplace(eq("_id", objectId), document);
            return objectId;
        }
    }

    public void createMany(String collection, List<Document> documents) {
        try {
            MongoCollection<Document> col = database.getCollection(collection);
            col.insertMany(documents);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateOne(String collection, Document document) {
        MongoCollection<Document> col = database.getCollection(collection);
        col.findOneAndReplace(eq("_id", document.getObjectId("_id")), document);
    }

    public void updateMany(String collection, List<Document> documents) {
        try {
            Set<ObjectId> objectIds = new HashSet<>();
            List<Document> filteredDocuments = new ArrayList<>();
            for (Document document : documents) {
                ObjectId id = document.getObjectId("_id");
                if (!objectIds.contains(id)) {
                    objectIds.add(id);
                    filteredDocuments.add(document);
                }
            }
            MongoCollection<Document> col = database.getCollection(collection);
            col.deleteMany(Filters.in("_id", objectIds));
            col.insertMany(filteredDocuments);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete opearations
     */

    public void deleteOne(String collection, String id) {
        MongoCollection<Document> col = database.getCollection(collection);
        col.findOneAndDelete(eq("_id", new ObjectId(id)));
    }

    public void deleteOneByCriteria(String collection, String field, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(field, value);
        deleteOneByCriteria(collection, map);
    }

    public void deleteOneByCriteria(String collection, Map<String, Object> criteria) {
        BasicDBObject query = getQuery(criteria);
        MongoCollection<Document> col = database.getCollection(collection);
        col.deleteOne(query);
    }

    public void deleteAll(String collection) {
        MongoCollection<Document> col = database.getCollection(collection);
        col.deleteMany(new Document());
    }

    public void deleteAllByCriteria(String collection, String field, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(field, value);
        deleteAllByCriteria(collection, map);
    }

    public void deleteAllByCriteria(String collection, Map<String, Object> criteria) {
        BasicDBObject query = getQuery(criteria);
        deleteAll(collection, query);
    }

    public void deleteAll(String collection, BasicDBObject query) {
        MongoCollection<Document> col = database.getCollection(collection);
        col.deleteMany(query);
    }

    public void dropCollection(String collection) {
        MongoCollection<Document> col = database.getCollection(collection);
        col.drop();
    }

    /**
     * Streaming operations
     */
    public void streamByCriteria(String collection, BlockingQueue<Document> queue, Bson query) {
        new Thread() {
            @Override
            public void run() {
                MongoCollection<Document> col = database.getCollection(collection);

                FindIterable<Document> documents = col.find(query).batchSize(5000);
                for (Document document : documents) {
                    pushToQueue(queue, document);
                }
            }
        }.start();
    }

    public Long getNumberOfAllDocuments(String collection) {
        MongoCollection<Document> col = database.getCollection(collection);
        long count = col.count();
        return count;
    }

    public Long getNumberOfDocuments(String collection, String field, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(field, value);
        return getNumberOfDocuments(collection, map);
    }

    public Long getNumberOfDocuments(String collection, Map<String, Object> criteria) {
        BasicDBObject query = getQuery(criteria);
        MongoCollection<Document> col = database.getCollection(collection);
        long count = col.count(query);
        return count;
    }

    private void pushToQueue(BlockingQueue<Document> queue, Document document) {
        if (queue.size() >= 5000) {
            ThreadHelper.deepSleep(100);
            pushToQueue(queue, document);
        } else {
            queue.add(document);
        }
    }

    public BasicDBObject getQuery(Map<String, Object> criteria) {
        Map<String, List<Object>> emptyMap = Collections.emptyMap();
        return getQuery(criteria, emptyMap);
    }

    public BasicDBObject getQuery(Map<String, Object> isCriteria, Map<String, List<Object>> inCriteria) {
        QueryBuilder queryBuilder = new QueryBuilder();

        for (Map.Entry<String, Object> stringObjectEntry : isCriteria.entrySet()) {
            String key = stringObjectEntry.getKey();
            Object value = stringObjectEntry.getValue();
            queryBuilder.and(key).is(value);
        }

        for (Map.Entry<String, List<Object>> entry : inCriteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            queryBuilder.and(key).in(value);
        }

        DBObject dbObject = queryBuilder.get();
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.putAll(dbObject);
        return basicDBObject;
    }


    public void insertMany(String collection, List<Document> documents) {
        getCollection(collection).insertMany(documents);
    }

    public void insertOne(String collection, Document document) {
        getCollection(collection).insertOne(document);
    }

    public static void main(String[] args) {
        MongoDBManager instance = MongoDBManager.getInstance();

        MongoDatabase database = instance.getDatabase();
        database.createCollection("test");
        try {
            String s = FileHelper.readFileToString("data/test1.json");
            JsonArray array = new JsonArray(s);
            for (Object o : array) {
                JsonObject jo = (JsonObject) o;
                Document document = Document.parse(jo.toString());
                MongoDBManager.getInstance().createOrUpdateOne("test", document);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonException e) {
            e.printStackTrace();
        }
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("name", "Sebastian");
        criteria.put("age", 38);
        List<Document> test = MongoDBManager.getInstance().readAllByCriteria("test", criteria);
        for (Document document : test) {
            System.out.println(document.toJson());
        }

        // List<Document> documents = MongoDBManager.getInstance().readAllByCriteria("test", "name", "Sebastian");
        // for (Document document : documents) {
        // System.out.println(document.toJson());
        // }
        database.getCollection("test").drop();

    }

}
