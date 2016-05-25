package org.kantega.respiro.documenter;

import fj.data.List;

public class Documentation {

    public final List<DependencyDocumentation> dependencies;
    public final List<RouteDocumentation> routes;
    public final List<ResourceDocumentation> resources;

    public Documentation(
      List<DependencyDocumentation> dependencies,
      List<RouteDocumentation> routes,
      List<ResourceDocumentation> resources) {
        this.dependencies = dependencies;
        this.routes = routes;
        this.resources = resources;
    }
}
