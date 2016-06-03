package org.kantega.respiro.documenter.flow;

import fj.data.List;


public class Model {

    public static Model emptyModel =  new Model(List.nil(),List.nil());

    public final List<Node> nodes;
    public final List<Edge> edges;



    private Model(List<Node> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }


    public Model add(Node node){
        return new Model(nodes.cons(node),edges);
    }

    public Model add(Edge edge){
        return new Model(nodes, edges.cons(edge));
    }

}
