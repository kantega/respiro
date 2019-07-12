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
