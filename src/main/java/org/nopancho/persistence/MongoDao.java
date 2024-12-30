package org.nopancho.persistence;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.UpdateResult;
import org.nopancho.persistence.model.Docable;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import ws.palladian.helper.collection.CollectionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ssprenger on 06.04.2019.
 * <p>
 * <p>
 * A utility class to quickly implement DAO for all kind of objects
 */
public class MongoDao<T extends Docable, I> {
    public static final String SEQUENCE_VALUE = "sequence_value";

    private MongoCollection<Document> collection;
    private String collectionName;
    private Class<T> objectClass;
    private Class<I> identifierClass;

    public MongoDao(String collection, Class<T> objectClazz, Class<I> identifierClass) {
        this.objectClass = objectClazz;
        this.identifierClass = identifierClass;
        this.collection = MongoDBManager.getInstance().getCollection(collection);
        this.collectionName = collection;
        if (identifierClass.equals(Integer.class)) {
            initCounterCollection();
        }
    }

    public void initCounterCollection() {
        MongoCollection<Document> collection = MongoDBManager.getInstance().getCollection(MongoCollections.ID_COUNTER);
        ArrayList<Document> counter = collection.find(Filters.eq(Docable.ID, collectionName)).into(new ArrayList<>());
        if (counter.isEmpty()) {
            // create the counter document
            Document document = new Document();
            document.put(Docable.ID, collectionName);
            document.put(SEQUENCE_VALUE, 0);
            collection.insertOne(document);
        }
    }


    /**
     * Read operations
     */
    public T readOne(I id) {
        return readOne(Filters.eq(Docable.ID, id));
    }

    public T readOne(Bson filter) {
        List<T> byId = readManyAsList(filter);
        if (byId.size() >= 1) {
            return byId.get(0);
        } else {
            return null;
        }
    }

    public List<T> readManyAsList(Bson filter) {
        return readManyAsIterable(filter).into(new ArrayList<>());
    }

    public MongoIterable<T> readManyAsIterable(Bson filter) {
        return readManyAsIterable(filter, null, null);
    }

    public MongoIterable<T> readManyAsIterable(Bson filter, Bson sort, Integer limit) {
        return readManyAsIterable(filter, sort, limit, null);
    }

