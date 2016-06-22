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

package org.kantega.respiro.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DefaultMongoDBBuilder implements MongoDBBuilder {

    final static Logger logger = LoggerFactory.getLogger(DefaultMongoDBBuilder.class);

    public DefaultMongoDBBuilder() {
    }

    @Override
    public Build mongodatabase(String serverAddress, int port, String databaseName) {
        return new DefaultBuild(serverAddress, port, databaseName);
    }

    private class DefaultBuild implements MongoDBBuilder.Build {

        private String username;
        private String password;
        private final List<ServerAddress> serverAddresses = new ArrayList<>();
        private String databaseName;


        public DefaultBuild(String serverAddress, int port, String databaseName) {
            serverAddresses.add(new ServerAddress(serverAddress, port));
            this.databaseName = databaseName;
        }

        @Override
        public Build auth(String username, String password) {
            this.password = password;
            this.username = username;
            return this;
        }

        @Override
        public Build serverAddress(String server, int port) {
            ServerAddress serverAddress = new ServerAddress(server, port);
            serverAddresses.add(serverAddress);
            return this;
        }

        @Override
        public Build databaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        @Override
        public MongoDatabase build() {
            MongoClient client;
            if (username != null && password != null) {
                MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(username, databaseName, password.toCharArray());
                List<MongoCredential> credentials = new ArrayList<>();
                credentials.add(mongoCredential);
                client = new MongoClient(serverAddresses, credentials);
            } else {
                client = new MongoClient(serverAddresses);
            }

            return client.getDatabase(databaseName);
        }
    }
}
