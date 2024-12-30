package org.nopancho.accounting.utils;

import org.nopancho.config.ConfigManager;
import ws.palladian.helper.nlp.StringHelper;
import ws.palladian.persistence.json.JsonObject;
import ws.palladian.retrieval.HttpResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * This class ...
 *
 * @author Sebastian Sprenger
 * @since 22.12.2024 at 08:57
 */
public class MailgunSender {
    public static boolean sendMailWithTemplate(String from, String to, String template, JsonObject variables) {
        String apiKey = ConfigManager.getConfig().getString("mail.apikey");
        apiKey =  "api:"+apiKey;
        apiKey = Base64.getEncoder().encodeToString(apiKey.getBytes(StandardCharsets.UTF_8));
        JsonObject body = new JsonObject();
        body.put("from", from);
        body.put("to", to);
        //welcome to buggy- confirm your registration
        body.put("template", template);
        body.put("h:X-Mailgun-Variables", variables.toString());
        String authHeader = "Basic " + apiKey;
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", authHeader);
        String url = "https://api.eu.mailgun.net/v3/nopancho.com/messages";
        HttpResult post = ApiUtils.postForm(url, body, header);
        String stringContent = post.getStringContent();
        System.out.println(stringContent);
        return true;
    }

    public static void main(String[] args) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("user_name", "Horst");
        jsonObject.put("app_name", "Buggy");
        jsonObject.put("code", "12346");
        jsonObject.put("from", "12346");
        jsonObject.put("verification_page_url", "https://login-buggy.nopancho.com/#/confirm-signup?userId=5");
        MailgunSender.sendMailWithTemplate("registration@buggy.nopancho.com", "s79sprenger@gmail.com", "confirm-registration", jsonObject);
    }
}
