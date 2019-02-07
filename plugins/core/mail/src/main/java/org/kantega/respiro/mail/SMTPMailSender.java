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

package org.kantega.respiro.mail;

import org.kantega.respiro.api.mail.Attachment;
import org.kantega.respiro.api.mail.MailSender;
import org.kantega.respiro.api.mail.Message;
import org.simplejavamail.email.AttachmentResource;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.email.EmailPopulatingBuilder;
import org.simplejavamail.email.Recipient;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.util.ByteArrayDataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;

public class SMTPMailSender implements MailSender {
    private final ServerConfig config;

    public SMTPMailSender(ServerConfig config) {
        this.config = config;
    }

    @Override
    public String send(Message msg) {

        // See: http://stackoverflow.com/questions/21856211/javax-activation-unsupporteddatatypeexception-no-object-dch-for-mime-type-multi
        currentThread().setContextClassLoader(getClass().getClassLoader());
        

        EmailPopulatingBuilder builder = EmailBuilder.startingBlank()
            .withSubject(msg.getSubject())
            .from(inetAddress(firstNotEmpty(msg.getFrom(), config.getFrom())))
            .to(whitelisted(msg.getTo()))
            .cc(whitelisted(msg.getCc()))
            .bcc(whitelisted(msg.getBcc()))
            .withReplyTo(msg.getReplyTo())
            .withAttachments(mapAttachemtns(msg.getAttachments()));

        if (msg.isHtml())
            builder = builder.withHTMLText(msg.getHtmlBody()).withPlainText(msg.getPlainTextBody());
        else
            builder = builder.withPlainText(msg.getBody());


        if (!builder.getRecipients().isEmpty()) {
            // send
            this.config.smtp().sendMail(builder.buildEmail());

            return format("Message to %s sent", builder.getRecipients().toString());
        } else
            return "Mail not sent due to empty recipients list.";

    }
    
    private static InternetAddress inetAddress(String fromAddress) {
        
        try {
            return new InternetAddress(fromAddress);
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<Recipient> whitelisted(final List<String> addresses) {
        return addresses.stream()
            .map(a -> a.split(";"))
            .flatMap(Arrays::stream)
            .filter(config::isInWhitelist)
            .map(adr -> new Recipient(null, adr, null))
            .collect(Collectors.toList());
    }

    private List<AttachmentResource> mapAttachemtns(final List<Attachment> attachments) {
        return attachments.stream().map(a -> new AttachmentResource(
            a.getFileName(), new ByteArrayDataSource(a.getContent(), a.getMimeType()) 
        )).collect(Collectors.toList());
    }

    private String firstNotEmpty(String... possbleEmpty) {
        return Stream.of(possbleEmpty).filter(str -> str != null && str.trim().length() > 0).findFirst().orElse(null);
    }


}
