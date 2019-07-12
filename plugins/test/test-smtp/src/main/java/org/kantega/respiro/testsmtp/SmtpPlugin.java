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

package org.kantega.respiro.testsmtp;

import com.dumbster.smtp.MailStore;
import com.dumbster.smtp.ServerOptions;
import com.dumbster.smtp.SmtpServer;
import org.kantega.respiro.api.ApplicationBuilder;
import org.kantega.respiro.api.Initializer;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import javax.annotation.PreDestroy;
import javax.ws.rs.core.Application;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.nio.file.Files;

/**
 *
 */
@Plugin
public class SmtpPlugin implements Initializer {

    private final SmtpServer server;
    private final Thread thread;

    @Export
    final Application smtpApp;
    
    @Export
    final Initializer initializer = this;

    public SmtpPlugin(ApplicationBuilder applicationBuilder) throws NoSuchFieldException, IllegalAccessException, IOException {

        ServerOptions options = new ServerOptions();
        options.port = 0;

        MailStore mailStore = new InMemoryMailStore();
        options.mailStore = mailStore;

        server = new SmtpServer();
        server.setPort(options.port);
        server.setThreaded(options.threaded);
        server.setMailStore(options.mailStore);

        thread = new Thread(server);
        thread.setDaemon(true);
        thread.start();
        int timeout = 1000;
        while (!server.isReady()) {
            try {
                Thread.sleep(1);
                timeout--;
                if (timeout < 1) {
                    throw new RuntimeException("Server could not be started.");
                }
            } catch (InterruptedException ignored) {
            }
        }

        Field serverSocketField = SmtpServer.class.getDeclaredField("serverSocket");
        serverSocketField.setAccessible(true);

        ServerSocket serverSocket = (ServerSocket) serverSocketField.get(server);

        serverSocket.getLocalPort();

        int port = serverSocket.getLocalPort();

        System.out.println("Started SMTP server on port " + serverSocket);

        Files.write(new File(System.getProperty("reststopPluginDir"), "target/test-classes/smtpPort.txt").toPath(),
            Integer.toString(port).getBytes());
        System.setProperty("smtpPort", Integer.toString(port));

        smtpApp = applicationBuilder.application().singleton(new SmtpdResource(mailStore)).build();

    }

    @PreDestroy
    public void stop() throws InterruptedException {
        server.stop();
        thread.join();
    }

    @Override
    public void initialize() {
        
    }
}
