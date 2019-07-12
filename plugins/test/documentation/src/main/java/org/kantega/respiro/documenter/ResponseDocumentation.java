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

import fj.data.Option;

public class ResponseDocumentation {

    public final String headers;
    public final String body;
    public final String status;


    public ResponseDocumentation(String headers, String body, String status) {
        this.headers = Option.fromNull(headers).orSome("");
        this.body = Option.fromNull(body).orSome("");
        this.status = Option.fromNull(status).orSome("");
    }

    @Override
    public String toString() {
        return "ResponseDocumentation{" +
          "headers='" + headers + '\'' +
          ", body='" + body + '\'' +
          ", status='" + status + '\'' +
          '}';
    }
}
