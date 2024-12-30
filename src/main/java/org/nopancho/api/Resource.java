package org.nopancho.api;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.palladian.persistence.json.JsonObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Resource {

    public static final String UNAUTHORIZED = "You are not authorized.";
    public static final String EXPIRED = "Your key has expired.";
    public static final String LIMIT_EXCEEDED = "Your API calls limit has exceeded.";

    private static final String STATUS = "status";
    private static final String DATA = "data";
    private static final String MESSAGE = "message";
    private static final String SUCCESS_STATUS = "success";
    private static final String INFO_STATUS = "info";

    @Context
    HttpServletRequest request;

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Resource.class);

    protected static final String FAILURE = "failure";

    /**
     * Customers api key mapping
     */
    private static final Map<Integer, String> API_KEY_MAPPING = new HashMap<>();

    public Resource() {

        // FIXME API key handling
        /*
        Collection<Channel> channels = ChannelManager.getInstance().getAllChannels();
        for (Channel channel: channels) {
            API_KEY_MAPPING.put(channel.getId(), customer.getApiKey());
        }

        String[] evaluationKeyArray = Controller.getConfig().getStringArray("api.evaluationKeys");
        for (String evaluationKey : evaluationKeyArray) {
            evaluationKeys.add(evaluationKey);
        }
        */
    }

    public static String getSuccessJson() {
        Document jsonObject = getSuccessDocument();
        jsonObject.put(STATUS, SUCCESS_STATUS);
        return jsonObject.toJson();
    }

    public static String getSuccessJson(String msg) {
        Document jsonObject = getSuccessDocument();
        if (msg != null) {
            jsonObject.put(MESSAGE, msg);
        }
        return jsonObject.toJson();
    }

    public static String getSuccessJson(Document data) {
        Document jsonObject = getSuccessDocument();
        jsonObject.put(DATA, data);
        return jsonObject.toJson();
    }

    public static String getSuccessJson(String msg, Document data) {
        Document jsonObject = getSuccessDocument();
        jsonObject.put(MESSAGE, msg);
        jsonObject.put(DATA, data);
        return jsonObject.toJson();
    }

    public static String getFailureJson(String msg) {
        Document jsonObject = getSuccessDocument();
        jsonObject.put("status", "failure");
        jsonObject.put(MESSAGE, msg);
        return jsonObject.toJson();
    }

    public static String getInfoJson(String msg) {
        Document jsonObject = new Document();
        jsonObject.put(STATUS, INFO_STATUS);
        jsonObject.put(MESSAGE, msg);
        return jsonObject.toJson();
    }

    public static JsonObject getFailureJson(Map<String, ? extends Object> map) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("status", FAILURE);
        if (map != null) {
            jsonObject.putAll(map);
        }
        return jsonObject;
    }

    public static Document getSuccessDocument() {
        Document jsonObject = new Document();
        jsonObject.put(STATUS, SUCCESS_STATUS);
        return jsonObject;
    }
    public static Response createOctetStreamResponse(File file, String responseFileName) {
        if (file != null && file.length() > 0) {
            Response.ResponseBuilder responseBuilder = Response.ok(file);
            responseBuilder.header("Content-Disposition", "attachment;filename=\"" + responseFileName + "\"");
            return responseBuilder.build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    protected String getRequestIp() {
        try {
            if (request != null) {
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("HTTP_CLIENT_IP");
                }
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("HTTP_X_FORWARDED_FOR");
                }
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Authenticate a request
     * 
     * @param apiKey The API key.
     * @param customerId The customer id.
     * @param fullAccess Whether the user needs full access (not just searching and suggesting)
     */
    // FIXME authentication
    public void authenticate(String apiKey, Integer customerId, boolean fullAccess) {
/*
        if (apiKey == null) {
            apiKey = "";
        }

        String queryIp = "unknown";

        if (request != null) {
            queryIp = Optional.ofNullable(getRequestIp()).orElse("unknown");
        }

        // customerId + ") with apiKey: " + apiKey + " from " + queryIp);

        if (apiKey.equalsIgnoreCase(ULTRA_KEY) || queryIp.contains("0:0:0:0:0:0:0:1")) {
            // LOGGER.debug("API call with ULTRA KEY!");
            return;
        }

        if (customerId == null) {
            throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, "Customer id not set.");
        }

        // is the key still valid?
        if (customerId > 0) {
            Customer customer = CustomerManager.getInstance().getCustomer(customerId);
            if (customer == null) {
                throw new ApplicationException(HttpStatus.SC_BAD_REQUEST, "Customer does not exist.");
            }
            if (Customer.hasExpiredKey(customer)) {
                throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, EXPIRED);
            }
        }

        String customerApiKeyMapping = API_KEY_MAPPING.get(customerId);

        if (customerApiKeyMapping == null) {
            throw new ApplicationException(HttpStatus.SC_BAD_REQUEST, "API key not valid.");
        }

        if (apiKey.equals(customerApiKeyMapping) || (!fullAccess && customerApiKeyMapping.startsWith(apiKey) && apiKey.length() == 16)) {
            return;
        }

        if (evaluationKeys.contains(apiKey)) {
            LOGGER.info("API call with EVALUATION KEY: " + apiKey + "!");
            return;
        }

        // if not allowed, throw error
        LOGGER.error("unauthorized access");
        throw new ApplicationException(HttpStatus.SC_UNAUTHORIZED, UNAUTHORIZED);
        */
    }
}