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

package org.kantega.respiro.test.database;

import org.kantega.respiro.api.DataSourceInitializer;
import org.h2.tools.Server;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.sql.DriverManager.getConnection;
import static org.h2.tools.Server.createTcpServer;

@Plugin
public class TestDatabasePlugin implements DataSourceInitializer {

    private final String basedir = getProperty("reststopPluginDir");
    private final Server srv;
    private final int port;
    List<Connection> connections = new ArrayList<>();

    @Export
    private final DataSourceInitializer initializer = this;

    public TestDatabasePlugin() throws SQLException, IOException {
        this.srv = createTcpServer("-tcpPort", "0").start();
        this.port = srv.getPort();
        setProperty("h2Port", Integer.toString(port));
        write(new File(basedir, "target/test-classes/h2Port.txt").toPath(), Integer.toString(port).getBytes());
    }

    private void loadTestdata() throws IOException, SQLException {

        File dummyBasedir = new File(basedir, "src/test/database");
        File[] dirs = dummyBasedir.listFiles(File::isDirectory);

        if (dirs != null) {
            for (File dir : dirs) {
                String url = format("jdbc:h2:tcp://localhost:" + port + "/mem:%s;MODE=Oracle", dir.getName());
                Connection conn = getConnection(url, "admin", "password");
                connections.add(conn);

                File[] files = dir.listFiles(pathname -> pathname.getName().endsWith(".sql"));
                for (File sqlFile : files) {
                    String fileContent = new String(readAllBytes(sqlFile.toPath()), "utf-8");
                    String expressions[] = fileContent.split(";");
                    for (String expression : expressions) {
                        conn.createStatement().executeUpdate(expression);
                    }
                }
                //conn.close();
            }
        }
    }

    @Override
    public void initialize() {
        try {
            loadTestdata();
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void destroy() {
        for (Connection connection : connections) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
