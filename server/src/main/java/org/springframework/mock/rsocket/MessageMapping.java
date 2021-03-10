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
package org.springframework.mock.rsocket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rsocket.frame.FrameType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.util.ObjectUtils;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

/**
 * @author Dave Syer
 *
 */
@JsonTypeInfo(use = Id.NAME, property = "frameType", visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(value = RequestResponse.class, name = "REQUEST_RESPONSE"),
		@JsonSubTypes.Type(value = RequestStream.class, name = "REQUEST_STREAM"),
		@JsonSubTypes.Type(value = RequestChannel.class, name = "REQUEST_CHANNEL"),
		@JsonSubTypes.Type(value = FireAndForget.class, name = "REQUEST_FNF") })
public abstract class MessageMapping {

	private Map<String, Object> request = new HashMap<>();

	private PathPatternRouteMatcher matcher = new PathPatternRouteMatcher();

	private FrameType frameType;

	private String pattern;

	private Function<Flux<Map<String, Object>>, Flux<Map<String, Object>>> handler;

	private ObjectMapper objectMapper = new ObjectMapper();

	@JsonIgnore
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public MessageMapping handler(
			Function<Flux<Map<String, Object>>, Flux<Map<String, Object>>> handler) {
		this.handler = handler;
		return this;
	}

	public <I, O> MessageMapping handler(Class<I> input, Function<I, O> handler) {
		this.handler = maps -> maps
				.map(map -> handler.apply(objectMapper.convertValue(map, input)))
				.flatMap(result -> {
					Object value = result;
					if (ObjectUtils.isArray(result)) {
						value = Arrays.asList((Object[]) result);
					}
					if (value instanceof Collection) {
						return Flux
								.fromStream(((Collection<?>) value).stream().map(item -> {
									@SuppressWarnings("unchecked")
									Map<String, Object> map = objectMapper
											.convertValue(item, Map.class);
									return map;
								}));
					}
					else {
						@SuppressWarnings("unchecked")
						Map<String, Object> map = objectMapper.convertValue(value,
								Map.class);
						return Mono.just(map);
					}
				});
		return this;
	}

	public <I, O> MessageMapping mapping(Class<I> input,
			Function<Flux<I>, Flux<O>> handler) {
		this.handler = maps -> handler
				.apply(maps.map(map -> objectMapper.convertValue(map, input)))
				.flatMap(result -> {
					Object value = result;
					if (ObjectUtils.isArray(result)) {
						value = Arrays.asList((Object[]) result);
					}
					if (value instanceof Collection) {
						return Flux
								.fromStream(((Collection<?>) value).stream().map(item -> {
									@SuppressWarnings("unchecked")
									Map<String, Object> map = objectMapper
											.convertValue(item, Map.class);
									return map;
								}));
					}
					else {
						@SuppressWarnings("unchecked")
						Map<String, Object> map = objectMapper.convertValue(value,
								Map.class);
						return Mono.just(map);
					}
				});
		return this;
	}

	public <I> MessageMapping request(I input) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = objectMapper.convertValue(input, Map.class);
		this.request = map;
		return this;
	}

	private boolean matches(Map<String, Object> source, Map<String, Object> target) {
		for (String key : source.keySet()) {
			if (!target.containsKey(key)) {
				return false;
			}
			Object object = target.get(key);
			if (object instanceof String) {
				Object pattern = source.get(key);
				if (pattern instanceof String) {
					if (!matcher.match((String) pattern, matcher.parseRoute(key))) {
						return false;
					}
				}
				else if (object != null) {
					return object.equals(pattern);
				}
			}
			else if (object instanceof Map) {
				Object other = source.get(key);
				if (target instanceof Map) {
					@SuppressWarnings({ "unchecked", "rawtypes" })
					boolean matches = matches((Map) other, (Map) object);
					if (!matches) {
						return false;
					}
				}
				else {
					return false;
				}
			}
			else {
				return source.get(key) == null;
			}
		}
		return true;
	}

	public boolean matches(Map<String, Object> request, String destination) {
		return matches(this.request, request)
				&& matcher.match(this.pattern, matcher.parseRoute(destination));
	}

	public Flux<Map<String, Object>> handle(Flux<Map<String, Object>> input) {
		return handler.apply(input);
	}

	public PathPatternRouteMatcher getMatcher() {
		return matcher;
	}

	public Map<String, Object> getRequest() {
		return request;
	}

	public void setMatcher(PathPatternRouteMatcher matcher) {
		this.matcher = matcher;
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

	public static MessageMapping stream(String pattern) {
		RequestStream result = new RequestStream();
		result.setPattern(pattern);
		return result;
	}

	public static MessageMapping forget(String pattern) {
		FireAndForget result = new FireAndForget();
		result.setPattern(pattern);
		return result;
	}

	public static MessageMapping channel(String pattern) {
		RequestChannel result = new RequestChannel();
		result.setPattern(pattern);
		return result;
	}

	public static MessageMapping response(String pattern) {
		RequestResponse result = new RequestResponse();
		result.setPattern(pattern);
		return result;
	}
}

class RequestResponse extends MessageMapping {
	private Map<String, Object> response = new HashMap<>();

	public RequestResponse() {
		handler(input -> input.map(request -> response));
		setFrameType(FrameType.REQUEST_RESPONSE);
	}

	public Map<String, Object> getResponse() {
		return this.response;
	}

}

class FireAndForget extends MessageMapping {
	public FireAndForget() {
		handler(input -> input.thenMany(Flux.empty()));
		setFrameType(FrameType.REQUEST_FNF);
	}
}

class RequestStream extends MessageMapping {

	private int repeat = 1;

	private List<Map<String, Object>> responses = new ArrayList<>();

	public RequestStream() {
		handler(input -> input.flatMap(request -> Flux.fromIterable(getResponses())));
		setFrameType(FrameType.REQUEST_STREAM);
	}

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
}

class RequestChannel extends MessageMapping {

	private int repeat = 1;

	private List<Map<String, Object>> responses = new ArrayList<>();

	public RequestChannel() {
		handler(input -> input.flatMap(request -> Flux.fromIterable(getResponses())));
		setFrameType(FrameType.REQUEST_CHANNEL);
	}

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
}