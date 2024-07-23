package org.springframework.mock.rsocket;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.mock.rsocket.server.RSocketServerConfiguration;

@SpringBootApplication
@Import(RSocketServerConfiguration.class)
public class RSocketServerApplication {

  public static void main(String[] args) {
    RSocketServerExtension.run(args);
  }

}
