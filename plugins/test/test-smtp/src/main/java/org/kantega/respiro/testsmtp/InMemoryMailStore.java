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

package org.kantega.respiro.testsmtp;

import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.MailStore;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 */
public class InMemoryMailStore implements MailStore {

    private List<MailMessage> messages = new CopyOnWriteArrayList<>();

    @Override
    public int getEmailCount() {
        return messages.size();
    }

    @Override
    public void addMessage(MailMessage message) {
        messages.add(message);
    }

    @Override
    public MailMessage[] getMessages() {
        return messages.toArray(new MailMessage[messages.size()]);
    }

    @Override
    public MailMessage getMessage(int index) {
        return messages.get(index);
    }

    @Override
    public void clearMessages() {
        messages.clear();
    }
}
