# Respiro
Modular, developer-friendly integration platform

# Release notes
## Respiro 2.1 
* DataSourceInitializers were too tight coupeled to JdbcPlugin as they were used by various datasources(MongoDb, Jms etc.) and would fail to initialize if the final configuration did not have a Jdbc DataSource. Renamed to Initializer and is now beeing initialized by respiro-api plugin.
## Respiro 2.2
* Dummy plugin: Added support for recognizing paths containing query parametres in rule.xml. 
## Respiro 2.3
* Bugfix in dummy plugin. When a rule is defined to match a url containing query parameters(Respiro 2.2), it would not match uri pattern when resource was declared auth=NONE.
* Supporting response-headers returned from dummy plugin (REST). Declare `response-headers` as element, containing wanted response as nodes:
 ```
 <rule>
     <method>POST</method>
     <path>/dummies/customers?apiKey=key</path>
     <content-type>application/json</content-type>
     <response-code>201</response-code>
     <response-headers>
         <Location>http://localhost:${reststopPort}/dummies/customers/100</Location>
     </response-headers>
 </rule>
 ```