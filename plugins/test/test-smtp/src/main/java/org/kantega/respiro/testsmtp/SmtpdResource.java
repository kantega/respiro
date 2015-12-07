/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro.testsmtp;

import com.dumbster.smtp.MailStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
@Path("dummy_smtpd")
public class SmtpdResource {
    // Disable metrics for this resource
    public static boolean METRICS = false;

    private final MailStore mailStore;

    public SmtpdResource(MailStore mailStore) {
        this.mailStore = mailStore;
    }

    @GET
    @Path("messages")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MessageJson> getMessages(@QueryParam("to") String to) {
        if( to != null)
            return Stream.of(mailStore.getMessages()).map(MessageJson::new).filter(e -> e.getTo().equals(to)).collect(Collectors.toList());

        return Stream.of(mailStore.getMessages()).map(MessageJson::new).collect(Collectors.toList());

    }

    @DELETE
    @Path("messages")
    public void delete() {
        mailStore.clearMessages();
    }

}
