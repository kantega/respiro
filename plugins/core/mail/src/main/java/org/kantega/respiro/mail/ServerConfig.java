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

package org.kantega.respiro.mail;

import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import java.util.Arrays;
import java.util.List;

class ServerConfig {
    private final String host;
    private final int port;
    private String fromMail;
    private boolean ssl;
    private String username;
    private String password;
    private List<String> whitelist;

    public ServerConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public void useSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public void setAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setFrom(String fromMail) {
        this.fromMail = fromMail;
    }

    public String getFrom() { return this.fromMail; }

    public void whitelist(String whitelist) {
        if (!"OFF".equals(whitelist.trim().toUpperCase())) {

            this.whitelist = Arrays.asList(whitelist.split(","));
        }
    }

    public boolean isInWhitelist(String emailAddress) {
        return this.whitelist == null || this.whitelist.contains(emailAddress);
    }


    public Mailer smtp() {
        final MailerBuilder.MailerRegularBuilder builder = MailerBuilder.withSMTPServer(host, port, username, password);
        if( ssl )
            builder.withTransportStrategy(TransportStrategy.SMTP_TLS);
        
        return builder.buildMailer();
    }
}
