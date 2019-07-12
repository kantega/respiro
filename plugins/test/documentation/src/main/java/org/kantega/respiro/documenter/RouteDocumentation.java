/*
 * Copyright 2019 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public final String id;
    public final List<RouteInputDocumentation> inputs;
    public final List<RouteNodeDocumentation> route;

    public RouteDocumentation(String id, List<RouteInputDocumentation> inputs, List<RouteNodeDocumentation> route) {
        this.id = id;
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
            .map(fromDefinition -> new RouteInputDocumentation(fromDefinition.getId(), fromDefinition.getLabel()));

        List<RouteNodeDocumentation> outputs =
          iterableList(routeDefinition.getOutputs()).map(RouteDocumentation::getDoc);



        return new RouteDocumentation(routeDefinition.getId(), inputs, outputs);
    }

    private static RouteNodeDocumentation getDoc(ProcessorDefinition<?> def) {
        return
          new RouteNodeDocumentation(def.getId(), def.getLabel(), iterableList(def.getOutputs()).map(RouteDocumentation::getDoc));
    }

    public List<String> collectLabels(){
        return
          inputs
            .map(i->i.label)
            .append(route.bind(RouteDocumentation::labelsForOutput));
    }

    private static List<String> labelsForOutput(RouteNodeDocumentation node){
        return List.single(node.label).append(node.next.bind(RouteDocumentation::labelsForOutput));
    }
}
