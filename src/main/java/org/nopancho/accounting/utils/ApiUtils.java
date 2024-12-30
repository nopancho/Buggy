package org.nopancho.accounting.utils;

import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.palladian.persistence.json.JsonObject;
import ws.palladian.retrieval.*;

import java.util.Map;
import java.util.Set;

/**
 * This class ...
 *
 * @author Sebastian Sprenger
 * @since 22.12.2024 at 08:54
 */
public class ApiUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUtils.class);

    public static HttpResult post(String url, String body, Map<String, String> header) {
        HttpRetriever retriever = HttpRetrieverFactory.getHttpRetriever();
        HttpRequest2Builder putProductRequestBuilder = new HttpRequest2Builder(HttpMethod.POST, url);
        putProductRequestBuilder.setEntity(new StringHttpEntity(body, ContentType.APPLICATION_JSON.getMimeType()));

        putProductRequestBuilder.addHeaders(header);
        HttpRequest2 putProductRequest = putProductRequestBuilder.create();
        try {
            HttpResult execute = retriever.execute(putProductRequest);
            return execute;
        } catch (HttpException e) {
            LOGGER.error("Exception while performing post operation", e);
        }
        return null;
    }

    public static HttpResult postForm(String url, JsonObject body, Map<String, String> header) {
        HttpRetriever retriever = HttpRetrieverFactory.getHttpRetriever();
        HttpRequest2Builder putProductRequestBuilder = new HttpRequest2Builder(HttpMethod.POST, url);
        putProductRequestBuilder.addHeaders(header);

        MultipartHttpEntity.Builder entityBuilder = new MultipartHttpEntity.Builder();
        Set<Map.Entry<String, Object>> entries = body.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            String value = (String) entry.getValue();
            entityBuilder.addPart(new StringHttpEntity(value, ContentType.TEXT_PLAIN), key, null);
        }
        entityBuilder.addPart(new StringHttpEntity("{\"foo\": \"bar\"}", "application/json"), "json", null);
        MultipartHttpEntity multipartHttpEntity = entityBuilder.create();
        putProductRequestBuilder.setEntity(multipartHttpEntity);
        HttpRequest2 request2 = putProductRequestBuilder.create();
        try {
            HttpResult execute = retriever.execute(request2);
            return execute;
        } catch (HttpException e) {
            LOGGER.error("Exception while performing post operation", e);
        }
        return null;
    }
}
