package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SocketsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocketsApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(ApplicationClient client) {
		return args -> client.sendAndReceive(new Foo("Client", "Request"))
				.subscribe(System.err::println);
	}

}
