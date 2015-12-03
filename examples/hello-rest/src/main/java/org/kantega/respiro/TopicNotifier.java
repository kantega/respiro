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
