package io.eventuate.messaging.activemq.consumer;

import io.eventuate.messaging.activemq.common.EventuateActiveMQCommonConfiguration;
import io.eventuate.messaging.activemq.common.EventuateActiveMQConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Optional;

@Configuration
@Import(EventuateActiveMQCommonConfiguration.class)
public class MessageConsumerActiveMQConfiguration {

  @Bean
  public MessageConsumerActiveMQImpl messageConsumer(EventuateActiveMQConfigurationProperties eventuateActiveMQConfigurationProperties) {
    return new MessageConsumerActiveMQImpl(eventuateActiveMQConfigurationProperties.getUrl(),
            Optional.ofNullable(eventuateActiveMQConfigurationProperties.getUser()),
            Optional.ofNullable(eventuateActiveMQConfigurationProperties.getPassword()));
  }
}
