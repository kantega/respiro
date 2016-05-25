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
