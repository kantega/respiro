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

import org.glassfish.jersey.client.ClientConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kantega.respiro.testsmtp.MessageJson;

import javax.jms.JMSException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import java.util.List;

import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.basic;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.kantega.respiro.test.Utils.getReststopPort;

public class UserProfileIT {

    private WebTarget target;
    private TopicListener topicListener;

    @Before
    public void before() throws JMSException {
        ClientConfig cc = new ClientConfig();
        cc.register(basic("joe", "joe"));
        Client client = newClient(cc);
        target = client.target("http://localhost:" + getReststopPort());
        target.path("dummy_smtpd").path("messages").request().delete();
        topicListener = new TopicListener();
    }

    @After
    public void after() {
        topicListener.close();
    }
    @Test
    public void shouldGetFullname() {

        // When
        UserProfile prof = target.path("userprofiles").path("OLANOR").request().get(UserProfile.class);

        // Then
        assertThat(prof.getFullName(), is("Ola Nordmann"));

        // And when
        List<MessageJson> messageJsons = target.path("dummy_smtpd").path("messages").request().get(new GenericType<List<MessageJson>>() {
        });

        // Then
        assertThat(messageJsons.size(), is(1));

        MessageJson email = messageJsons.get(0);

        assertThat(email.getSubject(), is("User Ola Nordmann looked up by joe"));

        assertThat(topicListener.getMessages().size(), is(1));

        assertThat(topicListener.getMessages().get(0), is("Profile 'Ola Nordmann' looked up by joe"));

    }

    @Test(expected = BadRequestException.class)
    public void shouldFailWithValidationError() {

        target.path("userprofiles").path("OL").request().get(UserProfile.class);

    }

    @Test
    public void shouldPostFullname() {
        UserProfile userProfile = target.path("userprofiles").path("OLANOR")
                .request().post(Entity.entity("Ola Normann", "text/plain"), UserProfile.class);


    }
}
