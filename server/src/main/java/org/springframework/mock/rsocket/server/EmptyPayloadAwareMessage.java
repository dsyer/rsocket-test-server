package org.springframework.mock.rsocket.server;

import java.util.Collections;
import java.util.Map;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

public class EmptyPayloadAwareMessage implements
    Message<Map<String, Object>> {

  private final Message<?> delegate;

  EmptyPayloadAwareMessage(Message<?> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Map<String, Object> getPayload() {
    if (this.delegate.getPayload() instanceof Map) {
      return (Map<String, Object>) this.delegate.getPayload();
    }
    return Collections.emptyMap();
  }

  @Override
  public MessageHeaders getHeaders() {
    return this.delegate.getHeaders();
  }
}
