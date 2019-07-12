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

package org.kantega.respiro.hello;

import org.glassfish.jersey.client.ClientConfig;
import org.junit.Before;
import org.junit.Test;
import org.kantega.respiro.exchanges.rest.ExchangeJs;
import org.kantega.respiro.exchanges.rest.MessageJs;
import org.kantega.respiro.hello.ws.hello_1_0.Hello;
import org.kantega.respiro.hello.ws.hello_1_0.MyFault;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.xml.transform.TransformerException;

import java.util.List;

import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.basic;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.kantega.respiro.hello.HelloJavaServiceIT.getService;
import static org.kantega.respiro.test.Utils.getReststopPort;

/**
 *
 */

public class ExchangesIT {

    private WebTarget target;
    private Hello helloPort;

    @Before
    public void before() {
        ClientConfig cc = new ClientConfig();
        cc.register(basic("joe", "joe"));
        Client client = newClient(cc);
        target = client.target("http://localhost:" + getReststopPort());
        helloPort = getService();

        target.path("respiro/exchanges/api/exchanges").request().delete();
    }

    @Test
    public void shouldCollectExchanges() throws TransformerException, MyFault {

        assertEquals(0, getExchanges().size());

        String textContent = helloPort.greet("Joe",null);

        List<ExchangeJs> exchanges = getExchanges();
        assertEquals(2, exchanges.size());

        ExchangeJs first = exchanges.get(0);

        List<MessageJs> messages = first.getBackendMessages();

        assertEquals(6, messages.size());

        assertThat(messages.get(0).getProtocol(), is("REST")); // REST POST
        assertThat(messages.get(0).getType(), is("REQUEST"));
        assertThat(messages.get(0).getMethod(), is("POST"));

        assertThat(messages.get(4).getProtocol(), is("SOAP")); // SOAP RESPONSE
        assertThat(messages.get(4).getType(), is("REQUEST"));
        assertThat(messages.get(4).getMethod(), is("POST"));


        assertThat(first.getOutMessage().getProtocol(), is("SOAP"));
        assertThat(first.getOutMessage().getType(), is("RESPONSE"));
        assertThat(first.getOutMessage().getMethod(), is("POST"));


    }

    private List<ExchangeJs> getExchanges() {
        return target.path("respiro/exchanges/api/exchanges").request().get(new GenericType<List<ExchangeJs>>() {});
    }


}
