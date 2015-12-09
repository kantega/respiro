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
