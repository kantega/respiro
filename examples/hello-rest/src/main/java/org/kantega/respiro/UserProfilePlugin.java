/*
 * Copyright 2019 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro;

import com.mongodb.client.MongoDatabase;
import org.kantega.respiro.api.ApplicationBuilder;
import org.kantega.respiro.api.DataSourceBuilder;
import org.kantega.respiro.api.mail.MailConfigBuilder;
import org.kantega.respiro.api.mail.MailSender;
import org.kantega.respiro.mongodb.MongoDBBuilder;
import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import javax.ws.rs.core.Application;

/**
 *
 */
@Plugin
public class UserProfilePlugin {



    @Export final Application exampleApplication;



    public UserProfilePlugin(@Config String jdbcDriverClass,
                             @Config String helloDatabaseUsername,
                             @Config String helloDatabasePassword,
                             @Config String helloDatabaseUrl,
                             @Config String smtpAddress,
                             @Config String smtpFrom,
                             @Config(defaultValue = "25") int smtpPort,
                             @Config String mongoDbServers,
                             @Config String mongoDbDatabase,
                             @Config String mongoDbUsername,
                             @Config String mongoDbPassword,
                             ApplicationBuilder builder,
                             DataSourceBuilder dsBuilder,
                             MongoDBBuilder mongoBuilder,
                             MailConfigBuilder mailConfigBuilder) {
        
        // testing dependecy only
        final MongoDatabase db = mongoBuilder
            .mongodatabase(mongoDbServers)
            .auth(mongoDbUsername, mongoDbPassword, mongoDbDatabase)
            .build().getDatabase(mongoDbDatabase);
        
        final DataSource myDataSource = dsBuilder.datasource(helloDatabaseUrl)
                .username(helloDatabaseUsername).password(helloDatabasePassword).driverClassname(jdbcDriverClass).build();
        final UsersDAO dao = new UsersDAO(myDataSource);

        final MailSender sender = mailConfigBuilder.server(smtpAddress,smtpPort, smtpFrom).build();
        exampleApplication = builder.application().singleton(new UserProfileResource(dao, sender)).build();

    }
    
}