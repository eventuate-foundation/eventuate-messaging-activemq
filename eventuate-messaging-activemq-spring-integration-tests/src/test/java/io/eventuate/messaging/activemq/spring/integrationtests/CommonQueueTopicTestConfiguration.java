package io.eventuate.messaging.activemq.spring.integrationtests;

import io.eventuate.messaging.activemq.spring.common.ChannelType;
import io.eventuate.messaging.activemq.spring.common.EventuateActiveMQCommonConfiguration;
import io.eventuate.messaging.activemq.spring.common.EventuateActiveMQConfigurationProperties;
import io.eventuate.messaging.activemq.spring.consumer.MessageConsumerActiveMQImpl;
import io.eventuate.messaging.activemq.producer.EventuateActiveMQProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableAutoConfiguration
@Import(EventuateActiveMQCommonConfiguration.class)
public class CommonQueueTopicTestConfiguration {

  @Bean
  @Qualifier("uniquePostfix")
  public String uniquePostfix() {
    return UUID.randomUUID().toString();
  }

  @Bean
  public MessageConsumerActiveMQImpl activeMQMessageConsumer(EventuateActiveMQConfigurationProperties eventuateActiveMQConfigurationProperties,
                                                             @Qualifier("uniquePostfix") String uniquePostfix,
                                                             @Qualifier("testChannelType") ChannelType channelType) {
    return new MessageConsumerActiveMQImpl(eventuateActiveMQConfigurationProperties.getUrl(),
            Collections.singletonMap("destination" + uniquePostfix, channelType),
            Optional.ofNullable(eventuateActiveMQConfigurationProperties.getUser()),
            Optional.ofNullable(eventuateActiveMQConfigurationProperties.getPassword()));
  }

  @Bean
  public EventuateActiveMQProducer activeMQMessageProducer(EventuateActiveMQConfigurationProperties eventuateActiveMQConfigurationProperties,
                                                           @Qualifier("uniquePostfix") String uniquePostfix,
                                                           @Qualifier("testChannelType") ChannelType channelType) {
    return new EventuateActiveMQProducer(eventuateActiveMQConfigurationProperties.getUrl(),
            Collections.singletonMap("destination" + uniquePostfix, channelType),
            Optional.ofNullable(eventuateActiveMQConfigurationProperties.getUser()),
            Optional.ofNullable(eventuateActiveMQConfigurationProperties.getPassword()));
  }
}
