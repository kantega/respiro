package org.kantega.respiro.ui;

import org.kantega.respiro.api.ApplicationBuilder;
import org.kantega.respiro.ui.resources.UserModulesResource;
import org.kantega.respiro.ui.resources.UserProfileResource;
import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.api.ServletBuilder;

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

        filters.add(servletBuilder.redirectServlet(respiroPath, respiroDir));
        filters.add(servletBuilder.resourceServlet(respiroDir +"respiro.js", getClass().getResource("/ui/respiro.js")));
        filters.add(servletBuilder.resourceServlet(respiroDir, getClass().getResource("/ui/index.html")));
    }
}
