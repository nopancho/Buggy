package org.nopancho.accounting.persistence;

import org.nopancho.accounting.model.UserInvitation;
import org.nopancho.persistence.MongoCollections;
import org.nopancho.persistence.MongoDao;
import org.bson.types.ObjectId;

public class UserInvitationDao extends MongoDao<UserInvitation, ObjectId> {

    private static UserInvitationDao INSTANCE = null;

    public static UserInvitationDao getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new UserInvitationDao();
        }
        return INSTANCE;
    }

    public UserInvitationDao() {
        super(MongoCollections.USER_INVITATIONS, UserInvitation.class, ObjectId.class);
    }
}
