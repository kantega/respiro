package org.kantega.respiro.documenter.flow;

public class Edge {


    public final Node from;
    public final Node to;

    private Edge( Node from, Node to) {
        this.from = from;
        this.to = to;
    }

    public static Edge Edge(Node from, Node to) {
        return new Edge(from, to);
    }
}
