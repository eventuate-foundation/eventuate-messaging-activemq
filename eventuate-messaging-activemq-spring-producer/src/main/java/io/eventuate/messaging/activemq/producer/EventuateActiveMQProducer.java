package io.eventuate.messaging.activemq.producer;

import io.eventuate.messaging.activemq.spring.common.ChannelType;
import io.eventuate.messaging.partitionmanagement.CommonMessageConsumer;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EventuateActiveMQProducer implements CommonMessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private Connection connection;
  private Session session;
  private Map<String, ChannelType> messageModes;

  public EventuateActiveMQProducer(String url, Optional<String> user, Optional<String> password) {
    this(url, Collections.emptyMap(), user, password);
  }

  public EventuateActiveMQProducer(String url,
                                   Map<String, ChannelType> messageModes,
                                   Optional<String> user,
                                   Optional<String> password) {
    this.messageModes = messageModes;

    ActiveMQConnectionFactory connectionFactory = createActiveMQConnectionFactory(url, user, password);
    try {
      logger.info("Creating connection");
      connection = connectionFactory.createConnection();
      connection.setExceptionListener(e -> logger.error(e.getMessage(), e));
      logger.info("Starting connection");
      connection.start();
      logger.info("Creating session");
      session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      logger.info("Created session");
    } catch (JMSException e) {
      logger.error("Producer initialization failed", e);
      throw new RuntimeException(e);
    }
  }

  public CompletableFuture<?> send(String topic, String key, String body) {
    MessageProducer producer = null;
    try {
      ChannelType mode = messageModes.getOrDefault(topic, ChannelType.TOPIC);

      Destination destination = mode == ChannelType.TOPIC ?
              session.createTopic("VirtualTopic." + topic) :
              session.createQueue(topic);

      producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.PERSISTENT);

      TextMessage message = session.createTextMessage(body);
      message.setStringProperty("JMSXGroupID", key);
      producer.send(message);
      producer.close();
    } catch (JMSException e) {
      logger.error("Sending failed", e);
    } finally {
      if (producer != null) {
        try {
          producer.close();
        } catch (JMSException e) {
          logger.error("closing producer failed", e);
        }
      }
    }

    return CompletableFuture.completedFuture(null);
  }


  private ActiveMQConnectionFactory createActiveMQConnectionFactory(String url, Optional<String> user, Optional<String> password) {
    return user
            .flatMap(usr -> password.flatMap(pass ->
                    Optional.of(new ActiveMQConnectionFactory(usr, pass, url))))
            .orElseGet(() -> new ActiveMQConnectionFactory(url));
  }

  @Override
  public void close() {
    try {
      logger.info("closing connection/session");
      connection.close();
      session.close();
      logger.info("closed connection/session");
    } catch (JMSException e) {
      logger.info("closing connection/session failed");
      logger.error(e.getMessage(), e);
    }
  }
}
