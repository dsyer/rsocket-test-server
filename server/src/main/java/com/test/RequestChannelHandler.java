package com.test;

import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component("request-channel")
public class RequestChannelHandler implements
		Function<Message<Flux<Map<String, Object>>>, Message<Flux<Map<String, Object>>>> {

	private static final Logger log = LoggerFactory
			.getLogger(RequestChannelHandler.class);

	private final RSocketMessageCatalog catalog;

	public RequestChannelHandler(RSocketMessageCatalog catalog) {
		this.catalog = catalog;
	}

	@Override
	public Message<Flux<Map<String, Object>>> apply(
			Message<Flux<Map<String, Object>>> input) {
		log.info("Incoming: " + input);
		// create a stream response and return it
		RSocketMessageHeaders headers = new RSocketMessageHeaders(input.getHeaders());
		String destination = headers.getDestination();
		return MessageBuilder.withPayload(input.getPayload().flatMap(t -> {
			// create a single response and return it
			for (MessageMap map : catalog.getMappings()) {
				if (map.matches(t, destination)) {
					return Flux.fromIterable(map.getResponses());
				}
			}
			throw new IllegalStateException(
					"No MessageMap found to match: " + destination);
		})).copyHeadersIfAbsent(input.getHeaders()).build();
	}

}