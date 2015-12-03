package org.kantega.respiro.activemq.testbroker;

import org.apache.activemq.broker.BrokerService;
import org.kantega.respiro.api.DataSourceInitializer;
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
public class TestActiveMqBrokerPlugin implements DataSourceInitializer {


    private final String basedir = getProperty("reststopPluginDir");

    @Export final DataSourceInitializer dataSourceInitializer = this;
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
