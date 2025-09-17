package io.eventuate.messaging.activemq.spring.integrationtests;

import io.eventuate.messaging.activemq.spring.common.ChannelType;
import io.eventuate.util.test.async.Eventually;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

@SpringBootTest(classes = TopicTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD) //to generate unique topic name for each test
public class TopicTest extends AbstractIntegrationTest {

  @Configuration
  @Import(CommonQueueTopicTestConfiguration.class)
  public static class Config {
    @Qualifier("testChannelType")
    @Bean
    public ChannelType testChannelType() {
      return ChannelType.TOPIC;
    }
  }

  @Test
  public void testSeveralSubscribersForDefinedTopic() {
    String topic = "destination" + uniquePostfix;
    testSeveralSubscribers(topic);
  }

  @Test
  public void testSeveralSubsribersDefaultMode() {
    String topic = "not_specfied_destination" + uniquePostfix;
    testSeveralSubscribers(topic);
  }

  @Test
  public void testJMSGroupIdOrdering() {
    int messages = 100;
    int consumers = 5;

    String destination = "destination" + uniquePostfix;
    String key = "key";

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    for (int i = 0; i < consumers; i ++) {
      messageConsumerActiveMQ.subscribe("subscriber", Collections.singleton(destination), message ->
              concurrentLinkedQueue.add(Integer.parseInt(message.getPayload())));
    }

    for (int i = 0; i < messages; i++) {
      eventuateActiveMQProducer.send(destination, key, String.valueOf(i));
    }

    Eventually.eventually(() -> Assertions.assertEquals(messages, concurrentLinkedQueue.size()));

    for (int i = 0; i < messages; i++) {
      Assertions.assertEquals(i, (int)concurrentLinkedQueue.poll());
    }
  }

  private void testSeveralSubscribers(String destination) {
    int messages = 10;
    int consumers = 2;

    String key = "key";

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    for (int i = 0; i < consumers; i ++) {
      messageConsumerActiveMQ.subscribe("subscriber" + i, Collections.singleton(destination), message ->
              concurrentLinkedQueue.add(Integer.parseInt(message.getPayload())));
    }

    for (int i = 0; i < messages; i++) {
      eventuateActiveMQProducer.send(destination, key, String.valueOf(i));
    }

    Eventually.eventually(() -> Assertions.assertEquals(messages * consumers, concurrentLinkedQueue.size()));
  }

  @Test
  public void testSubscriberWithPeriods() {

    ConcurrentLinkedQueue<String> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    String topic = "io.eventuate.SomeClass" + uniquePostfix;
    String key = "some.key" + uniquePostfix;
    String payload = "some.data" + uniquePostfix;
    String subscriberId = "io.eventuate.Subscriber" + uniquePostfix;

    messageConsumerActiveMQ.subscribe(subscriberId,
            Collections.singleton(topic),
            message -> concurrentLinkedQueue.add(message.getPayload()));

    eventuateActiveMQProducer.send(topic, key, payload);

    Eventually.eventually(() -> Assertions.assertEquals(1, concurrentLinkedQueue.size()));
  }
}
