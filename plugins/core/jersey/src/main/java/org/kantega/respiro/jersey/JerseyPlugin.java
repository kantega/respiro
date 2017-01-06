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

package org.kantega.respiro.jersey;

import org.kantega.respiro.api.ApplicationBuilder;
import org.kantega.respiro.api.RestClientBuilder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.servlet.api.FilterPhase;
import org.kantega.reststop.servlet.api.ServletBuilder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;

/**
 *
 */
@Plugin
public class JerseyPlugin  implements ApplicationDeployer {

    @Export
    private final ApplicationBuilder builder;

    @Export
    private final ApplicationDeployer deployer = this;

    @Export
    private final RestClientBuilder clientBuilder;

    @Export
    private final Filter jerseyFilter;

    private final ServletContainer filter;

    private final Collection<ClientCustomizer> clientCustomizers;
    private final Collection<ApplicationCustomizer> applicationCustomizers;

    public JerseyPlugin(ServletBuilder servletBuilder, Collection<ClientCustomizer> clientCustomizers, Collection<ApplicationCustomizer> applicationCustomizers) throws ServletException {
        this.clientCustomizers = clientCustomizers;
        this.applicationCustomizers = applicationCustomizers;

        builder = new DefaultApplicationBuilder();
        filter = addJerseyFilter(new ReststopApplication(Collections.EMPTY_LIST));
        filter.init(servletBuilder.filterConfig("jersey", new Properties()));

        this.jerseyFilter = servletBuilder.filter(filter, FilterPhase.USER, "/*");

        clientBuilder = new DefaultClientBuilder(clientCustomizers);
    }

    private ServletContainer addJerseyFilter(Application application) {
        ResourceConfig resourceConfig = getResourceConfig(application);

        return new ServletContainer(resourceConfig) {
            @Override
            public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
                // Force read of request parameters if specified, otherwise Jersey will eat them
                if (request.getMethod().equals("POST") && MediaType.APPLICATION_FORM_URLENCODED.equals(request.getContentType())) {
                    request.getParameterMap();
                }
                super.doFilter(request, response, chain);
            }
        };
    }


    private ResourceConfig getResourceConfig(Application application) {
        ResourceConfig resourceConfig = ResourceConfig.forApplication(application);
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(RolesAllowedDynamicFeature.class);

        applicationCustomizers.forEach(c -> c.customize(resourceConfig));

        Map<String, Object> props = new HashMap<>(resourceConfig.getProperties());
        props.put(ServletProperties.FILTER_FORWARD_ON_404, "true");
        resourceConfig.setProperties(props);

        return resourceConfig;
    }

    @Override
    public void deploy(Collection<Application> applications) {
        synchronized (JerseyPlugin.this) {

            filter.reload(getResourceConfig(new ReststopApplication(applications)));

        }


    }
}
