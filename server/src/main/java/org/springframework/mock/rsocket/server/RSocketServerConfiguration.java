/*
 * Copyright 2020-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.mock.rsocket.server;

import io.rsocket.frame.FrameType;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.rsocket.RSocketMessageCatalog;
import org.springframework.mock.rsocket.RSocketMessageRegistry;
import org.springframework.mock.rsocket.json.JsonRSocketMessageCatalog;

/**
 * @author Dave Syer
 *
 */
@EnableAutoConfiguration
@ComponentScan
@Configuration(proxyBeanMethods = false)
public class RSocketServerConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public RSocketMessageRegistry rSocketMessageCatalog() {
		return new JsonRSocketMessageCatalog();
	}

	@Bean
	public RSocketRouter customFunctionRouter() {
		return new RSocketRouter();
	}

	@Bean("fire-and-forget")
	public GenericRequestHandler fireAndForgetHandler(RSocketMessageCatalog catalog) {
		return new GenericRequestHandler(FrameType.REQUEST_FNF, catalog);
	}

	@Bean("request-response")
	public GenericRequestHandler requestResponseHandler(RSocketMessageCatalog catalog) {
		return new GenericRequestHandler(FrameType.REQUEST_RESPONSE, catalog);
	}

	@Bean("request-stream")
	public GenericRequestHandler requestStreamHandler(RSocketMessageCatalog catalog) {
		return new GenericRequestHandler(FrameType.REQUEST_STREAM, catalog);
	}

	@Bean("request-channel")
	public GenericRequestHandler requestChannelHandler(RSocketMessageCatalog catalog) {
		return new GenericRequestHandler(FrameType.REQUEST_CHANNEL, catalog);
	}
}
