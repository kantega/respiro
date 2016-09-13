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

import com.mongodb.client.MongoDatabase;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Plugin
public class MongoDBPlugin {

    final static Logger logger =
      LoggerFactory.getLogger(MongoDBPlugin.class);
    @Export
    private final MongoDatabaseProviderModifier modifier;

    public MongoDBPlugin() {
        this.modifier =
        dbp->{
            logger.warn(" ******** Attention please *********");
            logger.warn(" You are using the respoiro-mongodb-test-driver-plugin, and you have just modified the MongoDatabase provider to be used for testing. Databases will always be dropped before they are handed over to you.");
            logger.warn(" If you are using this in production, switch to the respiro-mongodb-plugin if you want your data to survive server restarts.");
            logger.warn(" ");
            return dbname -> {
                MongoDatabase db = dbp.getDatabase(dbname);
                db.drop();
                return db;
            };
        };
    }
}
