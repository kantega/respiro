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

package org.kantega.respiro.hello;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import static org.kantega.respiro.test.Utils.getReststopPort;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class RestGreetIT {

    private WebTarget target;

    @Before
    public void before() {
        ClientConfig cc = new ClientConfig();
        cc.register(HttpAuthenticationFeature.basic("jane", "jane"));
        Client client = ClientBuilder.newClient(cc);
        target = client.target("http://localhost:" + getReststopPort());
    }
    @Test
    public void shouldGetFullname() {

        // When
        String greet = target.path("greet").path("se").request().get(String.class);

        // Then
        assertThat(greet, is("Hej"));
    }

}
