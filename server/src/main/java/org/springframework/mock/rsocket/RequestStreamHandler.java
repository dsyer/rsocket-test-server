package org.springframework.mock.rsocket;

import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class RequestStreamHandler implements
		Function<Message<Map<String, Object>>, Flux<Message<Map<String, Object>>>> {

	private static final Logger log = LoggerFactory.getLogger(RequestStreamHandler.class);

	private final RSocketMessageCatalog catalog;

	public RequestStreamHandler(RSocketMessageCatalog catalog) {
		this.catalog = catalog;
	}

	@Override
	public Flux<Message<Map<String, Object>>> apply(Message<Map<String, Object>> t) {
		log.info("Incoming: " + t);
		// create a stream response and return it
		RSocketMessageHeaders headers = new RSocketMessageHeaders(t.getHeaders());
		String destination = headers.getDestination();
		// create a single response and return it
		for (MessageMap map : catalog.getMappings()) {
			if (map.matches(t.getPayload(), destination)) {
				return Flux.fromIterable(map.getResponses())
						.map(response -> MessageBuilder.withPayload(response)
								.copyHeadersIfAbsent(t.getHeaders()).build());
			}
		}
		throw new IllegalStateException("No MessageMap found to match: " + destination);
	}

}