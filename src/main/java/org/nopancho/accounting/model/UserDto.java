package org.nopancho.accounting.model;

import org.bson.Document;
import org.nopancho.core.models.Dto;


/**
 * This class ...
 *
 * @author Sebastian Sprenger
 * @since 23.11.2024 at 18:39
 */
public class UserDto extends Dto<User> {

    @Override
    public Document serialize(User user) {
        Document document = new Document();
        document.put(User.EMAIL, user.getEmail());
        document.put(User.ID, user.getId());
        return document;
    }
}
