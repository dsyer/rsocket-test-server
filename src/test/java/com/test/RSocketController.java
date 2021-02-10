package com.test;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class RSocketController {

	private static final Logger log = LoggerFactory.getLogger(RSocketController.class);

	private final RSocketMessageCatalog catalog;

	public RSocketController(RSocketMessageCatalog catalog) {
		this.catalog = catalog;
	}

	@MessageMapping("*")
	Map<String, Object> requestResponse(@Payload Map<String, Object> request,
			@Headers Map<String, Object> headers) {
		log.info("Received request-response request: {}, {}", request, headers);
		// create a single response and return it
		return catalog.getRequestResponse(headers);
	}

}