package org.kantega.respiro.documenter;

import fj.Show;
import fj.data.List;
import fj.data.Stream;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;

import static fj.data.List.*;
import static org.kantega.respiro.documenter.Strings.*;

public class RouteDocumentation {


    public static final Show<RouteDocumentation> loggerShow =
      Show.show(rd ->
        Stream
          .fromString("Route:")
          .append(nl)
          .append(toIndent(1).append(lparen.append(mkString(RouteInputDocumentation.loggerShow, ", ").show(rd.inputs).append(rparen).append(nl))))
          .append(toIndent(2).append(mkString(RouteNodeDocumentation.loggerShow(3),"\n").show(rd.route)))
      );

    public final List<RouteInputDocumentation> inputs;
    public final List<RouteNodeDocumentation> route;

    public RouteDocumentation(List<RouteInputDocumentation> inputs, List<RouteNodeDocumentation> route) {
        this.inputs = inputs;
        this.route = route;
    }

    @Override
    public String toString() {
        return "RouteDocumentation{" +
          "inputs=" + inputs +
          ", route=" + route +
          '}';
    }

    public static RouteDocumentation fromRoute(RouteDefinition routeDefinition) {
        List<RouteInputDocumentation> inputs =
          iterableList(routeDefinition.getInputs())
            .map(fromDefinition -> new RouteInputDocumentation(fromDefinition.getLabel()));

        List<RouteNodeDocumentation> outputs =
          iterableList(routeDefinition.getOutputs()).map(RouteDocumentation::getDoc);

        return new RouteDocumentation(inputs, outputs);
    }

    private static RouteNodeDocumentation getDoc(ProcessorDefinition<?> def) {
        return
          new RouteNodeDocumentation(def.getLabel(), iterableList(def.getOutputs()).map(RouteDocumentation::getDoc));
    }
}
