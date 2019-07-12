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
