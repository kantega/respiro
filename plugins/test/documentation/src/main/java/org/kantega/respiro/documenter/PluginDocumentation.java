package org.kantega.respiro.documenter;

import fj.data.List;
import org.apache.commons.lang3.StringUtils;

public class PluginDocumentation {

    public final String pluginDescription;
    public final List<DependencyDocumentation> dependencies;
    public final List<RouteDocumentation> routes;
    public final List<ResourceDocumentation> resources;

    public PluginDocumentation(
      String pluginDescription,
      List<DependencyDocumentation> dependencies,
      List<RouteDocumentation> routes,
      List<ResourceDocumentation> resources) {
        this.pluginDescription = StringUtils.replace(pluginDescription,"*","");
        this.dependencies = dependencies;
        this.routes = routes;
        this.resources = resources;
    }
}
