package org.kantega.respiro.documenter;

import fj.data.Option;

public class RequestDocumentation {

    public final String url;
    public final String headers;
    public final String body;

    public RequestDocumentation(String url, String headers, String body) {
        this.url = url;
        this.headers = Option.fromNull(headers).orSome("");
        this.body = Option.fromNull(body).orSome("");
    }

    @Override
    public String toString() {
        return "RequestDocumentation{" +
          "url='" + url + '\'' +
          ", headers='" + headers + '\'' +
          ", body='" + body + '\'' +
          '}';
    }
}
