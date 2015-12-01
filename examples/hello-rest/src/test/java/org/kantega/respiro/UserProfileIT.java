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

package org.kantega.respiro;

import org.kantega.respiro.testsmtp.MessageJson;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import java.util.List;

import static org.kantega.respiro.test.Utils.getReststopPort;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by helaar on 20.10.2015.
 */
public class UserProfileIT {

    private WebTarget target;

    @Before
    public void before() {
        ClientConfig cc = new ClientConfig();
        cc.register(HttpAuthenticationFeature.basic("joe", "joe"));
        Client client = ClientBuilder.newClient(cc);
        target = client.target("http://localhost:" + getReststopPort());
        target.path("dummy_smtpd").path("messages").request().delete();
    }
    @Test
    public void shouldGetFullname() {

        // When
        UserProfile prof = target.path("userprofiles").path("OLANOR").request().get(UserProfile.class);

        // Then
        assertThat(prof.getFullName(), is("Ola Nordmann"));

        // And when
        List<MessageJson> messageJsons = target.path("dummy_smtpd").path("messages").request().get(new GenericType<List<MessageJson>>() {});

        // Then
        assertThat(messageJsons.size(), is(1));

        MessageJson email = messageJsons.get(0);

        assertThat(email.getSubject(), is("User Ola Nordmann looked up by joe"));

    }

    @Test(expected = BadRequestException.class)
    public void shouldFailWithValidationError() {

        target.path("userprofiles").path("OL").request().get(UserProfile.class);

    }
}
