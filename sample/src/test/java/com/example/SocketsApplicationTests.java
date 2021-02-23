package com.example;

import com.test.TestApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Hooks;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest("io.rsocket.routing.client.service-name=application")
class SocketsApplicationTests {

	private static ConfigurableApplicationContext context;

	static {
		Hooks.onOperatorDebug();
		context = TestApplication.run();
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@AfterAll
	public static void close() {
		if (context != null && context.isRunning()) {
			context.close();
		}
	}

	@Test
	void contextLoads() {
		System.err.println();
	}

}
