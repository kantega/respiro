package org.kantega.respiro.mongodb;

public interface MongoDatabaseProviderModifier {

    MongoDatabaseProvider modify(MongoDatabaseProvider provider);
}
