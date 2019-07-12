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

package org.kantega.respiro.camelcollect;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.management.event.*;
import org.apache.camel.support.EventNotifierSupport;
import org.kantega.respiro.camel.CamelContextCustomizer;
import org.kantega.respiro.collector.Collector;
import org.kantega.respiro.collector.ExchangeInfo;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import java.util.EventObject;


/**
 *
 */
@Plugin
public class CamelCollectPlugin implements CamelContextCustomizer {

    public static final String RESPIRO_EXCHANGE_INFO = "respiro.exchangeInfo";
    @Export final CamelContextCustomizer camelContextCustomizer = this;

    @Override
    public void customize(CamelContext camelContext) {
        camelContext.getManagementStrategy().addEventNotifier(new EventNotifierSupport() {
            @Override
            public boolean isEnabled(EventObject event) {
                return event instanceof AbstractExchangeEvent;
            }

            @Override
            public void notify(EventObject e) throws Exception {
                if(e instanceof AbstractExchangeEvent) {
                    AbstractExchangeEvent event = (AbstractExchangeEvent) e;
                    Exchange exchange = event.getExchange();

                    if (event instanceof ExchangeCreatedEvent) {
                        ExchangeInfo exchangeInfo = Collector.newCollectionContext(new CamelExchangeMessage(exchange.getIn()));
                        exchange.setProperty(RESPIRO_EXCHANGE_INFO, exchangeInfo);
                    } else if (event instanceof ExchangeFailureHandlingEvent) {
                        ExchangeInfo exchangeInfo = (ExchangeInfo) exchange.getProperty(RESPIRO_EXCHANGE_INFO);
                        CamelExchangeMessage message = (CamelExchangeMessage) exchangeInfo.getInMessage();
                        message.fail(exchange.getException());

                    } else if (event instanceof ExchangeCompletedEvent) {
                        ExchangeInfo exchangeInfo = (ExchangeInfo) exchange.getProperty(RESPIRO_EXCHANGE_INFO);
                        CamelExchangeMessage message = (CamelExchangeMessage) exchangeInfo.getInMessage();
                        exchangeInfo.setOutMessage(message);
                        Collector.endCollectionContext();
                        Collector.clearCollectionContext();
                    }
                }
            }
        });
    }
}
