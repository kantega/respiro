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

import org.glassfish.jersey.SslConfigurator;
import org.kantega.respiro.api.RestClientBuilder;
import org.glassfish.jersey.client.ClientConfig;

import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Feature;
import java.util.Collection;

import static java.lang.Thread.currentThread;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.basic;

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
        private SSLContext sslContext;

        @Override
        public Build basicAuth(String username, String password) {
            this.basicAuth = basic(username, password);
            return this;
        }

        @Override
        public Build sslClientAuth(String keystorePath, String keystorePassword, String truststorePath, String truststorePassword) {

            if ((keystorePath == null) && (truststorePath == null)) {
                return this;
            }

            SslConfigurator sslConfig = SslConfigurator.newInstance().securityProtocol("TLS");
            if (keystorePath != null) {
                sslConfig = sslConfig
                    .keyStoreFile(keystorePath)
                    .keyStorePassword(keystorePassword)
                    .keyStoreType("PKCS12");
            }
            if (truststorePath != null) {
                sslConfig = sslConfig
                    .trustStoreFile(truststorePath)
                    .trustStorePassword(truststorePassword);
            }
            this.sslContext = sslConfig.createSSLContext();
            return this;
        }

        @Override
        public Client build() {
            ClientConfig cc = new ClientConfig();

            if (basicAuth != null) {
                cc.register(basicAuth);
            }

            for (ClientCustomizer clientCustomizer : clientCustomizers) {
                clientCustomizer.customize(cc);
            }
            ClassLoader contextClassloader = currentThread().getContextClassLoader();
            try {
                currentThread().setContextClassLoader(getClass().getClassLoader());
                ClientBuilder clientBuilder = ClientBuilder.newBuilder().withConfig(cc);

                if (sslContext != null) {
                    clientBuilder = clientBuilder.sslContext(sslContext).hostnameVerifier(getHostnameVerifier());
                }
                return clientBuilder.build();
            } finally {
                currentThread().setContextClassLoader(contextClassloader);
            }
        }

        private HostnameVerifier getHostnameVerifier() {
            return new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                    return true;
                }
            };
        }
    }
}
