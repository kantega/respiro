/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro.camel;

import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.GenericFile;
import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import java.io.File;

/**
 *
 */
@Plugin
public class CamelRouteDemo {

    @Export
    final RoutesBuilder helloRoute;

    public CamelRouteDemo(@Config int port,
                          @Config String knownHostsFile) {

        helloRoute = new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                onException(RuntimeException.class)
                        .to("file:target/failed");

                from("servlet:///hello")
                        .process(exchange -> {
                            String contentType = exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class);
                            String path = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
                            path = path.substring(path.lastIndexOf("/"));

                            // assert camel http header
                            String charsetEncoding = exchange.getIn().getHeader(Exchange.HTTP_CHARACTER_ENCODING, String.class);
                            // assert exchange charset
                            exchange.getOut().setHeader(Exchange.CONTENT_TYPE, contentType + "; charset=UTF-8");
                            exchange.getOut().setHeader("PATH", path);
                            exchange.getOut().setBody("<b>Hei Verden!</b>");
                        });

                from("file:target/incoming")
                        .process(exchange -> {
                            GenericFile<File> file = (GenericFile<File>) exchange.getIn().getBody();
                            System.out.println("Procesing " + file);
                            if (file.getFile().getName().startsWith("yo"))
                                throw new RuntimeException("inappropriate file");

                        })
                        .to("file:target/processed");

                from(SFTPConfigBuilder.sftp(knownHostsFile, "localhost", port, "/remotefiles", "target/work").auth("joe", "joe").build())
                        .to("file:target/fetchedfiles");
                from("servlet:///goddag").to("file:target/goddags");
            }
        };

    }
}
