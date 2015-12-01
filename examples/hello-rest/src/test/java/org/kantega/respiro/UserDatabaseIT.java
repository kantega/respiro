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

package org.kantega.respiro;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.kantega.respiro.test.Utils.getH2Port;

public class UserDatabaseIT {

    private JdbcDataSource dataSource;

    @Test
    public void findUserTest() {

        assertThat(new UsersDAO(dataSource).findName("OLANOR"),
                is("Ola Nordmann"));

    }

    @Before
    public void before() {
        dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:tcp://localhost:" + getH2Port() + "/mem:hellodata;MODE=Oracle");
        dataSource.setUser("admin");
        dataSource.setPassword("password");
    }
}
