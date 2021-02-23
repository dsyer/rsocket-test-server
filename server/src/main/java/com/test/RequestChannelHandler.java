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
		Function<Flux<Message<Map<String, Object>>>, Flux<Message<Map<String, Object>>>> {
	private static final Logger log = LoggerFactory
			.getLogger(RequestChannelHandler.class);
	private final RSocketMessageCatalog catalog;

	public RequestChannelHandler(RSocketMessageCatalog catalog) {
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
				if (map.matches(message.getPayload(), destination)) {
					return Flux.fromIterable(map.getResponses())
							.map(response -> MessageBuilder.withPayload(response)
									.copyHeadersIfAbsent(message.getHeaders()).build());
				}
			}
			throw new IllegalStateException(
					"No MessageMap found to match: " + destination);
		});
	}
}