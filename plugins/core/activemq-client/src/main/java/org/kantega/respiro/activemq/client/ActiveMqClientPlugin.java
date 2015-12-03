package org.kantega.respiro.activemq.client;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.kantega.respiro.api.DataSourceInitializer;
import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import java.util.Collection;

/**
 */
@Plugin
public class ActiveMqClientPlugin {

    @Export private final ConnectionFactory connectionFactory;
    @Export private final QueueConnectionFactory queueConnectionFactory;
    @Export private final TopicConnectionFactory topicConnectionFactory;

    public ActiveMqClientPlugin(@Config String activeMqBrokerURL, Collection<DataSourceInitializer> initializers) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activeMqBrokerURL);
        this.connectionFactory = connectionFactory;
        this.topicConnectionFactory = connectionFactory;
        this.queueConnectionFactory = connectionFactory;
    }


}
