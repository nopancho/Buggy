package org.nopancho.accounting.api;

import com.mongodb.client.model.Filters;
import org.nopancho.accounting.model.Rights;
import org.nopancho.accounting.model.User;
import org.nopancho.accounting.persistence.UserDao;
import org.nopancho.api.SecuredResource;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("users")
public class UserResource extends SecuredResource implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }



    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteUser(@QueryParam("userId") Integer userId) {
        User user = UserDao.getInstance().readOne(userId);
        if(user == null) {
            throw NOT_EXISTING_RESOURCE;
        }
        UserDao.getInstance().deleteOne(userId);
        return getSuccessJson("User deleted");
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String updateUsers(String usersToUpdate) {
        Document parse = Document.parse(usersToUpdate);
        List<Document> userDocuments = (List<Document>) parse.get("users");
        List<User> users = new ArrayList<>();
        for (Document userDocument : userDocuments) {
            User user = new User().parse(userDocument);
            users.add(user);
        }
        UserDao.getInstance().insertOrUpdateMany(users);
        // only accessible with organisation read rights
        return getSuccessJson("User updated");
    }


    @Path("/organisation/{organisationId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getUsersOfOrganisation(@PathParam("organisationId") String organisationId) {
        // only accessible with organisation read rights
//        authenticate(AccessStructure.ORGANISATION, new ObjectId(organisationId), Rights.READ);
//
//        List<User> users = UserDao.getInstance().readManyAsList(Filters.eq(User.ORGANISATION_ASSIGNMENTS + "."+ UserRightsAssignment.STRUCTURE_ID, new ObjectId(organisationId)));
//
//        List<Document> userDocuments = new ArrayList<>();
//        for (User value : users) {
//            Document serialize = value.serialize();
//            userDocuments.add(serialize);
//        }
//        Document response = new Document();
//        response.put("users", userDocuments);
//        return getSuccessJson(users.size()+" loaded", response);
        return null;
    }

    @Path("/channel/{channelId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getUsersOfChannel(@PathParam("channelId") String channelId) {
//        // only accessible with organisation read rights
//        List<User> users = UserDao.getInstance().readManyAsList(Filters.eq(User.CHANNEL_ASSIGNMENTS + "."+UserRightsAssignment.STRUCTURE_ID, new ObjectId(channelId)));
//        List<Document> userDocuments = new ArrayList<>();
//        for (User value : users) {
//            Document serialize = value.serialize();
//            userDocuments.add(serialize);
//        }
//        Document response = new Document();
//        response.put("users", userDocuments);
//        return getSuccessJson(users.size()+" loaded", response);
        return null;
    }

    public static void main(String[] args) {
    }
}
