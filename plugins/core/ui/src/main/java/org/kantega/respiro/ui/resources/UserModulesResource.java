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

package org.kantega.respiro.ui.resources;

import org.kantega.respiro.ui.UiModule;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Path("respiro/modules")
public class UserModulesResource {

    // Disable metrics for this resource
    public static boolean METRICS = false;

    private final Collection<UiModule> uiModules;

    public UserModulesResource(Collection<UiModule> uiModules) {
        this.uiModules = uiModules;
    }



    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ModuleJson> get() {
        return uiModules.stream().map( m -> toJson(m)).collect(Collectors.toList());
    }

    private ModuleJson toJson(UiModule m) {
        return new ModuleJson(m);
    }


}
