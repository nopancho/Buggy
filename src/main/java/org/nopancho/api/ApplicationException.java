package org.nopancho.api;


import ws.palladian.persistence.json.JsonObject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public class ApplicationException extends WebApplicationException {

    private static final long serialVersionUID = 4425663794311181584L;

    /**
     * Create a HTTP exception including a status code and a message.
     * 
     * @param message the String that is the entity of the response.
     */
    public ApplicationException(int errorCode, String message) {
        super(Response.status(errorCode).entity(makeFailureJson(errorCode, message).toString()).type(MediaType.APPLICATION_JSON).build());
    }

    public ApplicationException(int errorCode, String message, Map<String, ? extends Object> map) {
        super(Response.status(errorCode).entity(makeFailureJson(errorCode, message, map).toString()).type(MediaType.APPLICATION_JSON).build());
    }

    private static JsonObject makeFailureJson(int errorCode, String message) {
        return makeFailureJson(errorCode, message, null);
    }

    private static JsonObject makeFailureJson(int errorCode, String message, Map<String, ? extends Object> map) {
        JsonObject jsonObject = Resource.getFailureJson(map);
        jsonObject.put("code", errorCode);
        jsonObject.put("message", message);
        return jsonObject;
    }

}