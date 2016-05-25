package org.kantega.respiro.documenter;

import fj.Ord;
import fj.P;
import fj.Show;
import fj.data.Option;
import fj.data.Stream;

import static fj.Ord.*;
import static org.kantega.respiro.documenter.Strings.*;

public class DependencyDocumentation {

    public static final Show<DependencyDocumentation> loggerShow =
      Show.show(dd -> nil
        .append(Stream.fromString(dd.type))
        .append(space)
        .append(Stream.fromString(dd.url)));


    public static final Ord<DependencyDocumentation> ord =
      p2Ord(stringOrd, stringOrd).contramap(dd -> P.p(dd.type, dd.url));

    public final String type;
    public final String url;

    public DependencyDocumentation(String type, String url) {
        this.type = Option.fromNull(type).orSome("");
        this.url = Option.fromNull(url).orSome("");
    }

    @Override
    public String toString() {
        return "DependencyDocumentation{" +
          "type='" + type + '\'' +
          ", url='" + url + '\'' +
          '}';
    }
}
