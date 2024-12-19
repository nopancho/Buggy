package org.nopancho.accounting.model;

import org.nopancho.persistence.model.Docable;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Organisation implements Docable<Organisation, ObjectId> {

    private ObjectId id;
    private String name;


    @Override
    public Document serialize() {
        Document document = new Document();
        ObjectId id = getId();
        if(id != null) {
            document.put(ID, id);
        }
        document.put(NAME, getName());
        return document;
    }

    @Override
    public Organisation parse(Document document) {
        ObjectId objectId = document.getObjectId(ID);
        this.setId(objectId);
        this.setName(document.getString(NAME));
        return this;
    }

    @Override
    public ObjectId getId() {
        return id;
    }

    @Override
    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
