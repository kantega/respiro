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

package org.kantega.respiro.collector.jdbc;

import org.kantega.respiro.collector.ExchangeMessage;

/**
 *
 */
public class JdbcExchangeMessage implements ExchangeMessage {
    private final String sql;
    private final String url;

    public JdbcExchangeMessage(String url, String sql) {
        this.sql = sql;
        this.url = url;
    }

    @Override
    public String getPayload() {
        return sql;
    }

    @Override
    public String getAddress() {
        return url;
    }

    @Override
    public String getMethod() {
        return null;
    }

    @Override
    public String getHeaders() {
        return null;
    }

    @Override
    public ResponseStatus getResponseStatus() {
        return ResponseStatus.UNDETERMINED;
    }

    @Override
    public String getResponseCode() {
        return "";
    }

    @Override
    public Type getType() {
        return Type.REQUEST;
    }

    @Override
    public String getProtocol() {
        return "JDBC";
    }
}
