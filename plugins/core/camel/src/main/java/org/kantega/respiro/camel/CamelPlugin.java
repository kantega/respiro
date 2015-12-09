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

package org.kantega.respiro.camel;

import org.kantega.respiro.api.DataSourceInitializer;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.impl.DefaultCamelContext;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.api.ServletBuilder;

import javax.annotation.PreDestroy;
import javax.servlet.Filter;
import java.util.Collection;
import java.util.Properties;

/**
 *
 */
@Plugin
public class CamelPlugin implements CamelRouteDeployer {

    @Export
    private final Filter camelServlet;

    private final CamelHttpTransportServlet httpServlet;
    private final ServletBuilder servletBuilder;
    private final Collection<DataSourceInitializer> dataSourceInitializers;
    private final Collection<CamelContextCustomizer> camelContextCustomizers;

    private CamelContext camelContext;

    @Export final CamelRouteDeployer camelRouteDeployer = this;

    public CamelPlugin(ServletBuilder servletBuilder, Collection<DataSourceInitializer> dataSourceInitializers, Collection<CamelContextCustomizer> camelContextCustomizers) throws Exception {
        this.servletBuilder = servletBuilder;
        this.dataSourceInitializers = dataSourceInitializers;
        this.camelContextCustomizers = camelContextCustomizers;


        httpServlet = new CamelHttpTransportServlet();


        httpServlet.init(servletBuilder.servletConfig("CamelServlet", new Properties()));

        camelServlet = servletBuilder.servlet(httpServlet, "/camel/*");





    }

    @Override
    public void deploy(Collection<RouteBuilder> routeBuilders) {
        try {
            camelContext = new DefaultCamelContext();

            camelContextCustomizers.forEach(c -> c.customize(camelContext));

            for (RouteBuilder routeBuilder : routeBuilders) {
                camelContext.addRoutes(routeBuilder);
            }
            camelContext.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stop() throws Exception {
        httpServlet.destroy();
        camelContext.stop();

    }
}


