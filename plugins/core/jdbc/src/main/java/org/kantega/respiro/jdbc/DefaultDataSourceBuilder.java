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

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.kantega.respiro.api.DataSourceBuilder;

import javax.sql.DataSource;
import java.util.Collection;

public class DefaultDataSourceBuilder implements DataSourceBuilder {
    private final Collection<DataSourceCustomizer> dataSourceCustomizers;

    public DefaultDataSourceBuilder(Collection<DataSourceCustomizer> dataSourceCustomizers) {

        this.dataSourceCustomizers = dataSourceCustomizers;
    }

    @Override
    public Build datasource(String url) {
        return new DefaultBuild(url);
    }

    private class DefaultBuild implements Build {


        private final String url;
        private String username;
        private String password;
        private String driverClassname;

        public DefaultBuild(String url) {
            this.url = url;

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
        public DataSource build() {
            PoolProperties poolProperties = new PoolProperties();
            poolProperties.setDriverClassName(driverClassname);
            poolProperties.setUrl(url);
            poolProperties.setUsername(username);
            poolProperties.setPassword(password);
            DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
            for (DataSourceCustomizer customizer : dataSourceCustomizers) {
                dataSource = customizer.wrapDataSource(dataSource);
            }
            return dataSource;
        }
    }
}
