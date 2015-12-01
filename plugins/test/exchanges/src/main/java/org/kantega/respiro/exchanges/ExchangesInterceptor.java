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

package org.kantega.respiro.exchanges;

import org.kantega.respiro.collector.ExchangeInfo;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import static org.apache.cxf.interceptor.MessageSenderInterceptor.MessageSenderEndingInterceptor;
import static org.apache.cxf.phase.Phase.PREPARE_SEND_ENDING;
import static org.kantega.respiro.collector.ExchangeInfo.EXCHANGE_INFO;

public class ExchangesInterceptor extends AbstractPhaseInterceptor<Message> {

    private final Exchanges exchanges;

    public ExchangesInterceptor(Exchanges exchanges) {
        super(PREPARE_SEND_ENDING);
        this.exchanges = exchanges;
        addAfter(MessageSenderEndingInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) throws Fault {

        ExchangeInfo exchangeInfo = (ExchangeInfo) message.getExchange().get(EXCHANGE_INFO);
        exchanges.addExchange(exchangeInfo);
    }

}
