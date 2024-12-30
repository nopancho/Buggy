package org.nopancho.accounting.model;

import org.nopancho.persistence.model.Docable;
import org.bson.Document;
import org.bson.types.ObjectId;

public class UserInvitation implements Docable<UserInvitation, ObjectId> {

    public static final String ORGANISATION_ID = "organisationId";
    public static final String ACCEPTED = "accepted";
    private ObjectId id;
    private String email;
    private String confirmationKey;
    private ObjectId organisationId;
    private Boolean accepted = false;

    @Override
    public Document serialize() {
        Document document = new Document();
        ObjectId id = this.getId();
        if(id != null) {
            document.put(Docable.ID, id);
        }
        document.put(User.EMAIL, getEmail());
        document.put(User.CONFIRMATION_KEY, getConfirmationKey());
        document.put(ORGANISATION_ID, getOrganisationId());
        document.put(ACCEPTED, getAccepted());
        if(this.id != null) {
            document.put(Docable.ID, getId());
        }
        return document;
    }

    @Override
    public UserInvitation parse(Document document) {
        ObjectId id = document.getObjectId("_id");
        setId(id);
        this.setEmail(document.getString(User.EMAIL));
        String confKey = document.getString(User.CONFIRMATION_KEY);
        ObjectId organisationId = document.getObjectId(ORGANISATION_ID);
        Boolean accepted = document.getBoolean(ACCEPTED);
        this.setAccepted(accepted);
        this.setOrganisationId(organisationId);
        this.setConfirmationKey(confKey);
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getConfirmationKey() {
        return confirmationKey;
    }

    public void setConfirmationKey(String confirmationKey) {
        this.confirmationKey = confirmationKey;
    }

    public ObjectId getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(ObjectId organisationId) {
        this.organisationId = organisationId;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }
}