    public MongoIterable<T> readManyAsIterable(Bson filter, Bson sort, Integer limit, Integer skip) {
        FindIterable<Document> documents;
        if (sort == null) {
            documents = collection.find(filter);
        } else {
            documents = collection.find(filter).sort(sort);
        }

        if (skip != null) {
            documents = documents.skip(skip);
        }

        if (limit != null) {
            documents = documents.limit(limit);
        }
        MongoIterable<T> map = documents.map(document -> {
            T t = null;
            try {
                t = objectClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            T parse = (T) t.parse(document);
            return parse;
        });

        return map;
    }

    public static MongoIterable<Document> readDocumentsAsIterable(String collectionName, Bson filter, String... includes) {
        MongoCollection<Document> collection = MongoDBManager.getInstance().getCollection(collectionName);
        return collection.find(filter).projection(Projections.include(includes));
    }

    public static List<Document> readDocumentsAsList(String collectionName, Bson filter, String... includes) {
        MongoCollection<Document> collection = MongoDBManager.getInstance().getCollection(collectionName);
        return collection.find(filter).projection(Projections.include(includes)).into(new ArrayList<>());
    }


    public List<T> readAllAsList() {
        return readManyAsList(new BasicDBObject());
    }

    public MongoIterable<T> readAllAsIterable() {
        return readManyAsIterable(new BasicDBObject());
    }

    /**
     * write operations
     * If you need to overwrite the insert method to only overwrite the insertOrUpdateMany method
     */

    public T insertOrUpdateOne(T object) {
        Collection<T> toInsert = new ArrayList<>();
        toInsert.add(object);
        Collection<T> ts = insertOrUpdateMany(toInsert);
        T first = CollectionHelper.getFirst(ts);
        return first;
    }

    public Collection<T> insertOrUpdateMany(Collection<T> objects) {
        if (this.identifierClass.equals(Integer.class)) {
            return insertOrUpdateManyWithIntegerId(objects);
        } else {
            return insertOrUpdateManyWithObjectId(objects);
        }
    }

    /**
     * the base method for all insert and update operation on conceptDocuments. No other should be used because only this
     * takes care of proper id management
     *
     * @param objects
     */
    private Collection<T> insertOrUpdateManyWithIntegerId(Collection<T> objects) {
        if (objects == null || objects.isEmpty()) {
            return objects;
        }
        List<T> toUpdate = new ArrayList<>();
        List<T> toInsert = new ArrayList<>();
        for (T object : objects) {
            Object id = object.getId();
            if (id != null) {
                toUpdate.add(object);
            } else {
                toInsert.add(object);
            }
        }
        // determine the correct upper id for the insert operation
        MongoCollection<Document> collection = MongoDBManager.getInstance().getCollection(MongoCollections.ID_COUNTER);
        Document highestIdDocument = collection.findOneAndUpdate(Filters.eq("_id", collectionName), new BasicDBObject().append("$inc",
                new BasicDBObject().append(SEQUENCE_VALUE, toInsert.size())));
        Integer highestId = highestIdDocument.getInteger(SEQUENCE_VALUE);
        int i = highestId + 1;
        List<Document> toInsertAsDocuments = new ArrayList<>();
        for (T object : toInsert) {
            object.setId(i);
            i++;
            toInsertAsDocuments.add(object.serialize());
        }
        if (!toInsertAsDocuments.isEmpty()) {
            getCollection().insertMany(toInsertAsDocuments);
        }

        if (!toUpdate.isEmpty()) {
            // delete the documents to update
            List<Integer> toDeleteIds = new ArrayList<>();
            List<Document> toUpdateAsDocuments = new ArrayList<>();
            for (T object : toUpdate) {
                toDeleteIds.add((Integer) object.getId());
                toUpdateAsDocuments.add(object.serialize());
            }
            deleteMany(Filters.in(Docable.ID, toDeleteIds));
            // insert the new ones

            getCollection().insertMany(toUpdateAsDocuments);
        }
        List<T> toReturn = new ArrayList<>();
        toReturn.addAll(toInsert);
        toReturn.addAll(toUpdate);
        return toReturn;
    }

    private Collection<T> insertOrUpdateManyWithObjectId(Collection<T> objects) {
        if (objects == null || objects.isEmpty()) {
            return objects;
        }
        List<Document> documents = new ArrayList<>();
        List<T> objectsAsList = new ArrayList<>();
        objectsAsList.addAll(objects);
        List<I> idsToDelete = new ArrayList<>();
        for (T object : objectsAsList) {
            Document serialize = object.serialize();
            Object id = serialize.get(Docable.ID);
            if (id != null) {
                idsToDelete.add((I) id);
            }
            documents.add(serialize);
        }
        deleteMany(idsToDelete);
        collection.insertMany(documents);
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            T t = objectsAsList.get(i);
            t.setId(document.get(Docable.ID));
        }
        return objects;
    }

    /**
     * update operations
     */
    public UpdateResult updateMany(Bson filter, Bson update) {
        return getCollection().updateMany(filter, update);
    }

    public UpdateResult updateOne(Bson filter, Bson update) {
        return getCollection().updateOne(filter, update);
    }

    /**
     * delete operations
     */
    public void deleteOne(I id) {
        deleteOne(Filters.eq("_id", id));
    }

    public void deleteOne(Bson filter) {
        deleteMany(filter);
    }

    public void deleteMany(ObjectId channelId) {
        Bson filter = Filters.eq("channelId", channelId);
        deleteMany(filter);
    }

    public void deleteMany(Collection<I> ids) {
        Bson filter = Filters.in("_id", ids);
        deleteMany(filter);
    }

    public void deleteMany(Bson filter) {
        collection.deleteMany(filter);
    }

    public void deleteAll() {
        deleteMany(new BasicDBObject());
    }


    /**
     * count methods
     */
    public Long count(Bson filter) {
        long l = collection.countDocuments(filter);
        return l;
    }

    public Long count() {
        long l = collection.countDocuments();
        return l;
    }

    /**
     * utilities
     */
    public List<Document> toDocumentList(Collection<T> objects) {
        List<Document> documents = new ArrayList<>();
        for (T object : objects) {
            documents.add(object.serialize());
        }
        return documents;
    }

    public List<Document> toDocumentList(MongoIterable<T> objects) {
        ArrayList<T> into = objects.into(new ArrayList<>());
        return toDocumentList(into);
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    public void setCollection(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}
