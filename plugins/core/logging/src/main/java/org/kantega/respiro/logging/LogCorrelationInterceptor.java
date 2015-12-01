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

import org.apache.cxf.binding.soap.interceptor.Soap11FaultOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.apache.cxf.phase.Phase.PREPARE_SEND;

public class LogCorrelationInterceptor extends AbstractPhaseInterceptor<Message> {

    public LogCorrelationInterceptor() {
        super(PREPARE_SEND);
        addBefore(Soap11FaultOutInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) throws Fault {

        Fault fault = (Fault) message.getContent(Exception.class);
        String logCorrId = randomUUID().toString();
        fault.setMessage(transformMessage(fault, logCorrId));
        message.put("respiro.logCorrelationId", logCorrId);


    }

    private String transformMessage(Fault fault, String logCorrId) {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("[logCorrelationId:").append(logCorrId).append("] ");

        String origMessage = fault.getMessage();
        if (origMessage != null) {
            msgBuilder.append(origMessage);
        } else {
            msgBuilder.append(fault.getClass().getName());
        }
        return msgBuilder.toString();
    }


}
