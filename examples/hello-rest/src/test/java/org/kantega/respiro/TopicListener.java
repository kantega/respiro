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

package org.kantega.respiro;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.kantega.respiro.test.Utils;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TopicListener {

    private final TopicConnection con;
    private List<String> messages = new ArrayList<>();

    public TopicListener() throws JMSException {
        String activeMqPort = Utils.getPort("/activeMqPort.txt");
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:" + activeMqPort);


        con = connectionFactory.createTopicConnection();

        TopicSession session = con.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic topic = session.createTopic("userprofiles.lookup");

        MessageConsumer consumer = session.createConsumer(topic);

        consumer.setMessageListener(message -> {
            TextMessage textMessage = (TextMessage) message;
            try {
                messages.add(textMessage.getText());
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        });


        con.start();
    }

    public void close() {
        try {
            con.close();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getMessages() {
        return messages;
    }
}
