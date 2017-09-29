# Respiro
Modular, developer-friendly integration platform

# Release notes
## Respiro 2.6
* Updated third-party libraries:
  * [camel 1.19.3](http://camel.apache.org/camel-2193-release.html)
  * [reststop 3.3](https://github.com/kantega/reststop/wiki/ReleaseNotes)
  * [jersey 2.25.1](https://jersey.github.io/release-notes/2.25.1.html) 
  * [jackson-databind 2.9.1](https://github.com/FasterXML/jackson-databind)

## Respiro 2.5
* Changed mail configuration and smtp implementation. 
  * Removed recipient fields as those should be assigned to the message. 
  * Added possibility to override sender on outgoing messages.

## Respiro 2.4
* Added simple invocation tracing to dummy plugin (REST).  
  * Tracing can be queried using `GET /dummies/invocations`
  * Tracing can be reset using `DELETE /dummies/invocations`

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

## Respiro 2.2
* Dummy plugin: Added support for recognizing paths containing query parametres in rule.xml. 

## Respiro 2.1 
* DataSourceInitializers were too tight coupeled to JdbcPlugin as they were used by various datasources(MongoDb, Jms etc.) and would fail to initialize if the final configuration did not have a Jdbc DataSource. Renamed to Initializer and is now beeing initialized by respiro-api plugin.
