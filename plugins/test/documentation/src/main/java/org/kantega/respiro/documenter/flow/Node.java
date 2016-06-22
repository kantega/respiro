package org.kantega.respiro.documenter.flow;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class Node {
    public final String id;
    public final String label;
    public final NodeType type;

    private Node(String id, String label, NodeType type) {
        this.id = id;
        this.label = label;
        this.type = type;
    }

    public static Node Node(String label,NodeType type){
        return new Node(UUID.randomUUID().toString(), StringUtils.substringBefore(label,"?"), type);
    }
}
