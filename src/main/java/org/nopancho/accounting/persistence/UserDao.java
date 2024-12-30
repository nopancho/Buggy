package org.nopancho.accounting.persistence;

import org.nopancho.accounting.model.User;
import org.nopancho.persistence.MongoCollections;
import org.nopancho.persistence.MongoDao;

public class UserDao extends MongoDao<User, Integer> {

    private static UserDao INSTANCE = null;
    public static UserDao getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new UserDao();
        }
        return INSTANCE;
    }
    public UserDao() {
        super(MongoCollections.USERS, User.class, Integer.class);
    }
}
