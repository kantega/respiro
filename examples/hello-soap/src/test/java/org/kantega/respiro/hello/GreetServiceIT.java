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

import org.junit.Test;
import org.kantega.respiro.test.Utils;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class GreetServiceIT {

    @Test
    public void shouldRespond() throws TransformerException, XPathExpressionException {

        Dispatch<Source> helloPort = getDispatch();

        Source invoke = helloPort.invoke(new StreamSource(getClass().getResourceAsStream("greetRequest.xml")));

        DOMResult result = new DOMResult();
        TransformerFactory.newInstance().newTransformer().transform(invoke, result);

        Document doc = (Document) result.getNode();

        String textContent = XPathFactory.newInstance().newXPath().evaluate("//*[local-name()='messageResult']", doc.getDocumentElement());

        assertThat(textContent, is("Hej"));
    }


    private Dispatch<Source> getDispatch() {
        Service helloService = Service.create(getClass().getResource("/META-INF/wsdl/GreetingService-1.0/GreetingService.wsdl"),
                new QName("http://hello.respiro.kantega.org/ws/greet-1.0", "GreetingService"));

        Dispatch<Source> helloPort = helloService.createDispatch(new QName("http://hello.respiro.kantega.org/ws/greet-1.0", "GreetingPort"),
                Source.class, Service.Mode.PAYLOAD);

        BindingProvider prov = (BindingProvider)helloPort;
        Map<String,Object> rc = prov.getRequestContext();
        rc.put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
        rc.put(BindingProvider.SOAPACTION_URI_PROPERTY, "greet");
        rc.put(BindingProvider.USERNAME_PROPERTY, "joe");
        rc.put(BindingProvider.PASSWORD_PROPERTY, "joe");
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:" + Utils.getReststopPort() + "/ws/dummy/greeting-1.0");
        return helloPort;
    }


}
