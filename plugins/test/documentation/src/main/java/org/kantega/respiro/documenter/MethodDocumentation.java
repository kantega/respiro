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
import org.apache.commons.lang3.StringUtils;

import static fj.data.Stream.fromString;
import static org.kantega.respiro.documenter.Strings.mkString;
import static org.kantega.respiro.documenter.Strings.nl;

public class MethodDocumentation {

    public final static Show<MethodDocumentation> loggerShow =
      Show.show(md -> Strings
        .nil
        .append(fromString("Endpoint: ")).append(Stream.fromString(md.method)).append(Strings.space).append(Stream.fromString(md.path)).append(nl)
        .append(fromString("Parameters: ")).append(mkString(Show.stringShow, ",").show(md.parameters).append(nl))
        .append(fromString("Roles allowed: ")).append(mkString(Show.stringShow, ",").show(md.rolesAllowed))
        .append(fromString(md.documentation)).append(nl)
        .append(fromString("Examples:")).append(nl)
        .append(mkString(ExchangeDocumentation.loggerShow, "\n").show(md.exchangeDocumentations)).append(nl).append(nl));

   public final String path;
    public final String method;
    public final List<String> rolesAllowed;
    public final String documentation;
    public final List<String> parameters;
    public final List<ExchangeDocumentation> exchangeDocumentations;

    public MethodDocumentation(
      String path,
      String method,
      List<String> rolesAllowed,
      String documentation,
      List<String> parameters,
      List<ExchangeDocumentation> exchangeDocumentations) {
        this.path = StringUtils.remove(path, "\"");
        this.method = method;
        this.rolesAllowed = rolesAllowed;
        this.documentation = StringUtils.remove(documentation, "*");
        this.parameters = parameters;
        this.exchangeDocumentations = exchangeDocumentations;
    }

    @Override
    public String toString() {
        return "MethodDocumentation{" +
          "path='" + path + '\'' +
          ", method='" + method + '\'' +
          ", rolesAllowed=" + rolesAllowed +
          ", documentation='" + documentation + '\'' +
          ", parameters=" + parameters +
          ", exchangeDocumentations=" + exchangeDocumentations +
          '}';
    }

    public MethodDocumentation withRecorded(List<ExchangeDocumentation> exDocs) {
        return new MethodDocumentation(path, method, rolesAllowed, documentation, parameters, exDocs);
    }
}
