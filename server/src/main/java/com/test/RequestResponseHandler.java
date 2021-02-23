package com.test;

import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component("request-response")
public class RequestResponseHandler
		implements Function<Message<Map<String, Object>>, Message<Map<String, Object>>> {

	private static final Logger log = LoggerFactory
			.getLogger(RequestResponseHandler.class);

	private final RSocketMessageCatalog catalog;

	public RequestResponseHandler(RSocketMessageCatalog catalog) {
		this.catalog = catalog;
	}

	@Override
	public Message<Map<String, Object>> apply(Message<Map<String, Object>> t) {
		log.info("Incoming: " + t);
		RSocketMessageHeaders headers = new RSocketMessageHeaders(t.getHeaders());
		String destination = headers.getDestination();
		// create a single response and return it
		for (MessageMap map : catalog.getMappings()) {
			if (map.matches(t.getPayload(), destination)) {
				return MessageBuilder.withPayload(map.getResponse())
						.copyHeadersIfAbsent(t.getHeaders()).build();
			}
		}
		throw new IllegalStateException("No MessageMap found to match: " + destination);
	}

}