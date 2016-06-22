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

package org.kantega.respiro.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.kantega.respiro.api.DataSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Collection;

public class DefaultDataSourceBuilder implements DataSourceBuilder {
    private final Collection<DataSourceCustomizer> dataSourceCustomizers;
    private final long defaultMaxAge;
    final static Logger logger = LoggerFactory.getLogger(DefaultDataSourceBuilder.class);


    public DefaultDataSourceBuilder(Collection<DataSourceCustomizer> dataSourceCustomizers, long maxAge) {
        this.defaultMaxAge = maxAge;
        this.dataSourceCustomizers = dataSourceCustomizers;
    }

    @Override
    public Build datasource(String url) {
        return new DefaultBuild(url, defaultMaxAge);
    }

    private class DefaultBuild implements Build {


        private final String url;
        private String username;
        private String password;
        private String driverClassname;
        private long maxAge;

        public DefaultBuild(String url, long defaultMaxAge) {
            this.url = url;
            this.maxAge = defaultMaxAge;
        }

        @Override
        public Build username(String username) {
            this.username = username;
            return this;
        }

        @Override
        public Build password(String password) {
            this.password = password;
            return this;
        }

        @Override
        public Build driverClassname(String driver) {
            this.driverClassname = driver;
            return this;
        }

        @Override
        public Build maxAge(long maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        @Override
        public DataSource build() {

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaxLifetime(maxAge);
            config.setDriverClassName(driverClassname);

            //JTDS does not support connection.isValid()
            if (driverClassname.toLowerCase().contains("jtds")) {
                logger.info("JTDS driver detected, setting connection-test-query");
                config.setConnectionTestQuery("SELECT 1");
            }else{
                logger.info("Using jdbc built-in isValid() test");
            }
            config.setMaximumPoolSize(3);
            DataSource dataSource = new HikariDataSource(config);

            for (DataSourceCustomizer customizer : dataSourceCustomizers) {
                dataSource = customizer.wrapDataSource(dataSource);
            }
            return dataSource;
        }
    }
}
