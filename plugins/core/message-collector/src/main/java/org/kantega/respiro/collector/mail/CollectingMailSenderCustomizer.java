package org.kantega.respiro.collector.mail;

import org.kantega.respiro.api.mail.MailSender;
import org.kantega.respiro.api.mail.Message;
import org.kantega.respiro.collector.Collector;
import org.kantega.respiro.collector.ExchangeMessage;
import org.kantega.respiro.mail.MailSenderCustomizer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.*;
import static java.util.Collections.*;

public class CollectingMailSenderCustomizer implements MailSenderCustomizer {

    @Override
    public MailSender wrapMailSender(MailSender mailSender) {
        return new CollectingMailSender(mailSender);
    }

    final static class CollectingMailSender implements MailSender {

        final MailSender wrapped;

        CollectingMailSender(MailSender wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public String send(Message msg) {
            Map<String, List<String>> headers = new LinkedHashMap<>();
            headers.put("To", msg.getTo());
            headers.put("Cc", msg.getCc());
            headers.put("Bcc", msg.getBcc());
            headers.put("ReplyTo", msg.getReplyTo());
            headers.put("# attachments", singletonList(valueOf(msg.getAttachments().size())));
            headers.put("Subject", singletonList(msg.getSubject()));

            try {
                String response = wrapped.send(msg);
                headers.put("MessageId", singletonList(response));
                Collector.getCurrent().ifPresent(e -> e.addBackendMessage(new MailExhangeMessage(msg.getBody(), "OK", headers.toString())));
                return response;
            } catch (RuntimeException ex) {
                Collector.getCurrent().ifPresent(e -> e.addBackendMessage(new MailExhangeMessage(msg.getBody(), ex.getMessage(), headers.toString())));
                throw ex;
            }
        }
    }

    final static class MailExhangeMessage implements ExchangeMessage {

        final String body;
        final String response;
        final String header;

        MailExhangeMessage(String body, String response, String header) {
            this.body = body;
            this.response = response;
            this.header = header;
        }

        @Override
        public String getPayload() {
            return body;
        }

        @Override
        public String getAddress() {
            return null;
        }

        @Override
        public String getMethod() {
            return null;
        }

        @Override
        public String getHeaders() {
            return header;
        }

        @Override
        public ResponseStatus getResponseStatus() {
            return ResponseStatus.UNDETERMINED;
        }

        @Override
        public String getResponseCode() {
            return response;
        }

        @Override
        public Type getType() {
            return Type.REQUEST;
        }

        @Override
        public String getProtocol() {
            return "SMTP";
        }
    }
}
