package com.test;

import com.function.RSocketConfiguration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RSocketConfiguration.class)
public class TestApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(TestApplication.class)
				.properties("spring.rsocket.server.port=7000").run(args);
	}

}
