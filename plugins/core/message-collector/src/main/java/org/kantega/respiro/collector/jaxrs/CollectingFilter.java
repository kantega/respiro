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

import org.kantega.respiro.collector.Collector;
import org.kantega.respiro.collector.ExchangeInfo;
import org.kantega.respiro.collector.ExchangeMessage;
import org.glassfish.jersey.message.MessageUtils;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.*;
import java.nio.charset.Charset;

/**
 *
 */
@Priority(Integer.MIN_VALUE+1)
public class CollectingFilter implements ClientRequestFilter, WriterInterceptor, ClientResponseFilter, ContainerResponseFilter, ContainerRequestFilter {

    private static final String ENTITY_LOGGER_PROPERTY = CollectingFilter.class.getName() + ".entityLogger";

    private static final int DEFAULT_MAX_ENTITY_SIZE = 8 * 1024;

    private final int maxEntitySize;

    public CollectingFilter() {
        this.maxEntitySize = DEFAULT_MAX_ENTITY_SIZE;
    }

    @Override
    public void filter(ClientRequestContext context) throws IOException {

        Collector.getCurrent().ifPresent((exchangeInfo -> {


            JaxRsExchangeMessage msg = new JaxRsExchangeMessage(ExchangeMessage.Type.REQUEST);

            msg.setMethod(context.getMethod());
            msg.setAddress( context.getUri().toString());
            msg.setHeaders( context.getStringHeaders());


            if (context.hasEntity()) {
                final OutputStream stream = new LoggingStream(msg, context.getEntityStream()) {
                    @Override
                    public void collect() {
                        exchangeInfo.addBackendMessage(msg);
                    }
                };
                context.setEntityStream(stream);
                context.setProperty(ENTITY_LOGGER_PROPERTY, stream);
                // not calling log(b) here - it will be called by the interceptor
            } else {
                exchangeInfo.addBackendMessage(msg);
            }


        }));

    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        Collector.getCurrent().ifPresent(exchangeInfo -> {

            try {
                final StringBuilder b = new StringBuilder();

                JaxRsExchangeMessage msg = new JaxRsExchangeMessage(ExchangeMessage.Type.RESPONSE);


                msg.setResponseCode(responseContext.getStatus());
                msg.setHeaders( responseContext.getHeaders());

                if (responseContext.hasEntity()) {
                    responseContext.setEntityStream(logInboundEntity(b, responseContext.getEntityStream(),
                            MessageUtils.getCharset(responseContext.getMediaType())));
                }

                msg.setPayload(b.toString());

                exchangeInfo.addBackendMessage(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });



    }

    private InputStream logInboundEntity(final StringBuilder b, InputStream stream, final Charset charset) throws IOException {
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }
        stream.mark(maxEntitySize + 1);
        final byte[] entity = new byte[maxEntitySize + 1];
        final int entitySize = stream.read(entity);
        b.append(new String(entity, 0, Math.min(entitySize, maxEntitySize), charset));
        if (entitySize > maxEntitySize) {
            b.append("...more...");
        }
        b.append('\n');
        stream.reset();
        return stream;
    }

    @Override
    public void aroundWriteTo(final WriterInterceptorContext writerInterceptorContext)
            throws IOException, WebApplicationException {
        final LoggingStream stream = (LoggingStream) writerInterceptorContext.getProperty(ENTITY_LOGGER_PROPERTY);
        writerInterceptorContext.proceed();
        if (stream != null) {
            JaxRsExchangeMessage msg = stream.getMsg();
            msg.setPayload(stream.getStringBuilder(MessageUtils.getCharset(writerInterceptorContext.getMediaType())).toString());
            stream.collect();
        }
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
                        Collector.clearCollectionContext();
                    }
                };
                responseContext.setEntityStream(stream);
                requestContext.setProperty(ENTITY_LOGGER_PROPERTY, stream);
                // not calling log(b) here - it will be called by the interceptor
            } else {
                exchangeInfo.setOutMessage(msg);
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

    private abstract class LoggingStream extends FilterOutputStream {

        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private final StringBuilder b;
        private final JaxRsExchangeMessage msg;


        LoggingStream(final JaxRsExchangeMessage msg, final OutputStream inner) {
            super(inner);
            this.msg = msg;
            b = new StringBuilder();

        }

        public JaxRsExchangeMessage getMsg() {
            return msg;
        }

        StringBuilder getStringBuilder(final Charset charset) {
            // write entity to the builder
            final byte[] entity = baos.toByteArray();

            b.append(new String(entity, 0, Math.min(entity.length, maxEntitySize), charset));
            if (entity.length > maxEntitySize) {
                b.append("...more...");
            }
            b.append('\n');

            return b;
        }

        @Override
        public void write(final int i) throws IOException {
            if (baos.size() <= maxEntitySize) {
                baos.write(i);
            }
            out.write(i);
        }

        public abstract void collect();
    }
}


