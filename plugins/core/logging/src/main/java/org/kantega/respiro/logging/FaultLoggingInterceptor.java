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

package org.kantega.respiro.logging;

import org.kantega.respiro.collector.ExchangeInfo;
import org.kantega.respiro.collector.ExchangeMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.cxf.interceptor.MessageSenderInterceptor.MessageSenderEndingInterceptor;
import static org.apache.cxf.phase.Phase.PREPARE_SEND_ENDING;
import static org.kantega.respiro.collector.ExchangeInfo.EXCHANGE_INFO;
import static org.slf4j.LoggerFactory.getLogger;

public class FaultLoggingInterceptor extends AbstractPhaseInterceptor<Message> {

    private final Logger LOGGER = getLogger(FaultLoggingInterceptor.class);


    public FaultLoggingInterceptor() {
        super(PREPARE_SEND_ENDING);
        addAfter(MessageSenderEndingInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) throws Fault {

        Fault fault = (Fault) message.getContent(Exception.class);
        ExchangeInfo exchangeInfo = (ExchangeInfo) message.getExchange().get(EXCHANGE_INFO);
        LOGGER.error(buildErrorContext(exchangeInfo, fault), fault);
    }

    private String buildErrorContext(ExchangeInfo info, Fault fault) {
        StringBuilder builder = new StringBuilder();
        builder.append(fault.getMessage()).append(":\n");
        builder.append("incoming->\n").append(info.getInMessage().toString()).append("\n");
        for (ExchangeMessage loggingMessage : info.getBackendMessages()) {
            builder.append("->backend:\n").append(loggingMessage.toString()).append("\n");
        }
        if (info.getOutMessage() != null)
            builder.append("returning->\n").append(info.getOutMessage().toString()).append("\n");

        return builder.toString();
    }
}
