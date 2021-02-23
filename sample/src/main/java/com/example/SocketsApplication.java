package com.example;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SocketsApplication {

	private ApplicationClient client;

	public SocketsApplication(ApplicationClient client) {
		this.client = client;
	}

	public static void main(String[] args) {
		SpringApplication.run(SocketsApplication.class, args);
	}

	@GetMapping("/")
	public Mono<Foo> foo() {
		return client.sendAndReceive(new Foo("Client", "Request"));
	}

	@GetMapping("/stream")
	public Flux<Foo> stream() {
		return client.stream(new Foo("Client", "Request"));
	}

	@GetMapping("/long")
	public Flux<Foo> longStream() {
		return client.longStream(new Foo("Client", "Request"));
	}

	@GetMapping("/forget")
	public Mono<Void> forget() {
		return client.forget(new Foo("Client", "Request"));
	}

}
