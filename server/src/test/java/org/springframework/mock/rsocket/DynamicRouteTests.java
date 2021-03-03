package org.springframework.mock.rsocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeType;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
@ExtendWith(RSocketServerExtension.class)
class DynamicRouteTests {

	private RSocketRequester rsocketRequester;

	@Autowired
	public DynamicRouteTests(RSocketRequester.Builder rsocketRequesterBuilder,
			@Value("${rsocket.host:localhost}") String host,
			@Value("${rsocket.port:7000}") int port) {
		rsocketRequester = rsocketRequesterBuilder
				.dataMimeType(MimeType.valueOf("application/json")).tcp(host, port);
	}

	@Test
	void inject(RSocketMessageCatalog catalog) {
		assertThat(catalog).isNotNull();
	}

	@Test
	void stream(RSocketMessageRegistry catalog) {
		MessageMap stream = MessageMap.stream("dynamic");
		stream.getResponse().put("origin", "Server");
		catalog.register(stream);
		assertThat(rsocketRequester.route("dynamic").data(new Foo("Client", "Request"))
				.retrieveFlux(Foo.class).take(3).doOnNext(foo -> {
					System.err.println(foo);
					assertThat(foo.getOrigin()).isEqualTo("Server");
				}).count().block()).isEqualTo(1);
	}

	@EnableAutoConfiguration
	@Configuration
	static class Application {
	}

}
