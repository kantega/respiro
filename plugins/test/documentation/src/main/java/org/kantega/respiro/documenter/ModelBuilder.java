package org.kantega.respiro.documenter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fj.F;
import fj.data.List;
import org.apache.commons.lang3.StringUtils;
import org.kantega.respiro.documenter.flow.Edge;
import org.kantega.respiro.documenter.flow.Model;
import org.kantega.respiro.documenter.flow.Node;
import org.kantega.respiro.documenter.flow.NodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.kantega.respiro.documenter.flow.Edge.*;
import static org.kantega.respiro.documenter.flow.Node.*;


public class ModelBuilder {

    public static Model extractModel(List<RouteDocumentation> routes) {
        return routes.foldLeft((m, rDoc) -> augment(rDoc, m), Model.emptyModel);
    }


    public static Model augment(RouteDocumentation routeDocumentation, Model model) {

        F<Model, Model> modifier =
          routeDocumentation.inputs.isSingle() ?
          mo -> {
              Node startEvent = Node(routeDocumentation.inputs.head().label, NodeType.event);
              Node fetchTask = Node("fetch", NodeType.task);
              Edge edge = Edge(startEvent, fetchTask);
              return routeDocumentation.route.foldLeft((m, procs) -> append(procs, fetchTask, m), mo.add(startEvent).add(fetchTask).add(edge));
          } :
          mo -> {
              Node gate = Node("", NodeType.xorGate);

              Model modelWithInputs =
                routeDocumentation.inputs.foldLeft((m, input) -> {
                    Node startEvent = Node(input.label, NodeType.event);
                    Node fetchTask = Node("fetch", NodeType.task);
                    Edge edge = Edge(startEvent, fetchTask);
                    Edge edge2 = Edge(fetchTask, gate);
                    return m.add(startEvent).add(fetchTask).add(edge).add(edge2);
                }, mo.add(gate));

              return routeDocumentation.route.foldLeft((m, procs) -> append(procs, gate, m), modelWithInputs);
          };

        return modifier.f(model);
    }

    public static Model append(RouteNodeDocumentation processTask, Node prev, Model model) {
        Node task = Node(processTask.label, NodeType.task);
        Edge edge = Edge(prev, task);
        return processTask.next.foldLeft((m, proc) -> append(proc, task, m), model.add(task).add(edge));
    }

    public static Map<String, Object> toJson(Model model) {
        HashMap<String, Object> json = new HashMap<>();
        ArrayList<Object> nodes = new ArrayList<>();

        model.nodes.forEach(n -> {
            HashMap<String, Object> nodeJson = new HashMap<>();
            nodeJson.put("id", n.id);
            nodeJson.put("label", n.label);
            nodeJson.put("nodeType", n.type.name());
            HashMap<String, Object> dataJson = new HashMap<>();
            dataJson.put("data", nodeJson);
            nodes.add(dataJson);
        });

        ArrayList<Object> edges = new ArrayList<>();
        model.edges.forEach(e -> {
            HashMap<String, Object> edgeJson = new HashMap<>();
            edgeJson.put("source", e.from.id);
            edgeJson.put("target", e.to.id);
            HashMap<String, Object> dataJson = new HashMap<>();
            dataJson.put("data", edgeJson);
            edges.add(dataJson);
        });

        json.put("nodes", nodes);
        json.put("edges", edges);
        return json;

    }

}
