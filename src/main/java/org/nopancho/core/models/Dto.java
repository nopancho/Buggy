package org.nopancho.core.models;

import org.bson.Document;

/**
 * This class ...
 *
 * @author Sebastian Sprenger
 * @since 23.11.2024 at 20:20
 */
public abstract class Dto<T> {
    private T t;
    public abstract Document serialize(T t);

}
