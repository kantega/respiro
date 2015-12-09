package org.kantega.respiro.camelcollect;

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultDebugger;
import org.apache.camel.management.event.AbstractExchangeEvent;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.processor.interceptor.*;
import org.apache.camel.spi.EventNotifier;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.support.EventNotifierSupport;
import org.apache.camel.util.StopWatch;
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

        if (false) {
            camelContext.addInterceptStrategy(new InterceptStrategy() {
                @Override
                public Processor wrapProcessorInInterceptors(CamelContext context, ProcessorDefinition<?> definition, Processor target, Processor nextTarget) throws Exception {
                    return new DelegateAsyncProcessor(target) {
                        @Override
                        public boolean process(Exchange exchange, AsyncCallback callback) {
                            System.out.println("COLLECT BEFORE: " + exchange.toString());
                            return processor.process(exchange, new AsyncCallback() {
                                public void done(boolean doneSync) {
                                    System.out.println("COLLECT DONE: " + exchange.toString());
                                    // must notify original callback
                                    callback.done(doneSync);
                                }
                            });
                        }
                    };

                }
            });
        }

        if (false) {
            Tracer tracer = new Tracer();
            tracer.setTraceInterceptorFactory(new TraceInterceptorFactory() {
                @Override
                public Processor createTraceInterceptor(ProcessorDefinition<?> node, Processor target, TraceFormatter formatter, Tracer tracer) {
                    return new TraceInterceptor(node, target, formatter, tracer) {
                        @Override
                        protected void logExchange(Exchange exchange) throws Exception {

                        }
                    };
                }
            });
            tracer.setTraceOutExchanges(true);
            tracer.getTraceHandlers().clear();
            tracer.addTraceHandler(new TraceEventHandler() {
                @Override
                public void traceExchange(ProcessorDefinition<?> node, Processor target, TraceInterceptor traceInterceptor, Exchange exchange) throws Exception {
                    System.out.println("TRACE EXCHANGE: " + exchange.toString());
                }

                @Override
                public Object traceExchangeIn(ProcessorDefinition<?> node, Processor target, TraceInterceptor traceInterceptor, Exchange exchange) throws Exception {
                    System.out.println("TRACE IN: " + exchange.toString());
                    ExchangeInfo exchangeInfo = (ExchangeInfo) exchange.getProperty(RESPIRO_EXCHANGE_INFO);
                    if (exchangeInfo == null) {
                        exchangeInfo = Collector.newCollectionContext(new CamelExchangeMessage(exchange.getIn()));
                        exchange.setProperty(RESPIRO_EXCHANGE_INFO, exchangeInfo);
                    }
                    return null;
                }

                @Override
                public void traceExchangeOut(ProcessorDefinition<?> node, Processor target, TraceInterceptor traceInterceptor, Exchange exchange, Object traceState) throws Exception {
                    System.out.println("TRACE OUT: " + exchange.toString());
                }
            });
            camelContext.addInterceptStrategy(tracer);
        }

        if (true) {
            camelContext.getManagementStrategy().addEventNotifier(new EventNotifierSupport() {
                @Override
                public boolean isEnabled(EventObject event) {
                    return event instanceof AbstractExchangeEvent;
                }

                @Override
                public void notify(EventObject event) throws Exception {
                    System.out.println("EVENT: + " + event);
                }
            });
        }
    }
}
