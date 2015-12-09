package org.kantega.respiro.camelcollect;

import org.apache.camel.Message;
import org.kantega.respiro.collector.ExchangeMessage;

import java.net.URI;

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
        StringBuilder sb = new StringBuilder();

        URI uri = message.getExchange().getFromEndpoint().getEndpointConfiguration().getURI();

        sb.append(uri.getHost());
        if(uri.getPort() != -1 ) {
            sb.append(":").append(uri.getPort());
        }
        if(uri.getPath() != null) {
            sb.append(uri.getPath());
        }

        return sb.toString();
    }

    @Override
    public String getPayload() {
        return message.toString();
    }

    @Override
    public String getMethod() {
        return "Camel" ;
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
        return message.getExchange().getFromEndpoint().getEndpointConfiguration().getURI().getScheme();
    }
}
