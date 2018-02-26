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

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.kantega.respiro.api.mail.Attachment;
import org.kantega.respiro.api.mail.MailSender;
import org.kantega.respiro.api.mail.Message;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.util.ByteArrayDataSource;
import java.util.Collection;
import java.util.List;

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

        MultiPartEmail mail = config.newMail(msg.isHtml());

        try {
            mail.setCharset(msg.getCharset().name());
            addAddresses(mail.getToAddresses(), msg.getTo());
            addAddresses(mail.getCcAddresses(), msg.getCc());
            addAddresses(mail.getBccAddresses(), msg.getBcc());
            addAttachments(mail, msg);
            if (msg.getFrom() != null)
                mail.setFrom(msg.getFrom());
            
            mail.setSubject(msg.getSubject());
            addMailBody(mail, msg);

            if (mail.getToAddresses().size() + mail.getCcAddresses().size() + mail.getBccAddresses().size() > 0)
                return mail.send();
            else
                return "Mail not sent due to empty recipients list.";
        } catch (EmailException | AddressException e) {
            throw new RuntimeException(e);
        }
    }

    private void addAttachments(MultiPartEmail mail, Message msg) {
        for (Attachment attachment : msg.getAttachments()) {
            try {
                mail.attach(new ByteArrayDataSource(attachment.getContent(), attachment.getMimeType()), attachment.getFileName(), "");
            } catch (EmailException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void addAddresses(Collection<InternetAddress> toList, final List<String> addresses) throws AddressException {
        for (String address : addresses) {
            for (String mail : address.split(";"))
                if (config.isInWhitelist(mail))
                    toList.add(new InternetAddress(mail));
        }
    }

    private void addMailBody(MultiPartEmail mail, Message msg) throws EmailException {
        if (msg.isHtml()) {
            String body = msg.getBody();
            String plainTextBody = msg.getPlainTextBody();
            ((HtmlEmail) mail).setHtmlMsg(body)
                    .setTextMsg(plainTextBody.isEmpty() ? body : plainTextBody);
        } else {
            mail.setMsg(msg.getBody());
        }
    }

}
