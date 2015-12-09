package org.kantega.respiro.ui.resources;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

/**
 *
 */
@Path("respiro/userprofile")
public class UserProfileResource {

    // Disable metrics for this resource
    public static boolean METRICS = false;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public UserProfile get(@Context  SecurityContext securityContext) {

        return new UserProfile(securityContext.getUserPrincipal());
    }
}
