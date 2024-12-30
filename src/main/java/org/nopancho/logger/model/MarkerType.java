package org.nopancho.logger.model;

import net.logstash.logback.argument.StructuredArgument;

import static net.logstash.logback.argument.StructuredArguments.kv;

public class MarkerType {
    public static final StructuredArgument MSG_TO_USER = kv("userMessage", true);

    public static final String MT_USER_NAME = "USER_NAME";
    public static final String MT_ACCOUNT_ID = "CUSTOMER_NAME";
    public static final String MT_REQUEST_TIME = "REQUEST_TIME";
    public static final String MT_ENTITY_IDS = "ENTITY_IDS";
    public static final String COLLECTION_IDS = "COLLECTION_IDS";

}
