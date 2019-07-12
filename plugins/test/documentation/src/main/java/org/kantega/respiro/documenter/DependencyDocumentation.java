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

import fj.*;
import fj.data.Option;
import fj.data.Stream;
import fj.function.Booleans;
import org.apache.commons.lang3.StringUtils;

import static fj.Equal.*;
import static fj.Ord.*;
import static fj.data.Option.*;
import static fj.function.Booleans.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.kantega.respiro.documenter.Strings.*;

public class DependencyDocumentation {

    private static final F<String, Boolean> isMklabel =
      or(stringEqual.eq("sftp"), or(stringEqual.eq("ftp"), stringEqual.eq("file")));

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
        this.type = fromNull(type).orSome("");
        this.url = fromNull(url).orSome("");
    }

    public static Option<DependencyDocumentation> fromRouteLabel(String label) {
        String type = substringBefore(label, ":");
        String address = substringBefore(substringAfter(label, ":"),"?");
        if (isMklabel.f(type)) {
            return some(new DependencyDocumentation(type.toUpperCase(), address));
        } else {
            return none();
        }
    }

    @Override
    public String toString() {
        return "DependencyDocumentation{" +
          "type='" + type + '\'' +
          ", url='" + url + '\'' +
          '}';
    }


}
