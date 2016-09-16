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

package org.kantega.respiro.mongodb.driver;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.kantega.respiro.mongodb.MongoDBBuilder;
import org.kantega.respiro.mongodb.MongoDatabaseProvider;
import org.kantega.respiro.mongodb.MongoDatabaseProviderModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DefaultMongoDBBuilder implements MongoDBBuilder {

    final Collection<MongoDatabaseProviderModifier> modifiers;

    public DefaultMongoDBBuilder(Collection<MongoDatabaseProviderModifier> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public Build mongodatabase(List<ServerAddress> serverAddresses) {
        return new DefaultBuild(serverAddresses);
    }

    @Override
    public Build mongodatabase(String addressList) {
        String[] addresses = addressList.split(",");
        List<ServerAddress> srvAddresses = new ArrayList<>();
        for (String a : addresses) {
            String address[] = a.split(":");
            if (address.length < 2)
                throw new RuntimeException(String.format("Server address cannot be split into host and port '%s', a"));

            srvAddresses.add(new ServerAddress(address[0], Integer.valueOf(address[1])));
        }

        return mongodatabase(srvAddresses);
    }

    private class DefaultBuild implements MongoDBBuilder.Build {

        private List<ServerAddress> serverAddresses = new ArrayList<>();
        final List<MongoCredential> credentials = new ArrayList<>();

        public DefaultBuild(List<ServerAddress> serverAddresses) {
            this.serverAddresses = serverAddresses;
        }

        @Override
        public Build auth(String username, String password, String database) {
            credentials.add(MongoCredential.createScramSha1Credential(username, database, password.toCharArray()));
            return this;
        }


        @Override
        public MongoDatabaseProvider build() {
            final MongoClient client =
                    credentials.isEmpty()
                            ? new MongoClient(serverAddresses)
                            : new MongoClient(serverAddresses, credentials);
            MongoDatabaseProvider mdp = client::getDatabase;
            MongoDatabaseProvider modified = mdp;

            for (MongoDatabaseProviderModifier modifier : modifiers) {
                modified = modifier.modify(modified);
            }
            return modified;
        }


    }

}
