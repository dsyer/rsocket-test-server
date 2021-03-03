package org.springframework.mock.rsocket;

import java.util.Map;
import java.util.function.Function;

import io.rsocket.frame.FrameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class GenericRequestHandler implements
		Function<Flux<Message<Map<String, Object>>>, Flux<Message<Map<String, Object>>>> {

	private static final Logger log = LoggerFactory
			.getLogger(GenericRequestHandler.class);

	private final RSocketMessageCatalog catalog;

	private FrameType frameType;

	public GenericRequestHandler(FrameType frameType, RSocketMessageCatalog catalog) {
		this.frameType = frameType;
		this.catalog = catalog;
	}

	@Override
	public Flux<Message<Map<String, Object>>> apply(
			Flux<Message<Map<String, Object>>> input) {
		return input.flatMap(message -> {
			log.info("Incoming: " + message);
			RSocketMessageHeaders headers = new RSocketMessageHeaders(
					message.getHeaders());
			String destination = headers.getDestination();
			for (MessageMap map : catalog.getMappings()) {
				if (map.getFrameType() == frameType
						&& map.matches(message.getPayload(), destination)) {
					return map.handle(input.map(msg -> msg.getPayload()))
							.map(response -> MessageBuilder.withPayload(response)
									.copyHeadersIfAbsent(message.getHeaders()).build());
				}
			}
			throw new IllegalStateException(
					"No MessageMap found to match: " + destination);
		});
	}
}