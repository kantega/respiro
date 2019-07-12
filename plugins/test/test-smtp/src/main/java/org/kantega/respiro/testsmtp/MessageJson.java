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

package org.kantega.respiro.testsmtp;

import com.dumbster.smtp.MailMessage;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MessageJson {


    private String from;
    private String to;
    private String subject;
    private String body;
    private List<HeaderJson> headers;

    public MessageJson(MailMessage mailMessage) {
        List<HeaderJson> json = new ArrayList<HeaderJson>();
        mailMessage.getHeaderNames().forEachRemaining( name -> json.add(new HeaderJson(name, mailMessage.getHeaderValues(name))));
        this.headers = json;
        this.body = mailMessage.getBody();
        subject = getFirstHeaderValue("Subject");
        from  = getFirstHeaderValue("From");
        to = getFirstHeaderValue("To");
    }

    public MessageJson() {
    }


    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public String getTo() {
        return to;
    }

    public List<HeaderJson> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    private String getFirstHeaderValue(String name) {
        return headers.stream().filter( h -> h.getName().equals(name)).findFirst().get().getValues()[0];
    }
}
