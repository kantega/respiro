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

import javax.jms.*;

/**
 *
 */
public class TopicNotifier {

    private final TopicPublisher publisher;
    private final TopicSession session;
    private final TopicConnection con;

    public TopicNotifier(TopicConnectionFactory connectionFactory) {

        try {
            con = connectionFactory.createTopicConnection();

            session = con.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic topic = session.createTopic("userprofiles.lookup");

            publisher = session.createPublisher(topic);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }


    public void notifyLookup(UserProfile prof, String username) {

        try {
            TextMessage textMessage = session.createTextMessage(String.format("Profile '%s' looked up by %s", prof.getFullName(), username));
            publisher.publish(textMessage);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

    }

    public void close() {
        try {
            session.close();
        } catch (JMSException e) {
            // ignore
        }
        try {
            con.close();
        } catch (JMSException e) {
            // ignore
        }
    }
}
