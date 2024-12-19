package org.nopancho.logger;


import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;

import static org.nopancho.logger.model.MarkerType.*;

public class LogbackExample {
    public static Logger LOGGER = LoggerFactory.getLogger(LogbackExample.class);
    public static void logTest(){
        ArrayList<String> entityIds = new ArrayList<>();
        entityIds.add("ABC");
        entityIds.add("DEF");
        ArrayList<Integer> collectionIds = new ArrayList<>();
        collectionIds.add(12);
        collectionIds.add(23);

        MDC.put("accountId", "324564234438274238967243786");
        LOGGER.info("some");
        LOGGER.info("System ready");
        LOGGER.warn("Attention: Sysout coming:");
        System.out.println("This is a Sysout message!");
        LOGGER.info("Updated customer", StructuredArguments.kv(MT_USER_NAME, "someone@somewhere.com"));
        LOGGER.info("Updated {} product(s) for customer {} in {} minutes with articleNumbers: {}",  StructuredArguments.value(MT_ACCOUNT_ID, 1234), StructuredArguments.value(MT_USER_NAME, "someone"), StructuredArguments.value(MT_REQUEST_TIME, 3.5), StructuredArguments.value(MT_ENTITY_IDS, entityIds), StructuredArguments.kv(COLLECTION_IDS, collectionIds), MSG_TO_USER);

        LOGGER.error("Stacktrace test", new NullPointerException("NullError"));
    }
    public static void main(String[] args) {
        logTest();
    }
}
