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

package org.kantega.respiro.cxf;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.kantega.respiro.api.ServiceBuilder;
import org.kantega.respiro.cxf.api.ServiceCustomizer;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.handler.Handler;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.currentThread;
import static javax.xml.ws.BindingProvider.*;
import static org.apache.cxf.frontend.ClientProxy.getClient;

class DefaultServiceBuilder implements ServiceBuilder {


    private final Collection<ServiceCustomizer> serviceCustomizers;

    public DefaultServiceBuilder(Collection<ServiceCustomizer> serviceCustomizers) {


        this.serviceCustomizers = serviceCustomizers;
    }

    @Override
    public <P> Build<P> service(Class<? extends Service> service, Class<P> port) {
        return new Build<P>(service, port);
    }

    private class Build<P> implements ServiceBuilder.Build<P> {
        private Class<? extends Service> serviceClass;
        private Class<P> portClass;
        private String username;
        private String password;
        private String endpointAddress;
        private long connectionTimeoutMs = 15_000;
        private long receiveTimeoutMs = 60_000;
        private List<Handler> handlerChain = null;
        private KeyManager[] keyManagers;
        private TrustManager[] trustManagers;
        private String clientCertAlias;


        public Build(Class<? extends Service> service, Class<P> port) {
            this.serviceClass = service;
            this.portClass = port;
        }

        @Override
        public ServiceBuilder.Build<P> username(String username) {
            this.username = username;
            return this;
        }

        @Override
        public ServiceBuilder.Build<P> password(String password) {
            this.password = password;
            return this;
        }

        @Override
        public ServiceBuilder.Build<P> endpointAddress(String endpointAddress) {
            this.endpointAddress = endpointAddress;
            return this;
        }

        @Override
        public ServiceBuilder.Build<P> receiveTimeoutMs(long timeoutMs) {
            this.receiveTimeoutMs = timeoutMs;
            return this;
        }

        @Override
        public ServiceBuilder.Build<P> connectTimeoutMs(long timeoutMs) {
            this.connectionTimeoutMs = timeoutMs;
            return this;
        }

        @Override
        public ServiceBuilder.Build<P> addHandler(Handler handler) {

            if (handlerChain == null)
                handlerChain = new ArrayList<>();

            handlerChain.add(handler);
            return this;
        }


        @Override
        public ServiceBuilder.Build<P> certAlias(String alias) {
            clientCertAlias = alias;
            return this;
        }

        @Override
        public ServiceBuilder.Build<P> keystore(KeyStoreType type, String keystorePath, String keystorePassword) {

            try {
                final KeyStore store = KeyStore.getInstance(type.name());
                store.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
                final KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                factory.init(store, keystorePassword.toCharArray());
                this.keyManagers = factory.getKeyManagers();

            } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | IOException | CertificateException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        @Override
        public ServiceBuilder.Build<P> truststore(KeyStoreType type, String keystorePath, String keystorePassword) {
            try {
                final KeyStore store = KeyStore.getInstance(type.name());
                store.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
                final TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                factory.init(store);
                this.trustManagers = factory.getTrustManagers();
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        @Override
        public P build() {

            ClassLoader current = currentThread().getContextClassLoader();
            try {
                currentThread().setContextClassLoader(getClass().getClassLoader());
                final String wsdlLocation = findWsdlLocation(serviceClass);
                final URL wsdlURL = serviceClass.getClassLoader().getResource(wsdlLocation);
                final Service srv = serviceClass.getConstructor(URL.class).newInstance(wsdlURL);
                final P port = srv.getPort(portClass);

                final BindingProvider prov = (BindingProvider) port;
                final Map<String, Object> rc = prov.getRequestContext();
                final HTTPConduit conduit = (HTTPConduit) getClient(port).getConduit();


                configureAuthentication(rc);
                configureEndpointAddress(rc);
                configureTimeouts(conduit);
                configureTLS(conduit);

                configureHandlerChain(prov);

                applyPluginConfiguration(prov);
                return port;

            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                currentThread().setContextClassLoader(current);
            }
        }

        private void configureTLS(HTTPConduit conduit) {
            if (trustManagers == null && keyManagers == null)
                return;


            final TLSClientParameters tlsClientParameters = new TLSClientParameters();
            if (trustManagers != null) 
                tlsClientParameters.setTrustManagers(trustManagers);

            if (keyManagers != null)
                tlsClientParameters.setKeyManagers(keyManagers);

            tlsClientParameters.setCertAlias(clientCertAlias);
            
            conduit.setTlsClientParameters(tlsClientParameters);
        }

        private void configureHandlerChain(BindingProvider port) {
            if (this.handlerChain != null) {
                final List<Handler> handlerChain = port.getBinding().getHandlerChain();
                handlerChain.addAll(this.handlerChain);
            }
        }

        private void applyPluginConfiguration(BindingProvider port) {
            for (ServiceCustomizer customizer : serviceCustomizers) {
                customizer.customizeService(port);
            }
        }

        private void configureEndpointAddress(Map<String, Object> rc) {
            rc.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
        }

        private void configureAuthentication(Map<String, Object> rc) {
            rc.put(USERNAME_PROPERTY, username);
            rc.put(PASSWORD_PROPERTY, password);
        }

        private void configureTimeouts(HTTPConduit conduit) {

            HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
            httpClientPolicy.setConnectionTimeout(connectionTimeoutMs);
            httpClientPolicy.setReceiveTimeout(receiveTimeoutMs);
            httpClientPolicy.setAutoRedirect(true);
            httpClientPolicy.setAllowChunking(true);
            conduit.setClient(httpClientPolicy);
        }

        private String findWsdlLocation(Class<? extends Service> serviceClass) {
            return serviceClass.getAnnotation(WebServiceClient.class).wsdlLocation();
        }
    }
}
