/*
 * Copyright 2019 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro.collector.jaxrs;

import org.glassfish.jersey.message.MessageUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.*;
import java.nio.charset.Charset;

/**
 *
 */
public class CollectingFilter implements WriterInterceptor {

    protected static final String ENTITY_LOGGER_PROPERTY = CollectingFilter.class.getName() + ".entityLogger";


    public CollectingFilter(int maxEntitySize) {
        this.maxEntitySize = maxEntitySize;
    }

    private final int maxEntitySize;

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

    protected InputStream logInboundEntity(final StringBuilder b, InputStream stream, final Charset charset) throws IOException {
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

    public abstract class  LoggingStream extends FilterOutputStream {


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
