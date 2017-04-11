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

package org.kantega.respiro.activemq.testbroker;

import org.apache.activemq.broker.BrokerService;
import org.kantega.respiro.api.Initializer;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import javax.annotation.PreDestroy;
import java.io.File;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.nio.file.Files.write;

/**
 *
 */
@Plugin
public class TestActiveMqBrokerPlugin implements Initializer {


    private final String basedir = getProperty("reststopPluginDir");

    @Export
    final Initializer dataSourceInitializer = this;
    
    private final BrokerService broker;

    public TestActiveMqBrokerPlugin() throws Exception {

        broker = new BrokerService();
        // configure the broker
        broker.addConnector("tcp://localhost:0");
        broker.setDataDirectoryFile(new File(basedir, "target/activeMqData"));
        broker.setUseShutdownHook(false);
        broker.start();
        int port = broker.getTransportConnectors().get(0).getServer().getSocketAddress().getPort();

        setProperty("activeMqPort", Integer.toString(port));
        write(new File(basedir, "target/test-classes/activeMqPort.txt").toPath(), Integer.toString(port).getBytes());

    }

    @PreDestroy
    public void destroy() throws Exception {
        broker.stop();
    }

    @Override
    public void initialize() {

    }
}
