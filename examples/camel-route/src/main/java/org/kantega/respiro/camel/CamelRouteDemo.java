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

    @Export final RouteBuilder incomingRoute;
    @Export final RouteBuilder remoteFilesRoute;

    public CamelRouteDemo(@Config int port,
                          @Config String knownHostsFile) {

        remoteFilesRoute = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(SFTPConfigBuilder.sftp(knownHostsFile, "localhost", port, "/remotefiles", "target/work").auth("joe", "joe").build())
                        .to("file:target/fetchedfiles");
            }
        };

        incomingRoute = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                onException(BadNameException.class)
                        .handled(true)
                        .to("file:target/badnames");

                from("file:target/incoming")
                        .process(exchange -> {
                            GenericFile<File> file = (GenericFile<File>) exchange.getIn().getBody();
                            System.out.println("Processing " + file);
                            if (file.getFile().getName().startsWith("yo"))
                                throw new BadNameException("inappropriate file");

                        })
                        .to("file:target/processed");


            }
        };
    }
}
