package org.nopancho.accounting.persistence;

import org.nopancho.accounting.model.Organisation;
import org.nopancho.persistence.MongoCollections;
import org.nopancho.persistence.MongoDao;
import org.bson.types.ObjectId;

public class OrganisationDao extends MongoDao<Organisation, ObjectId> {
    private static OrganisationDao INSTANCE = null;

    public static OrganisationDao getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new OrganisationDao();
        }
        return INSTANCE;
    }
    public OrganisationDao() {
        super(MongoCollections.ORGANISATIONS, Organisation.class, ObjectId.class);
    }
}
