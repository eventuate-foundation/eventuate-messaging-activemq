package io.eventuate.messaging.activemq.spring.consumer;

import java.util.function.Consumer;

public interface ActiveMQMessageHandler extends Consumer<ActiveMQMessage> {
}
