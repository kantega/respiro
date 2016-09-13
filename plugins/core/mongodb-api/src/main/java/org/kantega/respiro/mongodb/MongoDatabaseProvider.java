package org.kantega.respiro.mongodb;

import com.mongodb.client.MongoDatabase;

public interface MongoDatabaseProvider {

    MongoDatabase getDatabase(String name);

}
