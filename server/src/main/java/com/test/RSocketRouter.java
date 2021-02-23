package com.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.function.context.FunctionProperties;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component("fakeFunctionRouter")
public class RSocketRouter implements MessageRoutingCallback {

	private static final Logger log = LoggerFactory.getLogger(RSocketRouter.class);

	@Override
	public String route(Message<?> message, FunctionProperties functionProperties) {
		log.info("Routing: " + message);
		RSocketMessageHeaders headers = new RSocketMessageHeaders(message.getHeaders());
		switch (headers.getFrameType()) {
		case REQUEST_RESPONSE:
			return "request-response";
		case REQUEST_STREAM:
			return "request-stream";
		case REQUEST_FNF:
			return "fire-and-forget";
		case REQUEST_CHANNEL:
			return "request-channel";
		default:
			throw new IllegalStateException("Cannot route: " + headers.getFrameType());
		}
	}

}