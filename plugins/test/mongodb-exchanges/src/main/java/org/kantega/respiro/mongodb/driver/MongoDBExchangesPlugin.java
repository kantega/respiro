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

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import de.flapdoodle.embed.mongo.runtime.Mongod;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.kantega.respiro.mongodb.MongoDatabaseProviderModifier;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

@Plugin
public class MongoDBExchangesPlugin {

    @Export
    private final MongoDatabaseProviderModifier modifier;


    public MongoDBExchangesPlugin() {
        modifier =
          mdbp -> {
              return dbname -> {
                  MongoDatabase db =
                    mdbp.getDatabase(dbname);

                  return new MongoDbWrapper(db);
              };
          };
    }

    static class MongoDbWrapper implements MongoDatabase {
        final MongoDatabase wrapped;

        MongoDbWrapper(MongoDatabase wrapped) {this.wrapped = wrapped;}


        @Override
        public String getName() {
            return wrapped.getName();
        }

        @Override
        public CodecRegistry getCodecRegistry() {
            return wrapped.getCodecRegistry();
        }

        @Override
        public ReadPreference getReadPreference() {
            return wrapped.getReadPreference();
        }

        @Override
        public WriteConcern getWriteConcern() {
            return wrapped.getWriteConcern();
        }

        @Override
        public ReadConcern getReadConcern() {
            return wrapped.getReadConcern();
        }

        @Override
        public MongoDatabase withCodecRegistry(CodecRegistry codecRegistry) {
            return new MongoDbWrapper(wrapped.withCodecRegistry(codecRegistry));
        }

        @Override
        public MongoDatabase withReadPreference(ReadPreference readPreference) {
            return new MongoDbWrapper(wrapped.withReadPreference(readPreference));
        }

        @Override
        public MongoDatabase withWriteConcern(WriteConcern writeConcern) {
            return new MongoDbWrapper(wrapped.withWriteConcern(writeConcern));
        }

        @Override
        public MongoDatabase withReadConcern(ReadConcern readConcern) {
            return null;
        }

        @Override
        public MongoCollection<Document> getCollection(String collectionName) {
            return null;
        }

        @Override
        public <TDocument> MongoCollection<TDocument> getCollection(
          String collectionName, Class<TDocument> tDocumentClass) {
            return null;
        }

        @Override
        public Document runCommand(Bson command) {
            return null;
        }

        @Override
        public Document runCommand(Bson command, ReadPreference readPreference) {
            return null;
        }

        @Override
        public <TResult> TResult runCommand(Bson command, Class<TResult> tResultClass) {
            return null;
        }

        @Override
        public <TResult> TResult runCommand(Bson command, ReadPreference readPreference, Class<TResult> tResultClass) {
            return null;
        }

        @Override
        public void drop() {

        }

        @Override
        public MongoIterable<String> listCollectionNames() {
            return null;
        }

        @Override
        public ListCollectionsIterable<Document> listCollections() {
            return null;
        }

        @Override
        public <TResult> ListCollectionsIterable<TResult> listCollections(Class<TResult> tResultClass) {
            return null;
        }

        @Override
        public void createCollection(String collectionName) {

        }

        @Override
        public void createCollection(
          String collectionName, CreateCollectionOptions createCollectionOptions) {

        }
    }


}
