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

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.annotations.SchemaValidation;
import org.apache.cxf.bus.managers.ServiceContractResolverRegistryImpl;
import org.apache.cxf.endpoint.ServiceContractResolverRegistry;
import org.apache.cxf.interceptor.security.SecureAnnotationsInterceptor;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.cxf.wsdl.WSDLManager;
import org.kantega.respiro.api.EndpointBuilder;
import org.kantega.respiro.api.EndpointConfig;
import org.kantega.respiro.api.ServiceBuilder;
import org.kantega.respiro.cxf.api.EndpointCustomizer;
import org.kantega.respiro.cxf.api.EndpointDeployer;
import org.kantega.respiro.cxf.api.ServiceCustomizer;
import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.servlet.api.ServletBuilder;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.wsdl.Definition;
import javax.xml.ws.Endpoint;
import java.util.*;

/**
 *
 */
@Plugin
public class CxfPlugin  {


    @Export
    private final EndpointBuilder endpointBuilder;

    @Export
    private final ServiceBuilder serviceBuilder;

    @Export
    private final EndpointDeployer endpointDeployer;

    @Export
    private final Filter cxfFilter;

    private final String schemaValidation;
    private final Collection<EndpointCustomizer> endpointCustomizers;

    private List<Endpoint> endpoints = new ArrayList<>();

    public static ThreadLocal<ClassLoader> pluginClassLoader = new ThreadLocal<>();

    public static ThreadLocal<EndpointConfig> currentConfig = new ThreadLocal<>();



    public CxfPlugin(@Config(defaultValue = "/ws/*", doc = "Path where the CXF plugin will be deployed") String mountPoint,

                     @Config(defaultValue = "BOTH") String schemaValidation,

                     Collection<EndpointCustomizer> endpointCustomizers,
                     Collection<ServiceCustomizer> serviceCustomizers,
                     ServletBuilder servletBuilder) throws ServletException {
        this.schemaValidation = schemaValidation;
        this.endpointCustomizers = endpointCustomizers;

        Bus bus = BusFactory.newInstance().createBus();

        ServiceContractResolverRegistry reg = new ServiceContractResolverRegistryImpl(bus);

        reg.register(new RespiroServiceContractResolver());

        CXFNonSpringServlet cxfNonSpringServlet = new CXFNonSpringServlet();
        cxfNonSpringServlet.setBus(bus);
        cxfNonSpringServlet.init(servletBuilder.servletConfig("cxf", new Properties()));


        cxfFilter = servletBuilder.servlet(cxfNonSpringServlet, mountPoint);

        endpointBuilder = new DefaultEndpointBuilder();
        serviceBuilder = new DefaultServiceBuilder(serviceCustomizers);

        endpointDeployer = endpointConfigs -> CxfPlugin.this.deployEndpoints(endpointConfigs);
    }


    private void deployEndpoints(Collection<EndpointConfig> endpointConfigs) {
        for (Endpoint endpoint : endpoints) {
            endpoint.stop();
        }
        endpoints.clear();

        WSDLManager wsdlManager = WSDLManagerDefinitionCacheCleaner.getWsdlManager();
        for (Definition def : new ArrayList<>(wsdlManager.getDefinitions().values())) {
            wsdlManager.removeDefinition(def);
        }


        for (EndpointConfig config : endpointConfigs) {

            try {
                pluginClassLoader.set(config.getClassLoader());

                currentConfig.set(config);
                Endpoint endpoint;
                try {
                    endpoint = Endpoint.create(config.getImplementor());
                    configureEndpoint(config, endpoint);

                    endpoint.publish(config.getPath());
                } finally {
                    currentConfig.remove();
                }

                customizeEndpoint(config, endpoint);

                CxfPlugin.this.endpoints.add(endpoint);
            } finally {
                pluginClassLoader.remove();
            }
        }



    }

    private void configureEndpoint(EndpointConfig config, Endpoint endpoint) {
        Map<String, Object> props = endpoint.getProperties();

        // Basic config
        if(config.getWsdlService() != null) {
            props.put(Endpoint.WSDL_SERVICE, config.getWsdlService());
        }

        if(config.getWsdlPort() != null) {
            props.put(Endpoint.WSDL_PORT, config.getWsdlPort());
        }

        // setup schema validation
        props.put(Message.SCHEMA_VALIDATION_ENABLED, SchemaValidation.SchemaValidationType.valueOf(schemaValidation.toUpperCase()));


        for (EndpointCustomizer customizer : endpointCustomizers) {
            customizer.configureEndpoint(endpoint);
        }
    }

    private void customizeEndpoint(EndpointConfig config, Endpoint endpoint) {

        EndpointImpl e = (EndpointImpl) endpoint;

        // log incoming requests
        SecureAnnotationsInterceptor sai = new RespiroSecureAnnotationsInterceptor();
        sai.setSecuredObject(config.getImplementor());
        e.getServer().getEndpoint().getInInterceptors().add(sai);

        for (EndpointCustomizer customizer : endpointCustomizers) {
            customizer.customizeEndpoint(endpoint);
        }

    }

}
