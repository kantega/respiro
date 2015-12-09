package org.kantega.respiro.camelcollect;

import org.apache.camel.Message;
import org.kantega.respiro.collector.ExchangeMessage;

/**
 *
 */
public class CamelExchangeMessage implements ExchangeMessage {
    private final Message message;

    public CamelExchangeMessage(Message message) {
        this.message = message;
    }

    @Override
    public String getAddress() {
        return null;
    }

    @Override
    public String getPayload() {
        return message.toString();
    }

    @Override
    public String getMethod() {
        return message.getExchange().getFromEndpoint().getEndpointKey();
    }

    @Override
    public String getHeaders() {
        return message.getHeaders().toString();
    }

    @Override
    public int getResponseCode() {
        return 0;
    }

    @Override
    public Type getType() {
        return Type.REQUEST;
    }

    @Override
    public String getProtocol() {
        return "Camel";
    }
}
