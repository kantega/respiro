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

package org.kantega.respiro.cxf;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.kantega.respiro.api.ServiceBuilder;
import org.kantega.respiro.cxf.api.ServiceCustomizer;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.handler.Handler;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

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
            
            if( handlerChain == null)
                handlerChain = new ArrayList<>();
            
            handlerChain.add(handler);
            return this;
        }

        @Override
        public P build() {

            ClassLoader current = currentThread().getContextClassLoader();
            try {
                currentThread().setContextClassLoader(getClass().getClassLoader());
                String wsdlLocation = findWsdlLocation(serviceClass);
                URL wsdlURL = serviceClass.getClassLoader().getResource(wsdlLocation);
                Service srv = serviceClass.getConstructor(URL.class).newInstance(wsdlURL);
                P port = srv.getPort(portClass);

                BindingProvider prov = (BindingProvider) port;
                Map<String, Object> rc = prov.getRequestContext();

                configureAuthentication(rc);
                configureEndpointAddress(rc);
                configureTimeouts(port);

                configureHandlerChain(prov);
                
                applyPluginConfiguration(prov);
                return port;

            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                currentThread().setContextClassLoader(current);
            }
        }

        private void configureHandlerChain(BindingProvider port) {
            if(this.handlerChain != null) {
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

        private void configureTimeouts(P port) {
            Client client = getClient(port);
            HTTPConduit conduit = (HTTPConduit) client.getConduit();

            HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
            httpClientPolicy.setConnectionTimeout(connectionTimeoutMs);
            httpClientPolicy.setReceiveTimeout(receiveTimeoutMs);
            conduit.setClient(httpClientPolicy);
        }

        private String findWsdlLocation(Class<? extends Service> serviceClass) {
            return serviceClass.getAnnotation(WebServiceClient.class).wsdlLocation();
        }
    }
}
