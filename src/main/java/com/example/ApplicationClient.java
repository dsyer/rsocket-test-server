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
package com.example;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

/**
 * @author Dave Syer
 *
 */
@Component
public class ApplicationClient {

	private RSocketRequester rsocketRequester;

	public ApplicationClient(RSocketRequester.Builder rsocketRequesterBuilder,
			@Value("${rsocket.host:localhost}") String host,
			@Value("${rsocket.port:7000}") int port) {
		rsocketRequester = rsocketRequesterBuilder
				.dataMimeType(MimeType.valueOf("application/json")).tcp(host, port);
	}

	public Mono<Foo> sendAndReceive(Foo foo) {
		return rsocketRequester.route("").data(foo).retrieveMono(Foo.class);
	}

}
