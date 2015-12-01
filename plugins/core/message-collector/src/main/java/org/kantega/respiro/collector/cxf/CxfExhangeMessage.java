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

package org.kantega.respiro.collector.cxf;

import org.kantega.respiro.collector.ExchangeMessage;
import org.apache.cxf.interceptor.LoggingMessage;

/**
 *
 */
public class CxfExhangeMessage implements ExchangeMessage {
    private final Type type;
    private final LoggingMessage loggingMessage;




    public CxfExhangeMessage(Type type, LoggingMessage loggingMessage) {
        this.type = type;
        this.loggingMessage = loggingMessage;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getPayload() {
        return loggingMessage.getPayload().toString();
    }

    @Override
    public String getAddress() {
        return loggingMessage.getAddress().toString();
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public int getResponseCode() {
        if(loggingMessage.getResponseCode().length() > 0) {
            return Integer.parseInt(loggingMessage.getResponseCode().toString());
        }
        return 0;
    }

    @Override
    public String getProtocol() {
        return "SOAP";
    }

    @Override
    public String getHeaders() {
        return loggingMessage.getHeader().toString();
    }

    @Override
    public String toString() {
        return loggingMessage.toString();
    }
}
