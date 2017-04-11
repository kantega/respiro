# Respiro
Modular, developer-friendly integration platform

# Release notes
## Respiro 2.1 
* DataSourceInitializers were too tight coupeled to JdbcPlugin as they were used by various datasources(MongoDb, Jms etc.) and would fail to initialize if the final configuration did not have a Jdbc DataSource. Renamed to Initializer and is now beeing initialized by respiro-api plugin.
