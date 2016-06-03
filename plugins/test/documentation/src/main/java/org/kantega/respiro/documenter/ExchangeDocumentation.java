package org.kantega.respiro.documenter;

import fj.Show;
import fj.data.Stream;

import static org.kantega.respiro.documenter.Strings.nil;
import static org.kantega.respiro.documenter.Strings.nl;

public class ExchangeDocumentation {
    public static final Show<ExchangeDocumentation> loggerShow =
      Show.show(ed-> nil.append(Stream.fromString("Request:")).append(nl)
        .append(Stream.fromString(ed.requestDocumentation.url)).append(nl)
        .append(Stream.fromString(ed.requestDocumentation.body)).append(nl)
        .append(Stream.fromString("Response: ")).append(Stream.fromString(ed.responseDocumentation.status)).append(nl)
        .append(Stream.fromString(ed.responseDocumentation.body)));

    public final RequestDocumentation requestDocumentation;
    public final ResponseDocumentation responseDocumentation;

    public ExchangeDocumentation(RequestDocumentation requestDocumentation, ResponseDocumentation responseDocumentation) {
        this.requestDocumentation = requestDocumentation;
        this.responseDocumentation = responseDocumentation;
    }

    @Override
    public String toString() {
        return "ExchangeDocumentation{" +
          "requestDocumentation=" + requestDocumentation +
          ", responseDocumentation=" + responseDocumentation +
          '}';
    }
}
