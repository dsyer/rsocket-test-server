package org.springframework.mock.rsocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeType;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(RSocketServerExtension.class)
class DynamicRouteTests {

	private RSocketRequester rsocketRequester;

	public DynamicRouteTests(@Autowired RSocketRequester.Builder rsocketRequesterBuilder,
			@Value("${rsocket.host:localhost}") String host,
			@Value("${test.rsocket.server.port:7000}") int port) {
		rsocketRequester = rsocketRequesterBuilder
				.dataMimeType(MimeType.valueOf("application/json")).tcp(host, port);
	}

	@Test
	void inject(RSocketMessageCatalog catalog) {
		assertThat(catalog).isNotNull();
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
	}

	@Test
	void handler(RSocketMessageRegistry catalog) {
		MessageMapping response = MessageMapping.<Foo, Foo>response("handler")
				.input(Foo.class).handler(foo -> new Foo("Server", "Response"));
		catalog.register(response);
		assertThat(rsocketRequester.route("handler").data(new Foo("Client", "Request"))
				.retrieveMono(Foo.class).doOnNext(foo -> {
					System.err.println(foo);
					assertThat(foo.getOrigin()).isEqualTo("Server");
				}).block()).isNotNull();
		assertThat(response.drain()).hasSize(1);
	}

	@Test
	void stream(RSocketMessageRegistry catalog) {
		MessageMapping stream = MessageMapping.<Foo, Foo>stream("dynamic")
				.input(Foo.class)
				.handler(foo -> new Foo[] { new Foo("Server", "Stream") });
		catalog.register(stream);
		assertThat(rsocketRequester.route("dynamic").data(new Foo("Client", "Request"))
				.retrieveFlux(Foo.class).take(3).doOnNext(foo -> {
					System.err.println(foo);
					assertThat(foo.getOrigin()).isEqualTo("Server");
				}).count().block()).isEqualTo(1);
		assertThat(stream.drain()).hasSize(1);
	}

	@Test
	void multi(RSocketMessageRegistry catalog) {
		MessageMapping stream = MessageMapping.<Foo, Foo>stream("other").input(Foo.class)
				.response(new Foo[] { new Foo("Server", "Stream", 0),
						new Foo("Server", "Stream", 1) });
		catalog.register(stream);
		assertThat(rsocketRequester.route("other").data(new Foo("Client", "Request"))
				.retrieveFlux(Foo.class).take(3).doOnNext(foo -> {
					System.err.println(foo);
					assertThat(foo.getOrigin()).isEqualTo("Server");
				}).count().block()).isEqualTo(2);
		assertThat(stream.drain()).hasSize(1);
	}

	@EnableAutoConfiguration
	@Configuration
	static class Application {
	}

}
