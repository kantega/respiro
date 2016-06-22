package org.kantega.respiro.documenter;

import fj.data.List;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class PluginDocumentation {

    public final String pluginDescription;
    public final List<DependencyDocumentation> dependencies;
    public final List<RouteDocumentation> routes;
    public final List<ResourceDocumentation> resources;
    public final Map<String, Object> model;

    public PluginDocumentation(
      String pluginDescription,
      List<DependencyDocumentation> dependencies,
      List<RouteDocumentation> routes,
      List<ResourceDocumentation> resources, Map<String, Object> model) {
        this.pluginDescription = StringUtils.replace(pluginDescription, "*", "");
        this.dependencies = dependencies;
        this.routes = routes;
        this.resources = resources;
        this.model = model;
    }
}
