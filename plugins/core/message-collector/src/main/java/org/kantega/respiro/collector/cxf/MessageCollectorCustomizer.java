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

package org.kantega.respiro.collector.cxf;

import org.kantega.respiro.collector.Collector;
import org.kantega.respiro.collector.ExchangeInfo;
import org.kantega.respiro.collector.ExchangeMessage;
import org.kantega.respiro.cxf.api.EndpointCustomizer;
import org.kantega.respiro.cxf.api.ServiceCustomizer;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingMessage;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import java.util.logging.Logger;

/**
 *
 */
public class MessageCollectorCustomizer implements EndpointCustomizer,ServiceCustomizer {

    ThreadLocal<Exchange> exchangeLocal = new ThreadLocal<>();


    @Override
    public void customizeEndpoint(Endpoint endpoint) {
        EndpointImpl e = (EndpointImpl) endpoint;


        e.getInInterceptors().add(new LoggingInInterceptor() {

            @Override
            public void handleMessage(Message message) throws Fault {
                exchangeLocal.set(message.getExchange());
                super.handleMessage(message);
            }

            @Override
            protected String formatLoggingMessage(LoggingMessage loggingMessage) {
                Exchange exchange = exchangeLocal.get();
                exchange.put(ExchangeInfo.EXCHANGE_INFO, Collector.newCollectionContext(new CxfExhangeMessage(ExchangeMessage.Type.REQUEST, loggingMessage)));
                return null;
            }

            @Override
            protected void log(Logger logger, String message) {
                // Empty
            }
        });

        LoggingOutInterceptor outInterceptor = new LoggingOutInterceptor() {



            @Override
            protected String formatLoggingMessage(LoggingMessage loggingMessage) {

                Exchange exchange = exchangeLocal.get();
                ExchangeInfo exchangeInfo = (ExchangeInfo) exchange.get(ExchangeInfo.EXCHANGE_INFO);
                exchangeInfo.setOutMessage(new CxfExhangeMessage(ExchangeMessage.Type.RESPONSE, loggingMessage));
                Collector.clearCollectionContext();
                return null;
            }

            @Override
            protected void log(Logger logger, String message) {
                // Empty
            }
        };
        e.getOutInterceptors().add(outInterceptor);
        e.getOutFaultInterceptors().add(outInterceptor);
    }

    @Override
    public void customizeService(BindingProvider bindingProvider) {

        Client client = ClientProxy.getClient(bindingProvider);
        client.getOutInterceptors().add(new LoggingOutInterceptor() {


            @Override
            protected String formatLoggingMessage(LoggingMessage loggingMessage) {

                Collector.getCurrent().ifPresent(
                        exchangeInfo -> exchangeInfo.addBackendMessage(new CxfExhangeMessage(ExchangeMessage.Type.REQUEST, loggingMessage))
                );

                return null;
            }

            @Override
            protected void log(Logger logger, String message) {

            }
        });
        LoggingInInterceptor inInterceptor = new LoggingInInterceptor() {


            @Override
            protected String formatLoggingMessage(LoggingMessage loggingMessage) {
                Collector.getCurrent().ifPresent(
                        exchangeInfo -> exchangeInfo.addBackendMessage(new CxfExhangeMessage(ExchangeMessage.Type.RESPONSE, loggingMessage))
                );
                return null;
            }

            @Override
            protected void log(Logger logger, String message) {

            }
        };
        client.getInInterceptors().add(inInterceptor);
        client.getInFaultInterceptors().add(inInterceptor);
    }
}
