package io.eventuate.messaging.activemq.spring.consumer;

public class ActiveMQMessage {
  private String payload;

  public ActiveMQMessage(String payload) {
    this.payload = payload;
  }

  public String getPayload() {
    return payload;
  }
}
