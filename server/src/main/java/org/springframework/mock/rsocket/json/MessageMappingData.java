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
package org.springframework.mock.rsocket.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rsocket.frame.FrameType;
import reactor.core.publisher.Flux;

import org.springframework.mock.rsocket.MessageMapping;

/**
 * @author Dave Syer
 *
 */
class MessageMappingData {

	private Map<String, Object> request = new HashMap<>();

	private FrameType frameType;

	private String pattern;

	private int repeat = 1;

	private List<Map<String, Object>> responses = new ArrayList<>();

	public Map<String, Object> getResponse() {
		if (responses.isEmpty()) {
			responses.add(new HashMap<>());
		}
		return this.responses.get(0);
	}

	public List<Map<String, Object>> getResponses() {
		List<Map<String, Object>> result = new ArrayList<>();
		int total = repeat <= 0 ? 0 : repeat;
		for (int i = 0; i < total; i++) {
			result.addAll(responses);
		}
		return result;
	}

	public int getRepeat() {
		return repeat;
	}

	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}

	public Map<String, Object> getRequest() {
		return request;
	}

	public FrameType getFrameType() {
		return frameType;
	}

	public void setFrameType(FrameType frameType) {
		this.frameType = frameType;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public MessageMapping mapping() {
		switch (frameType) {
		case REQUEST_FNF:
			return MessageMapping.forget(pattern).request(this.request).build();
		case REQUEST_RESPONSE:
			return MessageMapping.response(pattern).request(this.request)
					.handler(Object.class, input -> getResponse());
		case REQUEST_STREAM:
			return MessageMapping.stream(pattern).request(this.request).handler(
					Object.class, input -> getResponses().toArray(new Object[0]));
		case REQUEST_CHANNEL:
			return MessageMapping.channel(pattern).request(this.request).handler(
					Object.class,
					input -> input.flatMap(item -> Flux.fromIterable(getResponses())));
		default:
			throw new IllegalStateException("Cannot create: " + frameType);
		}
	}

}
