# Respiro
Modular, developer-friendly integration platform
[![Black Duck Security Risk](https://copilot.blackducksoftware.com/github/repos/helaar/respiro/branches/master/badge-risk.svg)](https://copilot.blackducksoftware.com/github/repos/helaar/respiro/branches/master)

# Release notes

## Respiro 2.25
* Added kerberos authentication support. See `respiro-kerberos-plugin` for more.
* Bugfix in html-formatted mail messages. Reordered `/body` and `/html` closing tags.
* [ActiveMQ 5.15.9](http://activemq.apache.org/activemq-5159-release.html)
* [Camel version 2.24.1](http://camel.apache.org/camel-2241-release.html) with workaround for classloader-problem 
  introduced by [CAMEL-13468](https://issues.apache.org/jira/browse/CAMEL-13468)  
* [Jackson Databind 2.9.9.1](https://github.com/FasterXML/jackson-databind)
* Removed JMS/ActiveMQ plugins. Use Camel JMS/ActiveMQ support instead!
* Removed Documenter plugin. Not in use/not maintained.

## Respiro 2.24
* [ActiveMQ versjon 5.15.8](http://activemq.apache.org/activemq-5158-release.html)
* [Camel version 2.19.5](http://camel.apache.org/camel-2241-release.html)
* Added camel-jms, camel-spring and camel-ftp dependency to camel-plugin
* Added system property `flapdoodleStartPort` to mongodb-test-plugin to allow flapdoodle start on fixed port.

## Respiro 2.23
* [Jackson Databind 2.9.8](https://github.com/FasterXML/jackson-databind)
* Bugfix on simple-java-mail to allow sender addresses on format `name <email@somewhere.com>`

## Respiro 2.22
* [Reststop 3.11.2](https://github.com/kantega/reststop/wiki/ReleaseNotes) 
* Reimplemented email support using simple-java-mail instead of apache-commons leads to minor api changes:
  * `replyTo` is now limited to one address. Multiple replyTo-addresses is no longer supported.
  * `charset` seems to have been linked to the apache-commons implementation and is thereby removed. 

## Respiro 2.21
* Correct header attribute name `Reply-To` in Mail sender customizer.
* New parameter `respiroExchangesSize` limits size of exchanges collection.
* [Apache cxf 3.2.7](http://cxf.apache.org/download.html)
* Transitive dependency to Apache commons-compress updated to 2.18 (flapdoodle 2.1.1) 

## Respiro 2.20.1
* Logging `replyTo` when collecting data from mail sender.
* Temporary fix for flapdoodle [Issue 232](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues/232): 
  Using development versjon when running on windows. 

## Respiro 2.20
* Added `replyTo`-support to mail-plugin
* [Flapdoodle 2.1.1](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/blob/master/Changelog.md)

## Respiro 2.19
* Bugfix in dummy rest services.
* [Reststop 3.10](https://github.com/kantega/reststop/wiki/ReleaseNotes)

## Respiro 2.18
* Added configuration parameter for maxPoolSize in DataSourceBuilder 
* [hikaryCP 3.2.0](https://github.com/brettwooldridge/HikariCP/wiki)

## Respiro 2.17.2
* [Google Guava 2.25.1](https://github.com/google/guava/releases/tag/v25.1)

(Respiro 2.17 & 2.17.1 failed release on oss.sonatype.org)

## Respiro 2.16
* [MongoDB Driver v 3.6.4](http://mongodb.github.io/mongo-java-driver/3.6/)
* [Flapdoodle v 2.0.1](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/blob/master/Changelog.md)

## Respiro 2.15
* [ActiveMQ 5.15.4](http://activemq.apache.org/activemq-5154-release.html)
* Excluded dependencies to jackson-databind libraries from jersey plugin

## Respiro 2.14
* [Jackson Databind 2.9.5](https://github.com/FasterXML/jackson-databind)
* [Reststop 3.9](https://github.com/kantega/reststop/wiki/ReleaseNotes)
* validation-api 2.0.1.Final
* [hibernate-validator 6.0.10.Final](http://hibernate.org/validator/documentation/) 
* [jersey 2.27](https://jersey.github.io/release-notes/2.27.html) 
* javax.ws.rs-api 2.1

## Respiro 2.13
* Added support for REST dummies returning text/html

## Respiro 2.12
* Added support for HTML MIME type in SMTPMailSender (respiro-mail plugin)

## Respiro 2.11
* [camel 1.19.4](http://camel.apache.org/camel-2194-release.html)
* [Apache cxf 3.2.1](http://cxf.apache.org/download.html)
* [ActiveMQ 5.15.2](http://activemq.apache.org/activemq-5152-release.html)
* Added a routing table feature to the dummy plugin. 

## Respiro 2.10
* [Apache Mina version 1.7.0](https://github.com/apache/mina-sshd) 
* Updated test-sshd-plugin due to changes in Apache Mina and fixing issue with java.security.InvalidKeyException on startup.

## Respiro 2.9
* Removed body filtering in response from dummy plugin as it messes up character encoding

## Respiro 2.8
* [Apache cxf 3.2.0](http://cxf.apache.org/download.html)
* Added jax-ws client-side Handler support to cxf-plugin 

## Respiro 2.7
* [Reststop 3.5](https://github.com/kantega/reststop/wiki/ReleaseNotes)

## Respiro 2.6 & 2.6.1
* Sender address is optional on smtp messages. Should be overridden by server config if not present.
* Updated third-party libraries:
  * [camel 1.19.3](http://camel.apache.org/camel-2193-release.html)
  * [reststop 3.3](https://github.com/kantega/reststop/wiki/ReleaseNotes)
  * [jersey 2.25.1](https://jersey.github.io/release-notes/2.25.1.html) 
  * [jackson-databind 2.9.1](https://github.com/FasterXML/jackson-databind)
  * [h2database 1.4.196](http://www.h2database.com/html/changelog.html)
  * [hikaryCP 2.7.1](https://github.com/brettwooldridge/HikariCP/wiki)
  * validation-api 2.0.0.Final
  * [hibernate-validator 6.0.2.Final](http://hibernate.org/validator/documentation/)

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
