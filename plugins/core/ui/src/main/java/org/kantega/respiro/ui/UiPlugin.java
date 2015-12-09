package org.kantega.respiro.ui;

import org.apache.commons.io.IOUtils;
import org.kantega.respiro.api.ApplicationBuilder;
import org.kantega.respiro.ui.resources.UserModulesResource;
import org.kantega.respiro.ui.resources.UserProfileResource;
import org.kantega.reststop.api.*;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

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


    private boolean partial(String name, ServletBuilder servletBuilder, String respiroDir) {
        return filters.add(servletBuilder.resourceServlet(respiroDir +"partials/" +name, getClass().getResource("/ui/partials/" + name)));
    }


}
