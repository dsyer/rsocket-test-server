package org.springframework.mock.rsocket;

import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.messaging.Message;

public class FireAndForgetHandler implements Consumer<Message<Map<String, Object>>> {

	private static final Logger log = LoggerFactory.getLogger(FireAndForgetHandler.class);

	private final RSocketMessageCatalog catalog;

	public FireAndForgetHandler(RSocketMessageCatalog catalog) {
		this.catalog = catalog;
	}

	@Override
	public void accept(Message<Map<String, Object>> t) {
		log.info("Incoming: " + t);
		RSocketMessageHeaders headers = new RSocketMessageHeaders(t.getHeaders());
		String destination = headers.getDestination();
		for (MessageMap map : catalog.getMappings()) {
			if (map.matches(t.getPayload(), destination)) {
				return;
			}
		}
		throw new IllegalStateException("No MessageMap found to match: " + destination);
	}

}