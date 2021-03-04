package org.springframework.mock.rsocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeType;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(RSocketServerExtension.class)
class DynamicRouteTests {

	private RSocketRequester rsocketRequester;

	@Autowired
	public DynamicRouteTests(RSocketRequester.Builder rsocketRequesterBuilder,
			RSocketStrategies strategies, @Value("${rsocket.host:localhost}") String host,
			@Value("${rsocket.port:7000}") int port) {
		rsocketRequester = rsocketRequesterBuilder.rsocketStrategies(strategies)
				.dataMimeType(MimeType.valueOf("application/json")).tcp(host, port);
	}

	@Test
	void inject(RSocketMessageCatalog catalog) {
		assertThat(catalog).isNotNull();
	}

	@Test
	void stream(RSocketMessageRegistry catalog) {
		MessageMapping stream = MessageMapping.stream("dynamic");
		stream.handler(Foo.class, foo -> new Foo("Server", "Stream"));
		catalog.register(stream);
		assertThat(rsocketRequester.route("dynamic").data(new Foo("Client", "Request"))
				.retrieveFlux(Foo.class).take(3).doOnNext(foo -> {
					System.err.println(foo);
					assertThat(foo.getOrigin()).isEqualTo("Server");
				}).count().block()).isEqualTo(1);
	}

	@Test
	void multi(RSocketMessageRegistry catalog) {
		MessageMapping stream = MessageMapping.stream("dynamic");
		stream.handler(Foo.class, foo -> new Foo[] { new Foo("Server", "Stream", 0),
				new Foo("Server", "Stream", 1) });
		catalog.register(stream);
		assertThat(rsocketRequester.route("dynamic").data(new Foo("Client", "Request"))
				.retrieveFlux(Foo.class).take(3).doOnNext(foo -> {
					System.err.println(foo);
					assertThat(foo.getOrigin()).isEqualTo("Server");
				}).count().block()).isEqualTo(2);
	}

	@EnableAutoConfiguration
	@Configuration
	static class Application {
	}

}
