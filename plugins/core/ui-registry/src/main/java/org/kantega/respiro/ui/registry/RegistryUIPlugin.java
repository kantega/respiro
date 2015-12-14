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

package org.kantega.respiro.ui.registry;

import org.kantega.respiro.api.ApplicationBuilder;
import org.kantega.respiro.ui.UiModule;
import org.kantega.reststop.api.*;

import javax.servlet.Filter;
import javax.ws.rs.core.Application;

/**
 *
 */
@Plugin
public class RegistryUIPlugin {

    @Export
    final UiModule uiModule = () -> "registry.js";
    @Export final Filter js;
    @Export final Filter html;
    @Export final Application uiApp;


    public RegistryUIPlugin(@Config(defaultValue = "/respiro") String respiroPath,
                            ServletBuilder servletBuilder,
                            ApplicationBuilder applicationBuilder,
                            ReststopPluginManager pluginManager) {

        uiApp = applicationBuilder.application()
                .singleton(new RegistryResource(pluginManager))
                .build();


        String respiroDir = respiroPath + "/";

        js = servletBuilder.resourceServlet(respiroDir +"registry.js", getClass().getResource("/registry/registry.js"));

        html = servletBuilder.resourceServlet(respiroDir +"partials/registry.html", getClass().getResource("/registry/registry.html"));
    }
}
