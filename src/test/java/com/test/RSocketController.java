package com.test;

import java.util.Map;

import com.example.Foo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class RSocketController {

	private static final Logger log = LoggerFactory.getLogger(RSocketController.class);

	@MessageMapping("hello")
	Foo requestResponse(@Payload Foo request, @Headers Map<String, Object> headers) {
		log.info("Received request-response request: {}, {}", request, headers);
		// create a single Message and return it
		return new Foo("Server", "Response");
	}

	// @MessageMapping("hello")
	Mono<Void> fireAndForget(@Payload Foo request, @Headers Map<String, Object> headers) {
		log.info("Received fire-and-forget request: {}, {}", request, headers);
		return Mono.empty();
	}

}