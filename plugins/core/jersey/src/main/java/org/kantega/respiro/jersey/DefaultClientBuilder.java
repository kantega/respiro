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

package org.kantega.respiro.jersey;

import org.kantega.respiro.api.RestClientBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Feature;
import java.util.Collection;

import static java.lang.Thread.currentThread;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.basic;
import static org.kantega.respiro.api.RestClientBuilder.Build;

public class DefaultClientBuilder implements RestClientBuilder {
    private final Collection<ClientCustomizer> clientCustomizers;

    public DefaultClientBuilder(Collection<ClientCustomizer> clientCustomizers) {

        this.clientCustomizers = clientCustomizers;
    }

    @Override
    public Build client() {
        return new Build();
    }

    class Build implements RestClientBuilder.Build {

        private Feature basicAuth;

        @Override
        public Build basicAuth(String username, String password) {
            this.basicAuth = basic(username, password);
            return this;
        }

        @Override
        public Client build() {
            ClientConfig cc = new ClientConfig();

            if (basicAuth != null)
                cc.register(basicAuth);

            for (ClientCustomizer clientCustomizer : clientCustomizers) {
                clientCustomizer.customize(cc);
            }
            ClassLoader contextClassloader = currentThread().getContextClassLoader();
            try {
                currentThread().setContextClassLoader(getClass().getClassLoader());
                return newClient(cc);
            } finally {
                currentThread().setContextClassLoader(contextClassloader);

            }
        }
    }

}
