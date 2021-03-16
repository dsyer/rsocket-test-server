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

import java.util.List;
import java.util.Map;

import io.rsocket.frame.FrameType;
import reactor.core.publisher.Flux;

import org.springframework.mock.rsocket.MessageMappingSpec.ChannelBuilder;
import org.springframework.mock.rsocket.MessageMappingSpec.ResponseBuilder;
import org.springframework.mock.rsocket.MessageMappingSpec.StreamBuilder;

/**
 * A mapping from request to a response. The mapping can have any of the standard RSocket
 * frame types, and only a request with that framing will match. Matching is also applied
 * against a destination (route) pattern, and also optionally on the incoming request
 * body, matching field by field. For many applications it will suffice to match only on
 * frame type and destination.
 * 
 * @author Dave Syer
 *
 */
public interface MessageMapping {

	/**
	 * Special form of {@link MessageMapping#drain(Class)} where the requests are left as
	 * typeless map, reflecting the underlying JSON.
	 */
	List<Map<String, Object>> drain();

	/**
	 * Inspect the requests that the server has so far received, converting to the
	 * specified type. Calling this method resets the state so calling it again before any
	 * requests are sent will return an empty list.
	 */
	<I> List<I> drain(Class<I> type);

	/**
	 * Query if this mapping matches a specific incoming request and destination.
	 */
	boolean matches(Map<String, Object> request, String destination);

	/**
	 * The frame type that will match this mapping.
	 */
	FrameType getFrameType();

	/**
	 * A rule for processing the incoming request. The type of request and result depends
	 * on the frame type. For fire and forget the response is empty, for request-response
	 * the response is single valued. For "request-channel" the input and output are both
	 * in principle multiple valued.
	 */
	Flux<Map<String, Object>> handle(Flux<Map<String, Object>> input);

	/**
	 * A pattern that matches the destination (route) of the incoming request. May contain
	 * wildcards.
	 */
	String getPattern();

	/**
	 * Factory method for a mapping with frame type REQUEST_FNF (fire and forget).
	 */
	public static MessageMapping forget(String pattern) {
		FireAndForget result = new FireAndForget();
		result.setPattern(pattern);
		return result;
	}

	/**
	 * Factory method for a mapping with frame type REQUEST_CHANNEL (2-way stream). To
	 * complete the mapping you must supply a handler, mapping requests to responses, or
	 * response prototype if the response doesn't depend on the request content.
	 */
	public static <I, O> ChannelBuilder<I, O> channel(String pattern) {
		return new ChannelBuilder<I, O>(pattern);
	}

	/**
	 * Factory method for a mapping with frame type REQUEST_STREAM, where the response is
	 * a stream. To complete the mapping you must supply a handler or response prototype
	 * (optionally multiple valued).
	 */
	public static <I, O> StreamBuilder<I, O> stream(String pattern) {
		return new StreamBuilder<I, O>(pattern);
	}

	/**
	 * Factory method for a mapping with frame type REQUEST_RESPONSE (single-valued
	 * response). To complete the mapping you must supply a handler or response prototype.
	 */
	public static <I, O> ResponseBuilder<I, O> response(String pattern) {
		return new ResponseBuilder<I, O>(pattern);
	}

}
