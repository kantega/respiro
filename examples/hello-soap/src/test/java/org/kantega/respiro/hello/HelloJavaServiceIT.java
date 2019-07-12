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

import org.kantega.respiro.hello.ws.hello_1_0.Hello;
import org.kantega.respiro.hello.ws.hello_1_0.MyFault;
import org.junit.Test;

import javax.xml.transform.TransformerException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.kantega.respiro.test.Utils.getReststopPort;

/**
 *
 */
public class HelloJavaServiceIT {

    @Test
    public void shouldRespond() throws TransformerException, MyFault {

        Hello helloPort = getService();

        String textContent = helloPort.greet("Joe",null);

        assertThat(textContent, is("Hello, Joe! (called by Jane Doe)"));
    }

    @Test (expected = WebServiceException.class)
    public void shouldFailBecauseOfNullReceiver() throws TransformerException, MyFault {

        Hello helloPort = getService();

        helloPort.greet(null,"se");

    }
    @Test(expected = WebServiceException.class)
    public void shouldFailBecauseOfUnknownLanguage() throws TransformerException, MyFault {

        Hello helloPort = getService();

        helloPort.greet("John","de");

    }

    @Test (expected = MyFault.class)
    public void shouldFailBecauseOfMyFault() throws MyFault {

        Hello helloPort = getService();

        helloPort.greet("John","oracle");

    }

    public static  Hello getService() {
        org.kantega.respiro.hello.ws.hello_1_0.HelloService helloService = new org.kantega.respiro.hello.ws.hello_1_0.HelloService();
        Hello helloPort = helloService.getHelloPort();

        BindingProvider prov = (BindingProvider)helloPort;
        Map<String,Object> rc = prov.getRequestContext();
        rc.put(BindingProvider.USERNAME_PROPERTY, "jane");
        rc.put(BindingProvider.PASSWORD_PROPERTY, "jane");
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:" + getReststopPort() + "/ws/hello-1.0");
        return helloPort;
    }
}
