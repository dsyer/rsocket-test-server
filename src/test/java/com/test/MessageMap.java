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

import io.rsocket.frame.FrameType;

import org.springframework.web.util.pattern.PathPatternRouteMatcher;

/**
 * @author Dave Syer
 *
 */
public class MessageMap {

	private Map<String, Object> response = new HashMap<>();

	private Map<String, Object> request = new HashMap<>();

	private PathPatternRouteMatcher matcher = new PathPatternRouteMatcher();

	private FrameType frameType;

	private String pattern;

	public boolean isRequestResponse() {
		return frameType == FrameType.REQUEST_RESPONSE;
	}

	private boolean matches(Map<String, Object> source, Map<String, Object> target) {
		for (String key : source.keySet()) {
			if (!target.containsKey(key)) {
				return false;
			}
			Object object = target.get(key);
			if (object instanceof String) {
				Object pattern = request.get(key);
				if (pattern instanceof String) {
					if (!matcher.match((String) pattern, matcher.parseRoute(key))) {
						return false;
					}
				}
				else {
					return false;
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
		}
		return true;
	}

	public boolean matches(Map<String, Object> request, String destination) {
		if (!matches(this.request, request)) {
			return false;
		}
		return matcher.match(this.pattern, matcher.parseRoute(destination));
	}

	public Map<String, Object> getResponse() {
		return this.response;
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

}
