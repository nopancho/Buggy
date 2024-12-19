package org.nopancho.accounting.api;

import org.nopancho.accounting.model.Organisation;
import org.nopancho.accounting.persistence.OrganisationDao;
import org.nopancho.api.SecuredResource;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("organisations")
public class OrganisationResource extends SecuredResource implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    @Path("/{organisationId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getOrganisation(@PathParam("organisationId") String organisationId) {
        Organisation organisation = OrganisationDao.getInstance().readOne(new ObjectId(organisationId));
        if(organisation == null) {
            throw NOT_EXISTING_RESOURCE;
        }
        return getSuccessJson(organisation.serialize());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String saveOrganisation(String organisation) {
        Document parse = Document.parse(organisation);
        Document organisationJson = (Document) parse.get("organisation");
        Organisation organisationObject = new Organisation().parse(organisationJson);
        OrganisationDao.getInstance().insertOrUpdateOne(organisationObject);
        return getSuccessJson("Organisation Settings saved");
    }
}
