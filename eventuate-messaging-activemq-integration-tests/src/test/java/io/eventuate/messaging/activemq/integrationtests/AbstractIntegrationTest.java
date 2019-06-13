package io.eventuate.messaging.activemq.integrationtests;

import com.google.common.collect.ImmutableSet;
import io.eventuate.messaging.activemq.consumer.MessageConsumerActiveMQImpl;
import io.eventuate.messaging.activemq.producer.EventuateActiveMQProducer;
import io.eventuate.util.test.async.Eventually;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractIntegrationTest {

  @Autowired
  protected MessageConsumerActiveMQImpl messageConsumerActiveMQ;

  @Autowired
  protected EventuateActiveMQProducer eventuateActiveMQProducer;

  @Autowired
  protected String uniquePostfix;

  @Test
  public void testThatMessageConsumingStoppedAfterFirstException() throws Exception {
    int messages = 10;

    String destination = "destination" + uniquePostfix;
    String key = "key" + uniquePostfix;
    String subscriber = "subscriber" + uniquePostfix;

    AtomicInteger exceptions = new AtomicInteger(0);

    messageConsumerActiveMQ.subscribe(subscriber, ImmutableSet.of(destination), message -> {
      try {
        throw new RuntimeException("Test");
      } finally {
        exceptions.incrementAndGet();
      }
    });

    for (int i = 0; i < messages; i++) {
      eventuateActiveMQProducer.send(destination, key, String.valueOf(i));
    }

    Eventually.eventually(() -> Assert.assertEquals(exceptions.get(), 1));
    Thread.sleep(3000);
    Assert.assertEquals(exceptions.get(), 1);
  }
}
