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

package org.kantega.respiro.collector.jaxrs;

import org.kantega.respiro.collector.ExchangeMessage;

import javax.ws.rs.core.MultivaluedMap;

/**
 *
 */
public class JaxRsExchangeMessage implements ExchangeMessage {


    private final Type type;
    private String method;
    private String address;
    private MultivaluedMap<String, String> headers;
    private String payload;
    private int responseCode;

    public JaxRsExchangeMessage(Type type) {
        this.type = type;
    }



    @Override
    public String getProtocol() {
        return "REST";
    }

    @Override
    public String  getResponseCode() {
        return Integer.toString(responseCode);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return payload;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getMethod() {
        return method;
    }


    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String getAddress() {
        return address;
    }

    public void setHeaders(MultivaluedMap<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String getHeaders() {
        return headers.toString();
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
