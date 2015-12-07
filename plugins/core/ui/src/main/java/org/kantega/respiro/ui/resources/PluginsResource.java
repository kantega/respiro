package org.kantega.respiro.ui.resources;


import org.kantega.reststop.api.ReststopPluginManager;
import org.kantega.reststop.classloaderutils.PluginClassLoader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Comparator;

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
    public PluginList get() {

        PluginList plugins = new PluginList();

        for (ClassLoader classLoader : pluginManager.getPluginClassLoaders()) {
            if(classLoader instanceof PluginClassLoader) {
                plugins.add(toJson(classLoader));
            }
        }
        Collections.sort(plugins, Comparator.comparing(PluginJson::getArtifactId));
        return plugins;
    }

    private PluginJson toJson(ClassLoader classLoader) {
        return new PluginJson((PluginClassLoader) classLoader);
    }
}
