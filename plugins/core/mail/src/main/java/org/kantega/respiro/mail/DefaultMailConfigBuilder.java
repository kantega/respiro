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

import org.kantega.respiro.api.mail.MailConfigBuilder;
import org.kantega.respiro.api.mail.MailSender;

/**
 * Created by helaar on 20.11.2015.
 */
public class DefaultMailConfigBuilder  implements MailConfigBuilder {

    @Override
    public Build server(String hostname, int port) {
        return new Builder(hostname, port);
    }

    class Builder implements Build {
        final ServerConfig config;

        public Builder(String host, int port) {
            config = new ServerConfig(host, port);
        }

        @Override
        public Build useSsl(boolean ssl) {
            config.useSsl(ssl);
            return this;
        }

        @Override
        public Build auth(String username, String password) {
            config.setAuth(username, password);
            return this;
        }

        @Override
        public Build from(String email) {
            config.setFrom(email);
            return this;
        }

        @Override
        public Build to(String email) {
            config.addTo(email);
            return this;
        }

        @Override
        public Build cc(String email) {
            config.addCc(email);
            return this;
        }

        @Override
        public Build bcc(String email) {
            config.addBcc(email);
            return this;
        }

        @Override
        public MailSender build() {
            return new SMTPMailSender(config);
        }
    }
}
