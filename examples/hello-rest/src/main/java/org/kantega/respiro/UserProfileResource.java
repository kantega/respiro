/*
 * Copyright 2019 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro;

import org.kantega.respiro.api.mail.MailSender;
import org.kantega.respiro.api.mail.Message;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

/**
 *
 */
@Path("userprofiles")
@RolesAllowed("innholdsprodusent")
public class UserProfileResource {


    private final UsersDAO dao;
    private final MailSender email;

    public UserProfileResource(UsersDAO dao, MailSender email) {
        this.dao = dao;
        this.email = email;
    }


    @GET
    @Produces({"application/json", "application/xml"})
    @Path("{username}")
    public UserProfile getProfile(@Size(min = 3) @PathParam("username") String username,
                                  @Context SecurityContext securityContext) {

        String fullName = dao.findName(username.toUpperCase());
        UserProfile prof = new UserProfile();
        prof.setUsername(username);
        prof.setFullName(fullName);

        email.send(
                new Message(String.format("User %s looked up by %s", prof.getFullName(), securityContext.getUserPrincipal().getName()))
                        .html(true)
                        .body("<ul><li>User lookup for user " + prof.getFullName() + "</ul></li>")
                        .plainTextBody("User lookup for user " + prof.getFullName())
                        .to("Someuser<"+prof.getUsername() + "@example.ru>"));

        return prof;
    }

    @POST
    @Produces({"application/json", "application/xml"})
    @Consumes("text/plain")
    @Path("{username}")
    public UserProfile postFullname(@Size(min = 3) @PathParam("username") String username,
                                    String fullName,
                                    @Context SecurityContext securityContext) {

        return getProfile(username, securityContext);
    }

}
