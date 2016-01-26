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

package org.kantega.respiro.ui.plugins;


import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.ReststopPluginManager;
import org.kantega.reststop.classloaderutils.PluginClassLoader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Field;
import java.util.*;

/**
 *
 */
@Path("respiro/plugins")
public class PluginsResource {

    // Disable metrics for this resource
    public static boolean METRICS = false;


    private final ReststopPluginManager pluginManager;

    public PluginsResource(ReststopPluginManager pluginManager) {

        this.pluginManager = pluginManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PluginList get() throws IllegalAccessException {



        Map<ClassLoader, List<Object>> map = new IdentityHashMap<>();

        for (ClassLoader classLoader : pluginManager.getPluginClassLoaders()) {
            if(classLoader instanceof PluginClassLoader) {
                map.put(classLoader, new ArrayList<>());
            }
        }

        for (Object plugin : pluginManager.getPlugins()) {
            map.get(pluginManager.getClassLoader(plugin)).add(plugin);
        }

        PluginList plugins = new PluginList();

        for (ClassLoader classLoader : map.keySet()) {
            PluginJson plugin = toJson(classLoader);
            plugins.add(plugin);
            for (Object pluginObj : map.get(classLoader)) {
                for (Field field : pluginObj.getClass().getDeclaredFields()) {
                    if (field.getAnnotation(Export.class) != null) {
                        plugin.getExports().add(field.getGenericType().getTypeName());
                    }
                }
            }
        }
        Collections.sort(plugins, Comparator.comparing(PluginJson::getArtifactId));
        return plugins;
    }

    private PluginJson toJson(ClassLoader classLoader) {
        return new PluginJson((PluginClassLoader) classLoader);
    }
}
