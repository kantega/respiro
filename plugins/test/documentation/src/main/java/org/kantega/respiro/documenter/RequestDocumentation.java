package org.kantega.respiro.documenter;

import fj.data.Option;

public class RequestDocumentation {

    public final String headers;
    public final String body;

    public RequestDocumentation(String headers, String body) {
        this.headers = Option.fromNull(headers).orSome("");
        this.body = Option.fromNull(body).orSome("");
    }

    @Override
    public String toString() {
        return "RequestDocumentation{" +
          "headers='" + headers + '\'' +
          ", body='" + body + '\'' +
          '}';
    }
}
