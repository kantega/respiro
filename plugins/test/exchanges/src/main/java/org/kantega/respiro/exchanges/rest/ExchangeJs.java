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

package org.kantega.respiro.exchanges.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class ExchangeJs {
    private MessageJs inMessage;
    private String uuid;
    private MessageJs outMessage;
    private Date date;
    private List<MessageJs> backendMessages = new ArrayList<>();

    public void setInMessage(MessageJs inMessage) {
        this.inMessage = inMessage;
    }

    public MessageJs getInMessage() {
        return inMessage;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setOutMessage(MessageJs outMessage) {
        this.outMessage = outMessage;
    }

    public MessageJs getOutMessage() {
        return outMessage;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void addBackendMessage(MessageJs messageJs) {
        backendMessages.add(messageJs);
    }

    public List<MessageJs> getBackendMessages() {
        return backendMessages;
    }
}
