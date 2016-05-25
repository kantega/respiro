package org.kantega.respiro.documenter;

import fj.Show;

public class RouteInputDocumentation {

    public static final Show<RouteInputDocumentation> loggerShow =
      Show.stringShow.contramap(rid->rid.label);

    public final String label;

    public RouteInputDocumentation(String label) {this.label = label;}
}
