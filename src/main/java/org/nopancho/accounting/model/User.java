package org.nopancho.accounting.model;

import org.nopancho.persistence.model.Docable;
import org.bson.Document;
import org.bson.types.ObjectId;
import ws.palladian.helper.math.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User implements Docable<User, Integer> {
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String TOKEN = "token";
    public static final String CONFIRMATION_KEY = "confirmationKey";
    public static final String CONFIRMED = "confirmed";
    public static final String FORGOT_PASSWORD_KEY = "forgotPasswordKey";
    public static final String ORGANISATION_ASSIGNMENTS = "organisationAssignment";
    public static final String CHANNEL_ASSIGNMENTS = "channelAssignments";
    public static final String SUPER_USER = "super_user";
    public static final String USER = "user";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";

    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String token;
    private Boolean confirmed = false;
    private String forgotPasswordKey = null;
    private Boolean superUser = false;

    private String registrationCode = null;


    private String confirmationKey;

    private enum UserColors {
        BLUE_GREY, DEEP_ORANGE, GREEN, TEAL, CYAN, BLUE, PINK, INDIGO, DEEP_PURPLE,
    }


    public User() {
        UserColors[] values = UserColors.values();
        UserColors randomEntry = MathHelper.getRandomEntry(Arrays.asList(values));
    }

    @Override
    public Document serialize() {
        Document document = new Document();
        Integer id = getId();
        if (id != null) {
            document.put(ID, id);
        }
        document.put(EMAIL, getEmail());
        document.put(FIRST_NAME, getFirstName());
        document.put(LAST_NAME, getLastName());
        document.put(PASSWORD, getPassword());
        document.put(CONFIRMED, getConfirmed());
        document.put(CONFIRMATION_KEY, getConfirmationKey());
        document.put(TOKEN, getToken());
        document.put(FORGOT_PASSWORD_KEY, getForgotPasswordKey());
        document.put(SUPER_USER, getSuperUser());
        List<Document> projectAssignments = new ArrayList<>();
        return document;
    }

    @Override
    public User parse(Document document) {
        Integer id = document.getInteger(Docable.ID);
        if (id != null) {
            setId(id);
        }
        this.setFirstName(document.getString(FIRST_NAME));

        this.setLastName(document.getString(LAST_NAME));

        String password = document.getString(PASSWORD);
        this.setPassword(password);
        this.setEmail(document.getString(EMAIL));
        String token = document.getString(TOKEN);
        this.setToken(token);
        Boolean confirmed = document.getBoolean(CONFIRMED);
        this.setConfirmed(confirmed);
        String forgotPasswordKey = document.getString(FORGOT_PASSWORD_KEY);
        this.setForgotPasswordKey(forgotPasswordKey);
        this.setConfirmationKey(document.getString(CONFIRMATION_KEY));
        Boolean superUser = document.getBoolean(SUPER_USER);
        this.setSuperUser(superUser);
        return this;
    }

    public void restrictInformation() {
        this.setPassword(null);
        this.setToken(null);
        this.setForgotPasswordKey(null);
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }


    public String getForgotPasswordKey() {
        return forgotPasswordKey;
    }

    public void setForgotPasswordKey(String forgotPasswordKey) {
        this.forgotPasswordKey = forgotPasswordKey;
    }


    public Boolean getSuperUser() {
        return superUser;
    }

    public void setSuperUser(Boolean superUser) {
        this.superUser = superUser;
    }


    public String getConfirmationKey() {
        return confirmationKey;
    }

    public void setConfirmationKey(String confirmationKey) {
        this.confirmationKey = confirmationKey;
    }
}
