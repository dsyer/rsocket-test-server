package com.example;

import com.test.RSocketServerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(RSocketServerExtension.class)
class SocketsApplicationTests {

	@Test
	void contextLoads() {
	}

}
