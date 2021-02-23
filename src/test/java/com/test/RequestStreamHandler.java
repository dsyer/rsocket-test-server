package com.test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component("request-stream")
public class RequestStreamHandler implements
		Function<Message<Map<String, Object>>, Message<List<Map<String, Object>>>> {

	private static final Logger log = LoggerFactory.getLogger(RequestStreamHandler.class);

	private final RSocketMessageCatalog catalog;

	public RequestStreamHandler(RSocketMessageCatalog catalog) {
		this.catalog = catalog;
	}

	@Override
	public Message<List<Map<String, Object>>> apply(Message<Map<String, Object>> t) {
		log.info("Incoming: " + t);
		// create a stream response and return it
		RSocketMessageHeaders headers = new RSocketMessageHeaders(t.getHeaders());
		String destination = headers.getDestination();
		// create a single response and return it
		for (MessageMap map : catalog.getMappings()) {
			if (map.matches(t.getPayload(), destination)) {
				return MessageBuilder.withPayload(map.getResponses())
						.copyHeadersIfAbsent(t.getHeaders()).build();
			}
		}
		throw new IllegalStateException("No MessageMap found to match: " + destination);
	}

}