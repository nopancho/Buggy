package org.nopancho.accounting.api;

import com.mongodb.client.model.Filters;
import org.nopancho.accounting.model.*;
import org.nopancho.accounting.persistence.UserDao;
import org.nopancho.accounting.persistence.UserInvitationDao;
import org.nopancho.accounting.utils.KeyGenerator;
import org.nopancho.api.ApplicationException;
import org.nopancho.api.SecuredResource;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.nopancho.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        // FIXME the mail sender doesn't work so I set the registration code to 111111
        confirmationCode = "111111";
        user.setConfirmationKey(confirmationCode);
        //save the user
        UserDao.getInstance().insertOrUpdateOne(user);

//        new Thread() {
//            @Override
//            public void run() {
//                Mailer.sendConfirmationEmail(email, confirmationCode, user.getId().toString(), ConfigManager.getConfig().getString("app"), ConfigManager.getConfig().getString("appname"), user.getFirstName() + " " + user.getLastName(), ConfigManager.getConfig().getString("company"));
//            }
//        }.start();

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
        }
        //TODO exception handling
        UserDao.getInstance().insertOrUpdateOne(user);

        return success("User successfully confirmed", new UserDto().serialize(user));
    }

    /**
     * resource to generate a reset password request by generating a key and sending it via email
     *
     * @param email
     * @return
     */
    @Path("/forgot")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String forgotPassword(String email) {
        Document parse = Document.parse(email);
        String emailAdress = parse.getString(User.EMAIL);
        User user = UserDao.getInstance().readOne(Filters.eq(User.EMAIL, emailAdress));
        if (user == null) {
            throw new ApplicationException(HttpStatus.SC_NOT_ACCEPTABLE, "There is no user with that e-mail adress");
        }
        String forgotPasswordKey = KeyGenerator.generateKey();
        user.setForgotPasswordKey(forgotPasswordKey);

        UserDao.getInstance().insertOrUpdateOne(user);
        user.restrictInformation();
//        new Thread() {
//            @Override
//            public void run() {
//                Mailer.sendForgotPasswordEmail(emailAdress, forgotPasswordKey, user.getId().toString(), ConfigManager.getConfig().getString("app"), ConfigManager.getConfig().getString("appname"), user.getFirstName() + " " + user.getLastName(), ConfigManager.getConfig().getString("company"));
//            }
//        }.start();

        return getSuccessJson(user.serialize());
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
    public String resetPassword(String reset) {
        Document parse = Document.parse(reset);
        String newPassword = parse.getString("new_password");
        String passwordKey = parse.getString(User.FORGOT_PASSWORD_KEY);
        Integer userId = parse.getInteger("userId");
        User user = UserDao.getInstance().readOne(userId);
        if (user == null) {
            throw new ApplicationException(HttpStatus.SC_NOT_ACCEPTABLE, "Unknown user");
        }
        String forgotPasswordKeyExisting = user.getForgotPasswordKey();
        if (!forgotPasswordKeyExisting.equals(passwordKey)) {
            throw new ApplicationException(HttpStatus.SC_NOT_ACCEPTABLE, "Invalid reset password request");
        }
        String hashedPassword = KeyGenerator.hashPassword(newPassword);
        user.setPassword(hashedPassword);
        UserDao.getInstance().insertOrUpdateOne(user);
        return getSuccessJson("password successfully resetted");
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
        if (user == null) {
            return error(HttpStatus.SC_UNAUTHORIZED, "E-mail or password are not correct", null);
        }

        String existingPassword = user.getPassword();
        if (!existingPassword.equals(KeyGenerator.hashPassword(password))) {
            return error(HttpStatus.SC_UNAUTHORIZED, "E-mail or password are not correct", null);
        }
        Boolean confirmed = user.getConfirmed();
        if (confirmed == false) {
            return error(HttpStatus.SC_UNAUTHORIZED, "You have to verify your account first", null);
        }

        // create the token

        String jwt = createToken(user);
        Document response = new Document();
        response.put(User.TOKEN, jwt);
        user.restrictInformation();
        response.put(User.USER, user.serialize());
        String login_successful = getSuccessJson("login successful", response);
        String url = ConfigManager.getConfig().getString("app.url");
        NewCookie sessionCookie = new NewCookie(
                "token",        // Cookie name
                jwt,            // Cookie value
                "/",            // Path
                url, // Domain (set explicitly)
                null,           // Comment
                NewCookie.DEFAULT_MAX_AGE, // Max age (use default or custom)
                true,          // Secure flag (set true if using HTTPS)
                true            // HttpOnly flag (for security)
        );


        URI redirectUri = UriBuilder.fromUri(url).build();
        LOGGER.info("redirecting to "+url);
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
}
