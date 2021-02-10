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
package com.test;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * @author Dave Syer
 *
 */
@Component
public class JsonRSocketMessageCatalog implements RSocketMessageCatalog {

	@Override
	public Map<String, Object> getRequestResponse(Map<String, Object> headers) {
		RSocketMessageHeaders copy = new RSocketMessageHeaders();
		copy.putAll(headers);
		// ... match the destination (it's a Route)
		return new HashMap<>() {
			{
				put("origin", "Server");
				put("interaction", "Response");
			}
		};
	}

}
