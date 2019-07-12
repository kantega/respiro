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

package org.kantega.respiro.ui.registry;


import org.kantega.respiro.api.EndpointConfig;
import org.kantega.reststop.api.PluginExport;
import org.kantega.reststop.api.ReststopPluginManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Field;

/**
 *
 */
@Path("respiro/registry")
public class RegistryResource {

    // Disable metrics for this resource
    public static boolean METRICS = false;

    private final ReststopPluginManager manager;

    public RegistryResource(ReststopPluginManager manager) {

        this.manager = manager;
    }


    @GET
    @Path("soap")
    @Produces(MediaType.APPLICATION_JSON)
    public SoapEndpoints getSoap() {

        SoapEndpoints soapEndpoints = new SoapEndpoints();

        for (PluginExport<EndpointConfig> endpointConfig : manager.findPluginExports(EndpointConfig.class)) {
            soapEndpoints.add(toSoapEndpoint(endpointConfig));
        }
        return soapEndpoints;
    }


    @GET
    @Path("rest")
    @Produces(MediaType.APPLICATION_JSON)
    public RestResources getRest() {

        RestResources restResources = new RestResources();

        for (PluginExport<Application> endpointConfig : manager.findPluginExports(Application.class)) {
            for (Object singleton : endpointConfig.getExport().getSingletons()) {
                if(singleton.getClass().isAnnotationPresent(Path.class)) {
                    if(!metricsDisabled(singleton.getClass())) {
                        restResources.add(toRestResource(singleton.getClass()));
                    }

                }
            }

        }
        return restResources;
    }

    private boolean metricsDisabled(Class<?> resourceClass) {
        try {
            Field metrics = resourceClass.getDeclaredField("METRICS");
            return Boolean.FALSE.equals(metrics.get(null));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }

    private RestResource toRestResource(Class clazz) {
        return new RestResource(clazz);
    }


    private SoapEndpoint toSoapEndpoint(PluginExport<EndpointConfig> endpointConfig) {
        return new SoapEndpoint(endpointConfig);
    }
}
