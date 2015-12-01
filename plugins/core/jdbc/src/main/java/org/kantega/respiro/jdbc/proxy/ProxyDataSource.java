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

package org.kantega.respiro.jdbc.proxy;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 *
 */
public class ProxyDataSource implements DataSource {

    private final DataSource wrapped;

    public ProxyDataSource(DataSource wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return wrapped.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return wrapped.getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return wrapped.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        wrapped.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        wrapped.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return wrapped.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return wrapped.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return wrapped.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return wrapped.isWrapperFor(iface);
    }
}
