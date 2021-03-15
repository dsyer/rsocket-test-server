package org.springframework.mock.rsocket;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(RSocketServerExtension.class)
@Disabled
class CborTests {

	private RSocketRequester rsocketRequester;

	public CborTests(@Autowired RSocketRequester.Builder rsocketRequesterBuilder,
			@Value("${test.rsocket.server.port:7000}") int port) {
		rsocketRequester = rsocketRequesterBuilder.tcp("localhost", port);
	}

	@Test
	void response(RSocketMessageRegistry catalog) {
		MessageMapping response = MessageMapping.response("response")
				.response(new Foo("Server", "Response"));
		catalog.register(response);
		assertThat(rsocketRequester.route("response").data(new Foo("Client", "Request"))
				.retrieveMono(Foo.class).doOnNext(foo -> {
					System.err.println(foo);
					assertThat(foo.getOrigin()).isEqualTo("Server");
				}).block()).isNotNull();
		assertThat(response.drain()).hasSize(1);
		assertThat(response.drain()).hasSize(0);
	}

	@EnableAutoConfiguration
	@Configuration
	static class Application {
	}

}
