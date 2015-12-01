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


import org.kantega.respiro.api.*;
import org.kantega.respiro.hello.ws.greet_1_0.Greeting;
import org.kantega.respiro.hello.ws.greet_1_0.GreetingService;
import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;

/**
 *
 */
@Plugin
public class HelloworldWsPlugin  {

    @Export
    private final EndpointConfig helloEndpoint;

    @Export final Application helloApplication;


    public HelloworldWsPlugin(@Config(doc="URL to the user profile REST service") String userProfileBaseUrl,
                              @Config String greetingEndpointAddress,
                              @Config String greetingUsername,
                              @Config String greetingPassword,
                              @Config String userProfileUsername,
                              @Config String userProfilePassword,
                              EndpointBuilder epBuilder,
                              ServiceBuilder srvBuilder,
                              RestClientBuilder clientBuilder,
                              ApplicationBuilder applicationBuilder,
                              RespiroExecutorService executorService) {

        Greeting greet = srvBuilder.service(GreetingService.class, Greeting.class)
                .username(greetingUsername).password(greetingPassword)
                .endpointAddress(greetingEndpointAddress).build();

        Client client = clientBuilder.client().basicAuth(userProfileUsername, userProfilePassword).build();
        HelloService helloService = new HelloService(greet, new UserProfileService(userProfileBaseUrl, client), executorService);

        helloEndpoint = epBuilder.endpoint(getClass(), helloService)
                .path("/hello-1.0")
                .wsdlNamed("HelloService", "1.0")
                .build();

        helloApplication = applicationBuilder.application().singleton(new GreetResource(greet)).build();
    }
}
