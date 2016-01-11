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

package org.kantega.respiro.camelcollect;

import org.apache.camel.Message;
import org.kantega.respiro.collector.ExchangeMessage;

import java.net.URI;

/**
 *
 */
public class CamelExchangeMessage implements ExchangeMessage {
    private final Message message;
    private volatile boolean failed;
    private volatile Exception exception;

    public CamelExchangeMessage(Message message) {
        this.message = message;
    }

    @Override
    public String getAddress() {
        StringBuilder sb = new StringBuilder();

        URI uri = message.getExchange().getFromEndpoint().getEndpointConfiguration().getURI();

        if(uri.getHost() != null) {
            sb.append(uri.getHost());
        }
        if(uri.getPort() != -1 ) {
            sb.append(":").append(uri.getPort());
        }
        if(uri.getPath() != null) {
            sb.append(uri.getPath());
        }

        return sb.toString();
    }

    @Override
    public String getPayload() {
        return message.toString();
    }

    @Override
    public String getMethod() {
        return "Camel" ;
    }

    @Override
    public String getHeaders() {
        return message.getHeaders().toString();
    }

    @Override
    public ResponseStatus getResponseStatus() {
        return failed ? ResponseStatus.ERROR : ResponseStatus.SUCCESS;
    }

    @Override
    public String getResponseCode() {
        return failed ? "FAILED" : "SUCCESS";
    }

    @Override
    public Type getType() {
        return Type.REQUEST;
    }

    @Override
    public String getProtocol() {
        return message.getExchange().getFromEndpoint().getEndpointConfiguration().getURI().getScheme();
    }

    public void fail(Exception exception) {
        this.failed = true;
        this.exception = exception;
    }
}
