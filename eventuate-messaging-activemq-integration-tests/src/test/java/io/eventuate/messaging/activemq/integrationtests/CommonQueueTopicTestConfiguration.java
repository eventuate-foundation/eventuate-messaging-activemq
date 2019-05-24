package io.eventuate.messaging.activemq.integrationtests;

import io.eventuate.messaging.activemq.common.ChannelType;
import io.eventuate.messaging.activemq.consumer.MessageConsumerActiveMQImpl;
import io.eventuate.messaging.activemq.producer.EventuateActiveMQProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.UUID;

@Configuration
@EnableAutoConfiguration
public class CommonQueueTopicTestConfiguration {

  @Bean
  @Qualifier("uniquePostfix")
  public String uniquePostfix() {
    return UUID.randomUUID().toString();
  }

  @Bean
  public MessageConsumerActiveMQImpl messageConsumerKafka(@Value("${activemq.url}") String activeMQURL,
                                                          @Qualifier("uniquePostfix") String uniquePostfix,
                                                          @Qualifier("testChannelType") ChannelType channelType) {
    return new MessageConsumerActiveMQImpl(activeMQURL,
            Collections.singletonMap("destination" + uniquePostfix, channelType));
  }

  @Bean
  public EventuateActiveMQProducer activeMQMessageProducer(@Value("${activemq.url}") String activeMQURL,
                                                           @Qualifier("uniquePostfix") String uniquePostfix,
                                                           @Qualifier("testChannelType") ChannelType channelType) {
    return new EventuateActiveMQProducer(activeMQURL,
            Collections.singletonMap("destination" + uniquePostfix, channelType));
  }
}
