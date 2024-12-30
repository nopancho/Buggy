package org.nopancho.persistence.model;

import org.bson.Document;

/**
 * Created by ssprenger on 06.04.2019.
 */
public interface Docable<T, I> {
    public static final String ID = "_id";
    public static final String NAME = "name";

    public Document serialize();

    public T parse(Document document);

    public I getId();
    public void setId(I id);
}
