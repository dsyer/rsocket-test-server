package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.rsocket.RSocketMessageRegistry;
import org.springframework.mock.rsocket.RSocketServerExtension;
import org.springframework.mock.rsocket.MessageMappingSpec;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest("rsocket.port=${test.rsocket.server.port}")
@AutoConfigureWebTestClient
@ExtendWith(RSocketServerExtension.class)
class SocketsApplicationTests {

	@Autowired
	private WebTestClient http;

	@Test
	void requestResponse(RSocketMessageRegistry catalog) {
		catalog.register(MessageMappingSpec.response("hello")
				.response(new Foo("Server", "response")));
		http.get().uri("/").exchange().expectStatus().isOk().expectBody(Foo.class)
				.value(foo -> assertThat(foo.getOrigin()).isEqualTo("Server"));
	}

	@Test
	void forget() {
		http.get().uri("/forget").exchange().expectStatus().isOk()
				.expectBody(String.class).value(foo -> assertThat(foo).isNull());
	}

	@Test
	void stream() {
		assertThat(http.get().uri("/stream").exchange().expectStatus().isOk()
				.returnResult(Foo.class).getResponseBody().take(3).doOnNext(foo -> {
					System.err.println(foo);
					assertThat(foo.getOrigin()).isEqualTo("Server");
				}).count().block()).isEqualTo(3);
	}

	@Test
	void channel() {
		assertThat(http.get().uri("/channel").exchange().expectStatus().isOk()
				.returnResult(Foo.class).getResponseBody().take(2).doOnNext(foo -> {
					System.err.println(foo);
					assertThat(foo.getOrigin()).isEqualTo("Server");
				}).count().block()).isEqualTo(2);
	}

	@Test
	void longStream() {
		assertThat(http.get().uri("/long").exchange().expectStatus().isOk()
				.returnResult(Foo.class).getResponseBody().take(6).count().block())
						.isEqualTo(6);
	}

}
