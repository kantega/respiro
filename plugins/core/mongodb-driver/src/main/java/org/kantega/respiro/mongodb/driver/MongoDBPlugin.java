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

import org.kantega.respiro.mongodb.MongoDBBuilder;
import org.kantega.respiro.mongodb.MongoDatabaseProviderModifier;
import org.kantega.respiro.mongodb.MongoInitializer;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import java.util.Collection;

@Plugin
public class MongoDBPlugin {

    @Export
    private final MongoDBBuilder builder;

    public MongoDBPlugin(Collection<MongoInitializer> initializers,
                         Collection<MongoDatabaseProviderModifier> modifiers) {
        this.builder = new DefaultMongoDBBuilder(modifiers);
        
        initializers.forEach(MongoInitializer::initialize);
    }
}
