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

import org.glassfish.jersey.message.MessageUtils;
import org.kantega.respiro.collector.Collector;
import org.kantega.respiro.collector.ExchangeInfo;
import org.kantega.respiro.collector.ExchangeMessage;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 */
@Priority(Integer.MIN_VALUE+1)
public class ContainerCollectingFilter extends CollectingFilter implements ContainerResponseFilter, ContainerRequestFilter {


    private static final int DEFAULT_MAX_ENTITY_SIZE = 8 * 1024;


    public ContainerCollectingFilter() {
        super(DEFAULT_MAX_ENTITY_SIZE);
    }





    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        Collector.getCurrent().ifPresent(exchangeInfo -> {

            JaxRsExchangeMessage msg = new JaxRsExchangeMessage(ExchangeMessage.Type.RESPONSE);


            msg.setResponseCode(responseContext.getStatus());
            msg.setHeaders(responseContext.getStringHeaders());

            if (responseContext.hasEntity()) {
                final OutputStream stream = new LoggingStream(msg, responseContext.getEntityStream()) {
                    @Override
                    public void collect() {
                        exchangeInfo.setOutMessage(msg);
                        Collector.endCollectionContext();
                        Collector.clearCollectionContext();
                    }
                };
                responseContext.setEntityStream(stream);
                requestContext.setProperty(ENTITY_LOGGER_PROPERTY, stream);
                // not calling log(b) here - it will be called by the interceptor
            } else {
                exchangeInfo.setOutMessage(msg);
                Collector.endCollectionContext();
                Collector.clearCollectionContext();
            }

        });
    }

    @Override
    public void filter(ContainerRequestContext context) throws IOException {

        JaxRsExchangeMessage msg = new JaxRsExchangeMessage(ExchangeMessage.Type.REQUEST);

        final StringBuilder b = new StringBuilder();

        msg.setMethod(context.getMethod());
        msg.setAddress(context.getUriInfo().getAbsolutePath().toString());
        msg.setHeaders( context.getHeaders());

        if (context.hasEntity()) {
            context.setEntityStream(
                    logInboundEntity(b, context.getEntityStream(), MessageUtils.getCharset(context.getMediaType())));
        }

        msg.setPayload(b.toString());
        ExchangeInfo exchangeInfo = Collector.newCollectionContext(msg);
        context.setProperty("collector.msg", exchangeInfo);
    }
}


