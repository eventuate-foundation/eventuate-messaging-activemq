package io.eventuate.messaging.activemq.spring.consumer;

import io.eventuate.messaging.activemq.spring.common.ChannelType;
import io.eventuate.messaging.partitionmanagement.CommonMessageConsumer;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageConsumerActiveMQImpl implements CommonMessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final String id = UUID.randomUUID().toString();

  private ActiveMQConnectionFactory connectionFactory;

  private Connection connection;
  private Session session;
  private List<javax.jms.MessageConsumer> consumers = new ArrayList<>();
  private List<Future<Void>> processingFutures = new ArrayList<>();
  private Map<String, ChannelType> messageModes;

  private AtomicBoolean runFlag = new AtomicBoolean(true);

  public MessageConsumerActiveMQImpl(String url,
                                     Optional<String> user,
                                     Optional<String> password) {
    this(url, Collections.emptyMap(), user, password);
  }

  public MessageConsumerActiveMQImpl(String url,
                                     Map<String, ChannelType> messageModes,
                                     Optional<String> user,
                                     Optional<String> password) {
    this.messageModes = messageModes;
    connectionFactory = createActiveMQConnectionFactory(url, user, password);
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
      logger.error("Consumer initialization failed", e);
      throw new RuntimeException(e);
    }
  }

  public Subscription subscribe(String subscriberId, Set<String> channels, ActiveMQMessageHandler handler) {
    try {
      logger.info("Subscribing: subscriberId: {}, channels: {}", subscriberId, channels);
      List<javax.jms.MessageConsumer> subscriptionConsumers = new ArrayList<>();
      for (String channel : channels) {
        ChannelType mode = messageModes.getOrDefault(channel, ChannelType.TOPIC);

        String destinationName = mode == ChannelType.TOPIC ?
                String.format("Consumer.%s.VirtualTopic.%s", formatSubscriberId(subscriberId), channel) :
                channel;

        logger.info("Creating queue: {}", destinationName);
        Destination destination = session.createQueue(destinationName);

        logger.info("Creating consumer: {}", destination);
        javax.jms.MessageConsumer consumer = session.createConsumer(destination);
        consumers.add(consumer);
        subscriptionConsumers.add(consumer);

        processingFutures.add(CompletableFuture.supplyAsync(() -> process(subscriberId, consumer, handler)));
        logger.info("Subscribed: subscriberId: {}, channels: {}", subscriberId, channels);
      }

      return new Subscription(() -> {
        logger.info("closing consumers");
        subscriptionConsumers.forEach(consumer -> {
          try {
            consumer.close();
          } catch (JMSException e) {
            logger.error("closing consumer failed", e);
            throw new RuntimeException(e);
          }
        });
        logger.info("closed consumers");
      });

    } catch (JMSException e) {
      logger.error("Subscription failed", e);
      throw new RuntimeException(e);
    }
  }

  private String formatSubscriberId(String subscriberId) {
    return subscriberId.replace(".", "::");
  }

  private ActiveMQConnectionFactory createActiveMQConnectionFactory(String url, Optional<String> user, Optional<String> password) {
    return user
            .flatMap(usr -> password.flatMap(pass ->
                    Optional.of(new ActiveMQConnectionFactory(usr, pass, url))))
            .orElseGet(() -> new ActiveMQConnectionFactory(url));
  }

  private Void process(String subscriberId,
                       javax.jms.MessageConsumer consumer,
                       ActiveMQMessageHandler handler) {
    logger.info("starting processing");
    while (runFlag.get()) {
      try {
        javax.jms.Message message = consumer.receive(100);

        if (message == null) {
          continue;
        }

        TextMessage textMessage = (TextMessage) message;
        ActiveMQMessage activeMQMessage = new ActiveMQMessage(textMessage.getText());
        try {
          logger.trace("Invoking handler {} {}", subscriberId, activeMQMessage);
          handler.accept(activeMQMessage);
          logger.trace("handled message {} {}", subscriberId, activeMQMessage);
        } catch (Throwable t) {
          logger.trace("Got exception {} {}", subscriberId, activeMQMessage);
          logger.trace("Got exception ", t);
          throw new RuntimeException(t);
        }

        acknowledge(textMessage);

      } catch (JMSException e) {
        logger.error("processing message failed", e);
      }
    }
    logger.info("processing finished");

    try {
      logger.info("closing consumer");
      consumer.close();
      logger.info("closed consumer");
    } catch (JMSException e) {
      logger.error("closing consumer failed", e);
    }

    return null;
  }

  private void acknowledge(TextMessage textMessage) {
    try {
      textMessage.acknowledge();
    } catch (JMSException e) {
      logger.error("message acknowledgement failed", e);
    }
  }

  public void close() {
    runFlag.set(false);

    processingFutures.forEach(f -> {
      try {
        f.get();
      } catch (InterruptedException | ExecutionException e) {
        logger.error("Getting data from future failed", e);
      }
    });

    try {
      logger.info("closing session and connection");
      session.close();
      connection.close();
    } catch (JMSException e) {
      logger.error("closing session/connection failed", e);
    }
    logger.info("closed session and connection");
  }

  public String getId() {
    return id;
  }
}