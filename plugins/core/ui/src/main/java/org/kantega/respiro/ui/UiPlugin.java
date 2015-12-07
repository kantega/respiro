package org.kantega.respiro.ui;

import org.kantega.respiro.api.ApplicationBuilder;
import org.kantega.respiro.ui.resources.PluginsResource;
import org.kantega.respiro.ui.resources.RegistryResource;
import org.kantega.respiro.ui.resources.UserProfileResource;
import org.kantega.reststop.api.*;

import javax.servlet.Filter;
import javax.ws.rs.core.Application;
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
                    Collection<MenuContributor> menuContributions,
                    ServletBuilder servletBuilder,
                    ApplicationBuilder applicationBuilder,
                    ReststopPluginManager pluginManager) {

        uiApp = applicationBuilder.application()
                .singleton(new UserProfileResource())
                .singleton(new PluginsResource(pluginManager))
                .singleton(new RegistryResource(pluginManager))
                .build();


        String respiroDir = respiroPath + "/";

        filters.add(servletBuilder.redirectServlet(respiroPath, respiroDir));
        filters.add(servletBuilder.resourceServlet(respiroDir, getClass().getResource("/ui/index.html")));
        filters.add(servletBuilder.resourceServlet(respiroDir +"respiro.js", getClass().getResource("/ui/respiro.js")));
        partial("plugins.html", servletBuilder, respiroDir);
        partial("metrics.html", servletBuilder, respiroDir);
        partial("registry.html", servletBuilder, respiroDir);


    }

    private boolean partial(String name, ServletBuilder servletBuilder, String respiroDir) {
        return filters.add(servletBuilder.resourceServlet(respiroDir +"partials/" +name, getClass().getResource("/ui/partials/" + name)));
    }


}
