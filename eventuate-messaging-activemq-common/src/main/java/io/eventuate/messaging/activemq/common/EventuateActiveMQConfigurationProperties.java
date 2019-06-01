package io.eventuate.messaging.activemq.common;

import org.springframework.beans.factory.annotation.Value;

public class EventuateActiveMQConfigurationProperties {

  @Value("${activemq.url}")
  private String url;

  @Value("${activemq.user:#{null}}")
  private String user;

  @Value("${activemq.password:#{null}}")
  private String password;

  public String getUrl() {
    return url;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }
}
