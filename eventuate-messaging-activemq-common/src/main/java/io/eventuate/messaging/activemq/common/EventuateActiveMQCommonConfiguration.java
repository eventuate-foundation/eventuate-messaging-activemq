package io.eventuate.messaging.activemq.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateActiveMQCommonConfiguration {
  @Bean
  public EventuateActiveMQConfigurationProperties eventuateActiveMQConfigurationProperties() {
    return new EventuateActiveMQConfigurationProperties();
  }
}
