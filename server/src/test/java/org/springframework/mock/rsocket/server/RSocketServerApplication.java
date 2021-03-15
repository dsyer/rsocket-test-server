package org.springframework.mock.rsocket.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.mock.rsocket.RSocketServerExtension;

@SpringBootApplication
@Import(RSocketServerConfiguration.class)
public class RSocketServerApplication {

	public static void main(String[] args) {
		RSocketServerExtension.run(args);
	}

}
