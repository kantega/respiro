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

import org.kantega.respiro.api.DataSourceBuilder;
import org.kantega.respiro.api.DataSourceInitializer;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import java.util.Collection;

/**
 * Created by helaar on 19.10.2015.
 */
@Plugin
public class JdbcPlugin {

    @Export
    private final DataSourceBuilder dsBuilder;

    public JdbcPlugin(Collection<DataSourceInitializer> initializers, Collection<DataSourceCustomizer> dataSourceCustomizers) {
        this.dsBuilder = new DefaultDataSourceBuilder(dataSourceCustomizers);

        for (DataSourceInitializer initializer : initializers) {
            initializer.initialize();
        }
    }
}
