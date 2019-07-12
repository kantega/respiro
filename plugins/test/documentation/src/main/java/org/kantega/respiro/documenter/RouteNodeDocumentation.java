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

import static org.kantega.respiro.documenter.Strings.*;

public class RouteNodeDocumentation {

    public final String id;
    public final String label;
    public final List<RouteNodeDocumentation> next;

    public RouteNodeDocumentation(String id, String label, List<RouteNodeDocumentation> next) {
        this.id = id;
        this.label = label;
        this.next = next;
    }

    public static Show<RouteNodeDocumentation> loggerShow(int indent) {
        return Show.show(rnd -> {
            Stream<Character> s = toIndent(indent).append(lparen).append(space).append(Stream.fromString(rnd.label)).append(space).append(rparen).append(nl);
            Stream<Character> withNext =
              rnd.next.isEmpty() ?
              s :
              s.append(mkString(RouteNodeDocumentation.loggerShow(indent + 1), "\n").show(rnd.next));
            return withNext;
        });
    }


}
