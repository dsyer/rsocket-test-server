package com.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component("fakeFunctionRouter")
public class RSocketRouter {

	private static final Logger log = LoggerFactory.getLogger(RSocketRouter.class);

	public String route(Message<?> message) {
		log.info("Routing: " + message);
		RSocketMessageHeaders headers = new RSocketMessageHeaders(message.getHeaders());
		switch (headers.getFrameType()) {
		case REQUEST_RESPONSE:
			return "request-response";
		default:
			throw new IllegalStateException("Cannot route: " + headers.getFrameType());
		}
	}

}