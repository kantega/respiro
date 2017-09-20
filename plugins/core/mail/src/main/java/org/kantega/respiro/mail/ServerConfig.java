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
import org.apache.commons.mail.MultiPartEmail;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.valueOf;

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

    public void whitelist(String whitelist) {
        if (!"OFF".equals(whitelist.trim().toUpperCase())) {

            this.whitelist = Arrays.asList(whitelist.split(","));
        }
    }

    public boolean isInWhitelist(String emailAddress) {
        return this.whitelist == null || this.whitelist.contains(emailAddress);
    }


}
