package io.eventuate.messaging.activemq.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramConsumerActiveMQConfiguration {
  @Bean
  public MessageConsumerActiveMQImpl messageConsumer(@Value("${activemq.url}") String activeMQURL) {
    return new MessageConsumerActiveMQImpl(activeMQURL);
  }
}
