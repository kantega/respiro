package org.kantega.respiro.ui.plugins;

import org.kantega.respiro.api.ApplicationBuilder;
import org.kantega.respiro.ui.UiModule;
import org.kantega.reststop.api.*;

import javax.servlet.Filter;
import javax.ws.rs.core.Application;

/**
 *
 */
@Plugin
public class PluginsUiPlugin {

    @Export final UiModule uiModule = () -> "plugins.js";
    @Export final Filter js;
    @Export final Filter html;
    @Export final Application uiApp;


    public PluginsUiPlugin(@Config(defaultValue = "/respiro") String respiroPath,
                           ServletBuilder servletBuilder,
                           ApplicationBuilder applicationBuilder,
                           ReststopPluginManager pluginManager) {

        uiApp = applicationBuilder.application()
                .singleton(new PluginsResource(pluginManager))
                .build();

        String respiroDir = respiroPath + "/";

        js = servletBuilder.resourceServlet(respiroDir +"plugins.js", getClass().getResource("/plugins/plugins.js"));

        html = servletBuilder.resourceServlet(respiroDir +"partials/plugins.html", getClass().getResource("/plugins/plugins.html"));
    }
}
