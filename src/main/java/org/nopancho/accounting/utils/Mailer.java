package org.nopancho.accounting.utils;

import org.nopancho.config.ConfigManager;

import javax.mail.Message;
import java.util.*;


public class Mailer extends ws.palladian.helper.Mailer {


    public Mailer() {
        super(makeProps(), ConfigManager.getConfig().getString("mail.user"), "mail.password");
    }

    private static final Properties makeProps() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP server
        props.put("mail.smtp.port", "587"); // Use STARTTLS port
        props.put("mail.smtp.auth", "true"); // Enable authentication
        props.put("mail.smtp.starttls.enable", "true"); // Use STARTTLS (modern and secure)
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");// Enforce STARTTLS
        return props;
    }

    @Override
    public boolean sendMail(String sender, String recipient, String subject, String text) {
        return super.sendMail(sender, recipient, subject, text);
    }

    /**
     * Send an e-mail to the user with a link to the confirmation site
     *
     */
    public static boolean sendConfirmationEmail(String recepientAdress, String recepientName, String senderAdress, String senderName,  String registrationCode, String userId, String domainBase, String appname) {
        if (recepientAdress!= null && registrationCode != null) {
            List<String> recipientList = Arrays.asList(recepientAdress);
            Map<Message.RecipientType, List<String>> recipients = new HashMap<>();
            recipients.put(Message.RecipientType.TO, recipientList);

            if (!domainBase.endsWith("/")) {
                domainBase += "/";
            }
            String link = domainBase + "#/confirm-registration/" +userId;
            String subject = "Your Registration at "+appname;
            StringBuilder content = new StringBuilder();
            String intro = "Hey there,\n thank you for your registration. Please complete your registration by navigating to the following url:\n";
            String linkText = "<a href=\"" + link + "\" >"+link+"</a>";
            String registrationText = "\nYour code is:\n"+registrationCode;
            content.append(intro).append(linkText).append(registrationText).append("\n\nKind regards,\n"+senderName);

            Mailer mailer = new Mailer();
            boolean sendMail = mailer.sendMail(senderAdress, senderName, recipients, subject, content.toString(), true, new ArrayList<>(), new ArrayList<>());
            return sendMail;
        }
        return false;
    }

    public static boolean sendForgotPasswordEmail(String recepientAdress, String recepientName, String senderAdress, String senderName,  String confirmationCode, String userId, String domainBase, String appname, String name) {
        if (recepientAdress!= null && confirmationCode != null) {
            List<String> recipientList = Arrays.asList(recepientAdress);
            Map<Message.RecipientType, List<String>> recipients = new HashMap<>();
            recipients.put(Message.RecipientType.TO, recipientList);
            if (!domainBase.endsWith("/")) {
                domainBase += "/";
            }
            String link = domainBase + "#/reset-password/" +userId+"/"+ confirmationCode;
            String subject = "Password reset at "+appname;
            String content = "Hallo "+name+",\n Your password has been resettet. You can set your new password navigating to the following url with your browser:\n<a href=\"" + link + "\" >"+link+"</a>\n\nKind regards,\n"+senderName;

            Mailer mailer = new Mailer();
            boolean sendMail = mailer.sendMail(senderAdress, senderName, recipients, subject, content, true, new ArrayList<>(), new ArrayList<>());
            return sendMail;
        }
        return false;
    }

    public static void main(String[] args) {
    }
}