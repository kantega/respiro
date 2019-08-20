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

package org.kantega.respiro.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 */
@Plugin
public class CamelPlugin implements CamelRouteDeployer {

    private final Collection<CamelContextCustomizer> camelContextCustomizers;


    private final SimpleRegistry simpleRegistry = new SimpleRegistry();

    private final ContextRegistry contexts;

    @Export
    final CamelRouteDeployer camelRouteDeployer = this;

    @Export
    final CamelRegistry camelRegistry = new SimpleCamelRegistry(simpleRegistry);

    public CamelPlugin(Collection<CamelContextCustomizer> camelContextCustomizers) throws Exception {
        this.camelContextCustomizers = camelContextCustomizers;
        contexts = new ContextRegistry(simpleRegistry);
    }

    @Override
    public void deploy(Collection<RouteBuilder> routeBuilders) {
        try {


            for (RouteBuilder routeBuilder : routeBuilders) {
                contexts.register(routeBuilder, camelContextCustomizers);
            }
            contexts.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stop() {
        contexts.stop();
        camelContextCustomizers.forEach(CamelContextCustomizer::shutdown);

    }

    private class SimpleCamelRegistry implements CamelRegistry {
        private final SimpleRegistry simpleRegistry;

        public SimpleCamelRegistry(SimpleRegistry simpleRegistry) {
            this.simpleRegistry = simpleRegistry;
        }

        @Override
        public void add(String name, Object component) {
            simpleRegistry.put(name, component);
        }

        @Override
        public void remove(String name) {
            simpleRegistry.remove(name);
        }
    }

    private static class ContextRegistry {
        private final Map<ClassLoader, CamelContext> contextMap = new HashMap<>();
        final private SimpleRegistry simpleRegistry;

        private ContextRegistry(SimpleRegistry simpleRegistry) {
            this.simpleRegistry = simpleRegistry;
        }

        public void register(RouteBuilder route, Collection<CamelContextCustomizer> camelContextCustomizers) throws Exception {


            Optional.ofNullable(contextMap.get(route.getClass().getClassLoader()))
                .orElseGet(() -> {
                    
                    final CamelContext newCc = new DefaultCamelContext(simpleRegistry);
                    newCc.setApplicationContextClassLoader(route.getClass().getClassLoader());
                    camelContextCustomizers.forEach(cust -> cust.customize(newCc));
                    contextMap.put(route.getClass().getClassLoader(), newCc);

                    return newCc;
                }).addRoutes(route);



        }


        public void start() {
            contextMap.values().forEach(c -> {
                try {
                    c.start();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to start context", e);
                }
            });
        }

        public void stop() {
            contextMap.values().forEach(c -> {
                try {
                    c.stop();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to start context", e);
                }
            });
        }
    }
}


