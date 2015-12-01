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

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import static java.lang.String.valueOf;

class ServerConfig {
    private final String host;
    private final int port;
    private String fromMail;
    private boolean ssl;
    private String username;
    private String password;
    private final List<InternetAddress> to = new ArrayList<>();
    private final List<InternetAddress> cc = new ArrayList<>();
    private final List<InternetAddress> bcc = new ArrayList<>();

    public ServerConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public MultiPartEmail newMail() {
        MultiPartEmail mail = new MultiPartEmail();

        mail.setHostName(host);
        if (!ssl)
            mail.setSmtpPort(port);
        else
            mail.setSslSmtpPort(valueOf(port));
        if (username != null && password != null)
            mail.setAuthentication(username, password);

        try {
            if (!to.isEmpty())
                mail.setTo(to);
            if (!cc.isEmpty())
                mail.setCc(cc);
            if (!bcc.isEmpty())
                mail.setBcc(bcc);
            if (fromMail != null)
                mail.setFrom(fromMail);
        } catch (EmailException e) {
            throw new RuntimeException(e);
        }

        return mail;
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

    public void addTo(String email) {
        try {
            for (String mail : email.split(";"))
                this.to.add(new InternetAddress(mail));
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }
    }

    public void addCc(String email) {
        try {
            for (String mail : email.split(";"))
                this.cc.add(new InternetAddress(mail));
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }
    }

    public void addBcc(String email) {
        try {
            for (String mail : email.split(";"))
                this.bcc.add(new InternetAddress(mail));
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }

    }


}
