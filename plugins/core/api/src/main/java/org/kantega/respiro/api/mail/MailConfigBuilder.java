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

package org.kantega.respiro.api.mail;

public interface MailConfigBuilder {


    Build server(String hostname, int port);

    interface Build {

        Build useSsl(boolean ssl);

        Build auth(String username, String password);

        Build from(String email);

        Build to(String email);

        Build cc(String email);

        Build bcc(String email);
        
        Build whitelist(String whitelist);

        MailSender build();

    }
}
