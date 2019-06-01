package io.eventuate.messaging.activemq.consumer;

import java.util.function.Consumer;

public interface ActiveMQMessageHandler extends Consumer<ActiveMQMessage> {
}
