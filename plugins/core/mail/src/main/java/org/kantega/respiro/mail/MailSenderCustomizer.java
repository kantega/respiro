package org.kantega.respiro.mail;

import org.kantega.respiro.api.mail.MailSender;

public interface MailSenderCustomizer {

    MailSender wrapMailSender(MailSender mailSender);

}
