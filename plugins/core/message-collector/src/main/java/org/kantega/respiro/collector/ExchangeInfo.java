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

package org.kantega.respiro.collector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 */
public class ExchangeInfo {
    public static final String EXCHANGE_INFO = "collector.exchange_info";
    private final ExchangeMessage inMessage;
    private final UUID uuid;
    private final Date date;
    private ExchangeMessage outMessage;
    private List<ExchangeMessage> backendMessages = new ArrayList<>();

    public ExchangeInfo(ExchangeMessage inMessage) {
        this.inMessage = inMessage;
        this.uuid = UUID.randomUUID();
        this.date = new Date();
    }


    public Date getDate() {
        return date;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ExchangeMessage getInMessage() {
        return inMessage;
    }

    public void setOutMessage(ExchangeMessage outMessage) {
        this.outMessage = outMessage;
    }

    public ExchangeMessage getOutMessage() {
        return outMessage;
    }

    public void addBackendMessage(ExchangeMessage loggingMessage) {
        backendMessages.add(loggingMessage);
    }

    public List<ExchangeMessage> getBackendMessages() {
        return backendMessages;
    }
}
