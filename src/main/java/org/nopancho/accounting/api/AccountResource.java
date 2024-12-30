package org.nopancho.accounting.api;

import com.mongodb.client.model.Filters;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.nopancho.accounting.model.User;
import org.nopancho.accounting.model.UserDto;
import org.nopancho.accounting.model.UserInvitation;
import org.nopancho.accounting.persistence.UserDao;
import org.nopancho.accounting.persistence.UserInvitationDao;
import org.nopancho.accounting.utils.KeyGenerator;
import org.nopancho.accounting.utils.MailgunSender;
import org.nopancho.api.ApplicationException;
import org.nopancho.api.SecuredResource;
import org.nopancho.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.palladian.persistence.json.JsonObject;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Path("account")
public class AccountResource extends SecuredResource implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountResource.class);
    private static final String HYPER_SECRET_SUPER_USER_KEY = "12345";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Account Resource is very ready");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    /**
     * creates a user and an organisation object and stores it to db.
     * The user gets an email with a confirmation link which then calls confirmRegistration.
     * If the organisation name is a key a super user without a organisation is created.
     *
     * @param registration
     * @return
     */
    @Path("/register")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(String registration) {
        Document parsedRegistration = Document.parse(registration);
        // check whether user with that email exists
        User user = new User().parse(parsedRegistration);
        String email = user.getEmail();
        User existingUser = UserDao.getInstance().readOne(Filters.eq(User.EMAIL, user.getEmail()));
        if (existingUser != null) {
            ApplicationException user_already_exists = new ApplicationException(HttpStatus.SC_CONFLICT, "User already exists");
            throw user_already_exists;
        }
        // create User
        String hashedPassword = KeyGenerator.hashPassword(parsedRegistration.getString(User.PASSWORD));
        user.setPassword(hashedPassword);
        // create token for user
        String confirmationCode = KeyGenerator.generateRegistrationCode();

        user.setConfirmationKey(confirmationCode);
        //save the user
        UserDao.getInstance().insertOrUpdateOne(user);

        new Thread() {
            @Override
            public void run() {
                        /*
                          Variables for template confirm-registration
                          {{appname}}
                          {{code}}
                          {{username}}
                          {{verification_page_url}}
                         */
                String confirmUrl = ConfigManager.getConfig().getString("mail.confirm_url");
                String appname = ConfigManager.getConfig().getString("appname");
                String registrationMail = ConfigManager.getConfig().getString("mail.registration");
                confirmUrl = confirmUrl.replace("<user_id>", "" + user.getId());

                JsonObject jsonObject = new JsonObject();
                jsonObject.put("username", user.getEmail());
                jsonObject.put("appname", appname);
                jsonObject.put("code", confirmationCode);
                jsonObject.put("verification_page_url", confirmUrl);
                MailgunSender.sendMailWithTemplate(registrationMail, user.getEmail(), "confirm-registration", jsonObject);
            }
        }.start();

        Document serialize = new UserDto().serialize(user);
        return success("Registration successful", serialize);
    }

    /**
     * creates a user and an organisation object and stores it to db.
     * The user gets an email with a confirmation link which then calls confirmRegistration.
     * If the organisation name is a key a super user without a organisation is created.
     *
     * @param registration
     * @return
     */
    @Path("register-invited")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String registerInvited(String registration) {
        Document parsedRegistration = Document.parse(registration);
        String email = parsedRegistration.getString(User.EMAIL);
        String confKey = parsedRegistration.getString(User.CONFIRMATION_KEY);
        // check whether the confirmation key is valid
        UserInvitation userInvitation = UserInvitationDao.getInstance().readOne(Filters.eq(User.CONFIRMATION_KEY, confKey));
        ApplicationException invalidRegistration = new ApplicationException(HttpStatus.SC_NOT_ACCEPTABLE, "Invalid registration");
        if (userInvitation == null) {
            throw invalidRegistration;
        }
        String emailInvitation = userInvitation.getEmail();
        if (!emailInvitation.equals(email)) {
            invalidRegistration = new ApplicationException(HttpStatus.SC_NOT_ACCEPTABLE, "Invalid registration");
            throw invalidRegistration;
        }

        // check whether user with that email exists
        User existingUser = UserDao.getInstance().readOne(Filters.eq(User.EMAIL, email));
        if (existingUser != null) {
            ApplicationException user_already_exists = new ApplicationException(HttpStatus.SC_NOT_ACCEPTABLE, "User already exists");
            throw user_already_exists;
        }

        // create User
        User user = new User();
        user.setEmail(email);
        user.setConfirmed(true);
        // hash the password
        String hashedPassword = KeyGenerator.hashPassword(parsedRegistration.getString(User.PASSWORD));
        user.setPassword(hashedPassword);
        //user.setConfirmationKey(parsedRegistration.getString(User.CONFIRMATION_KEY));

        // create Organisation
        String organisationId = parsedRegistration.getString("organisationId");

        //save the user
        UserDao.getInstance().insertOrUpdateOne(user);

        // confirm the invitation
        userInvitation.setAccepted(true);
        UserInvitationDao.getInstance().insertOrUpdateOne(userInvitation);

        return getSuccessJson("Registration successful", user.serialize());
    }

    /**
     * a resource used to check whether a user has confirmed his registration
     *
     * @param userId
     * @return
     */
    @Path("/status/{userId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getRegistrationStatus(@PathParam("userId") Integer userId) {
        User user = UserDao.getInstance().readOne(userId);
        if (user == null) {
            throw new ApplicationException(HttpStatus.SC_CONFLICT, "Unable to fullfill registration request. Please check your emails");
        }
        user.restrictInformation();
        return getSuccessJson(user.serialize());
    }

    /**
     * called when a registration is confirmed
     *
     * @param confirmation
     * @return
     */
    @Path("/confirm")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmRegistration(String confirmation) {
        Document parse = Document.parse(confirmation);
        String confirmationKey = parse.getString(User.CONFIRMATION_KEY);
        Integer userId = Integer.parseInt(parse.getString("userId"));
        User user = UserDao.getInstance().readOne(userId);
        if (confirmationKey.equals(user.getConfirmationKey())) {
            user.setConfirmed(true);
            UserDao.getInstance().insertOrUpdateOne(user);

            new Thread() {
                @Override
                public void run() {
                    /*
                     * variables in template confirm-registration-successful:
                     * {{appname}}
                     * {{username}}
                     * {{support_mail}}
                     */

                    String appname = ConfigManager.getConfig().getString("appname");
                    String supportMail = ConfigManager.getConfig().getString("mail.support");
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.put("appname", appname);
                    jsonObject.put("username", user.getEmail());
                    jsonObject.put("support_mail", supportMail);
                    MailgunSender.sendMailWithTemplate(supportMail, user.getEmail(), "confirm-registration-successful", jsonObject);
                }
            }.start();
            return success("User successfully confirmed", new UserDto().serialize(user));
        } else {
            return error(HttpStatus.SC_UNAUTHORIZED, "invalid confirmation code", null);
        }
    }

    /**
     * resource to generate a reset password request by generating a key and sending it via email
     *
     * @param email
     * @return
     */
    @Path("/forgot-password")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response forgotPassword(String email) {
        Document parse = Document.parse(email);
        String emailAdress = parse.getString(User.EMAIL);
        User user = UserDao.getInstance().readOne(Filters.eq(User.EMAIL, emailAdress));
        if (user != null) {

            String forgotPasswordKey = KeyGenerator.generateKey();
            user.setForgotPasswordKey(forgotPasswordKey);

            UserDao.getInstance().insertOrUpdateOne(user);
            new Thread() {
                @Override
                public void run() {
                    /*
                     * variables in template reset-password:
                     * {{appname}}
                     * {{reset_password_page_url}}
                     * {{support_mail}}
                     */

                    String resetUrl = ConfigManager.getConfig().getString("mail.reset_url");
                    resetUrl = resetUrl.replace("<user_id>", "" + user.getId()).replace("<forgot_password_key>", forgotPasswordKey);
                    String appname = ConfigManager.getConfig().getString("appname");
                    String supportMail = ConfigManager.getConfig().getString("mail.support");
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.put("appname", appname);
                    jsonObject.put("reset_password_page_url", resetUrl);
                    jsonObject.put("support_mail", supportMail);
                    MailgunSender.sendMailWithTemplate(supportMail, user.getEmail(), "reset-password", jsonObject);
                }
            }.start();
        }
        // even though no reset password mail is sent we return success since the user
        // should not get information about existing users
        return success("An email with a link to reset your password was sent to " + email, null);
    }

    /**
     * resource to reset a password by a before created reset key
     *
     * @param reset
     * @return
     */
    @Path("/reset")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(String reset) {
        Document parse = Document.parse(reset);
        String newPassword = parse.getString("newPassword");
        String passwordKey = parse.getString(User.FORGOT_PASSWORD_KEY);
        Integer userId = Integer.parseInt(parse.getString("userId"));
        User user = UserDao.getInstance().readOne(userId);
        if (user == null) {
            return error(HttpStatus.SC_UNAUTHORIZED, "Invalid reset password request", null);
        }
        String forgotPasswordKeyExisting = user.getForgotPasswordKey();
        if (!forgotPasswordKeyExisting.equals(passwordKey)) {
            return error(HttpStatus.SC_UNAUTHORIZED, "Invalid reset password request", null);
        }
        String hashedPassword = KeyGenerator.hashPassword(newPassword);
        user.setPassword(hashedPassword);
        UserDao.getInstance().insertOrUpdateOne(user);

        new Thread() {
            @Override
            public void run() {
                /*
                 * variables in template confirm-reset-password:
                 * {{appname}}
                 * {{username}}
                 * {{support_mail}}
                 */

                String appname = ConfigManager.getConfig().getString("appname");
                String supportMail = ConfigManager.getConfig().getString("mail.support");
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("appname", appname);
                jsonObject.put("support_mail", supportMail);
                jsonObject.put("username", user.getEmail());
                MailgunSender.sendMailWithTemplate(supportMail, user.getEmail(), "confirm-reset-password", jsonObject);
            }
        }.start();
        return success("password successfully resetted", null);
    }

    /**
     * login via email and password. Called when the user has no token
     *
     * @return
     */
    @Path("/login")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@FormParam("email") String email, @FormParam("password") String password) {

        LOGGER.info("received login request ");
        User user = UserDao.getInstance().readOne(Filters.eq(User.EMAIL, email));
        boolean loginSuccessful = true;
        if (user == null) {
            loginSuccessful = false;
        } else {
            String existingPassword = user.getPassword();
            if (!existingPassword.equals(KeyGenerator.hashPassword(password))) {
                loginSuccessful = false;
            }
            Boolean confirmed = user.getConfirmed();
            if (confirmed == false) {
                loginSuccessful = false;
            }
        }
        if (!loginSuccessful) {
            // Get the original URL
            String loginUrl = ConfigManager.getConfig().getString("login.url");

            // Append error parameter
            String redirectUrl = loginUrl + (loginUrl.contains("?") ? "&" : "?") + "error=true";

            // Redirect back to the original URL with error param
            URI redirectUri = UriBuilder.fromUri(redirectUrl).build();
            return Response.seeOther(redirectUri).build();
        }

        // create the token

        String jwt = createToken(user);
        Document response = new Document();
        response.put(User.TOKEN, jwt);
        user.restrictInformation();
        response.put(User.USER, user.serialize());
        String login_successful = getSuccessJson("login successful", response);
        String url = ConfigManager.getConfig().getString("app.url");
        String domain = ConfigManager.getConfig().getString("app.domain");
        NewCookie sessionCookie = new NewCookie("token",        // Cookie name
                jwt,            // Cookie value
                "/",            // Path
                domain, // Domain (set explicitly)
                null,           // Comment
                NewCookie.DEFAULT_MAX_AGE, // Max age (use default or custom)
                true,          // Secure flag (set true if using HTTPS)
                false            // HttpOnly flag (for security)
        );

        URI redirectUri = UriBuilder.fromUri(url).build();
        LOGGER.info("redirecting to " + url);
        return Response.seeOther(redirectUri).cookie(sessionCookie).build();
    }

    /**
     * login via token. The token is always refreshed so that encoded roles are up to date in the token
     *
     * @return
     */
    @Path("/login")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginWithToken() {
        LOGGER.info("received login with token request");
        User user = getUserFromRequest();
        LOGGER.info("login with token succeeded");
        return success("login successful", new UserDto().serialize(user));
    }

    /**
     * a get endpoint to check the health of this resource
     */
    @Path("/health")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response healthcheck() {
        return success("Account Resource working", null);
    }

}
