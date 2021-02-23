package com.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TestApplication {

	public static void main(String[] args) {
		run(args);
	}

	public static ConfigurableApplicationContext run(String... args) {
		return new SpringApplicationBuilder(TestApplication.class)
				.properties("spring.rsocket.server.port=7000",
						"spring.main.web-application-type=none")
				.run(args);
	}

}
