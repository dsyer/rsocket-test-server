package org.springframework.mock.rsocket;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RSocketServerConfiguration.class)
public class RSocketServerApplication {

	public static void main(String[] args) {
		run(args);
	}

	public static ConfigurableApplicationContext run(String... args) {
		return new SpringApplicationBuilder(RSocketServerApplication.class)
				.properties("spring.rsocket.server.port=7000",
						"spring.main.web-application-type=none")
				.run(args);
	}

}
