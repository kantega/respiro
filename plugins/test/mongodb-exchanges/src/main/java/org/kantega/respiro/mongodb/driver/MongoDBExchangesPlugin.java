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
import com.mongodb.client.model.CreateViewOptions;
import com.mongodb.session.ClientSession;
import de.flapdoodle.embed.mongo.runtime.Mongod;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.kantega.respiro.mongodb.MongoDatabaseProviderModifier;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import java.util.List;

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

        MongoDbWrapper(MongoDatabase wrapped) {
            this.wrapped = wrapped;
        }


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
            return wrapped.withReadConcern(readConcern);
        }

        @Override
        public MongoCollection<Document> getCollection(String collectionName) {
            return wrapped.getCollection(collectionName);
        }

        @Override
        public <TDocument> MongoCollection<TDocument> getCollection(
            String collectionName, Class<TDocument> tDocumentClass) {
            return wrapped.getCollection(collectionName,tDocumentClass);
        }

        @Override
        public Document runCommand(Bson command) {
            return wrapped.runCommand(command);
        }

        @Override
        public Document runCommand(Bson command, ReadPreference readPreference) {
            return runCommand(command, readPreference);
        }

        @Override
        public <TResult> TResult runCommand(Bson command, Class<TResult> tResultClass) {
            return wrapped.runCommand(command,tResultClass);
        }

        @Override
        public <TResult> TResult runCommand(Bson command, ReadPreference readPreference, Class<TResult> tResultClass) {
            return wrapped.runCommand(command,readPreference,tResultClass);
        }

        @Override
        public Document runCommand(ClientSession clientSession, Bson bson) {
            return wrapped.runCommand(clientSession, bson);
        }

        @Override
        public Document runCommand(ClientSession clientSession, Bson bson, ReadPreference readPreference) {
            return wrapped.runCommand(clientSession, bson, readPreference);
        }

        @Override
        public <TResult> TResult runCommand(ClientSession clientSession, Bson bson, Class<TResult> aClass) {
            return wrapped.runCommand(clientSession, bson, aClass);
        }

        @Override
        public <TResult> TResult runCommand(ClientSession clientSession, Bson bson, ReadPreference readPreference, Class<TResult> aClass) {
            return wrapped.runCommand(clientSession, bson, readPreference, aClass);
        }

        @Override
        public void drop() {
            wrapped.drop();
        }

        @Override
        public void drop(ClientSession clientSession) {
            wrapped.drop(clientSession);
        }

        @Override
        public MongoIterable<String> listCollectionNames() {
            return wrapped.listCollectionNames();
        }

        @Override
        public ListCollectionsIterable<Document> listCollections() {
            return wrapped.listCollections();
        }

        @Override
        public <TResult> ListCollectionsIterable<TResult> listCollections(Class<TResult> tResultClass) {
            return wrapped.listCollections(tResultClass);
        }

        @Override
        public MongoIterable<String> listCollectionNames(ClientSession clientSession) {
            return wrapped.listCollectionNames(clientSession);
        }

        @Override
        public ListCollectionsIterable<Document> listCollections(ClientSession clientSession) {
            return wrapped.listCollections(clientSession);
        }

        @Override
        public <TResult> ListCollectionsIterable<TResult> listCollections(ClientSession clientSession, Class<TResult> aClass) {
            return wrapped.listCollections(clientSession,aClass);
        }

        @Override
        public void createCollection(String collectionName) {
            wrapped.createCollection(collectionName);
        }

        @Override
        public void createCollection(
            String collectionName, CreateCollectionOptions createCollectionOptions) {
            wrapped.createCollection(collectionName,createCollectionOptions);

        }

        @Override
        public void createCollection(ClientSession clientSession, String s) {

            wrapped.createCollection(clientSession,s);
        }

        @Override
        public void createCollection(ClientSession clientSession, String s, CreateCollectionOptions createCollectionOptions) {

            wrapped.createCollection(clientSession,s,createCollectionOptions);
        }

        @Override
        public void createView(String s, String s1, List<? extends Bson> list) {

            wrapped.createView(s,s1,list);
        }

        @Override
        public void createView(String s, String s1, List<? extends Bson> list, CreateViewOptions createViewOptions) {
            wrapped.createView(s,s1,list,createViewOptions);
        }

        @Override
        public void createView(ClientSession clientSession, String s, String s1, List<? extends Bson> list) {

            wrapped.createView(clientSession,s,s1,list);
        }

        @Override
        public void createView(ClientSession clientSession, String s, String s1, List<? extends Bson> list, CreateViewOptions createViewOptions) {

            wrapped.createView(clientSession, s, s1, list,createViewOptions);
        }
    }


}
