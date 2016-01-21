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

package org.kantega.respiro.exchanges.rest;

import org.kantega.respiro.collector.DoNotCollect;
import org.kantega.respiro.collector.ExchangeInfo;
import org.kantega.respiro.collector.ExchangeMessage;
import org.kantega.respiro.exchanges.Exchanges;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 */
@Path("respiro/exchanges/api")
@DoNotCollect
public class ExchangesResource {

    // Disable metrics for this resource
    public static boolean METRICS = false;

    private final Exchanges exchanges;

    public ExchangesResource(Exchanges exchanges) {
        this.exchanges = exchanges;
    }

    @Path("exchanges")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ExchangeJs> getExchanges() {
        return exchanges.getExchangeLog()
                .stream()
                .map(ExchangesResource::toJs).collect(Collectors.toList());
    }

    @Path("exchanges")
    @DELETE

    public void clear() {
        exchanges.clear();
    }

    @Path("exchanges/{uuid}")
    @GET
    public ExchangeJs getDetails(@PathParam("uuid") UUID uuid) {
        return exchanges.getExchangeLog().stream().filter( e -> e.getUuid().equals(uuid)).findAny().map(ExchangesResource::toJs).get();
    }

    private static ExchangeJs toJs(ExchangeInfo exchangeInfo) {
        ExchangeJs js = new ExchangeJs();


        js.setUuid(exchangeInfo.getUuid().toString());
        js.setDate(exchangeInfo.getDate());
        js.setInMessage(messageToJs(exchangeInfo.getInMessage()));
        js.setOutMessage(messageToJs(exchangeInfo.getOutMessage()));

        for (ExchangeMessage message : exchangeInfo.getBackendMessages()) {
            js.addBackendMessage(messageToJs(message));
        }


        return js;
    }

    private static MessageJs messageToJs(ExchangeMessage message) {
        MessageJs js = new MessageJs();
        js.setPayload(message.getPayload());
        js.setAddress(message.getAddress());
        js.setResponseCode(message.getResponseCode());
        js.setResponseStatus(message.getResponseStatus().toString());
        js.setType(message.getType().toString());
        js.setProtocol(message.getProtocol());
        js.setMethod(message.getMethod());
        js.setHeaders(message.getHeaders());

        return js;
    }
}
