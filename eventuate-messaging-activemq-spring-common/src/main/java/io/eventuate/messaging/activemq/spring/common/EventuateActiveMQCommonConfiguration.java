package io.eventuate.messaging.activemq.spring.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateActiveMQCommonConfiguration {
  @Bean
  public EventuateActiveMQConfigurationProperties eventuateActiveMQConfigurationProperties() {
    return new EventuateActiveMQConfigurationProperties();
  }
}
