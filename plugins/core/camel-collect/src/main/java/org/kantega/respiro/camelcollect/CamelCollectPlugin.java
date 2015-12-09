package org.kantega.respiro.camelcollect;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.management.event.AbstractExchangeEvent;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeCreatedEvent;
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
    public CamelCollectPlugin() {

    }

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

                    } else if (event instanceof ExchangeCompletedEvent) {
                        ExchangeInfo exchangeInfo = Collector.newCollectionContext(new CamelExchangeMessage(exchange.getIn()));
                        exchangeInfo.setOutMessage(new CamelExchangeMessage(exchange.getIn()));
                        Collector.endCollectionContext();
                        Collector.clearCollectionContext();
                    }
                }
            }
        });
    }
}
