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
