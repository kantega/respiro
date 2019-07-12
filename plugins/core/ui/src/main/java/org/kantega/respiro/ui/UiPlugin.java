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

package org.kantega.respiro.ui;

import org.kantega.respiro.api.ApplicationBuilder;
import org.kantega.respiro.ui.resources.UserModulesResource;
import org.kantega.respiro.ui.resources.UserProfileResource;
import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.servlet.api.ServletBuilder;

import javax.servlet.Filter;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
@Plugin
public class UiPlugin {


    @Export final Collection<Filter> filters = new ArrayList<>();
    @Export final Application uiApp;

    public UiPlugin(@Config(defaultValue = "/respiro") String respiroPath,
                    Collection<UiModule> uiModules,
                    ServletBuilder servletBuilder,
                    ApplicationBuilder applicationBuilder) throws IOException {

        uiApp = applicationBuilder.application()
                .singleton(new UserModulesResource(uiModules))
                .singleton(new UserProfileResource())
                .build();


        String respiroDir = respiroPath + "/";

        filters.add(servletBuilder.redirectFrom(respiroPath).to(respiroDir));
        filters.add(servletBuilder.resourceServlet(getClass().getResource("/ui/respiro.js"), respiroDir +"respiro.js"));
        filters.add(servletBuilder.resourceServlet(getClass().getResource("/ui/index.html"), respiroDir));
    }
}
