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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.rsocket.context.RSocketServerBootstrap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ClassUtils;

/**
 * @author Dave Syer
 *
 */
public class RSocketServerExtension
		implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

	public static String PROPERTY_NAME = "test.rsocket.server.port";

	private static final int MAX_COUNT = 180;

	private ConfigurableApplicationContext application;

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		if (application != null && application.isRunning()) {
			application.close();
		}
		application = null;
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		application = RSocketServerExtension.run();
		RSocketServerBootstrap server = application.getBean(RSocketServerBootstrap.class);
		int count = 0;
		while (!server.isRunning() && count++ < MAX_COUNT) {
			try {
				Thread.sleep(20);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		if (count >= MAX_COUNT) {
			throw new TimeoutException("Timed out waiting for RSocket server to start");
		}
		setPortProperty(SpringExtension.getApplicationContext(context),
				application.getEnvironment().getProperty("local.rsocket.server.port",
						Integer.class));
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext,
			ExtensionContext extensionContext) throws ParameterResolutionException {
		return RSocketMessageCatalog.class
				.isAssignableFrom(parameterContext.getParameter().getType());
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext,
			ExtensionContext extensionContext) {
		return getRSocketMessageCatalog(extensionContext);
	}

	private RSocketMessageRegistry getRSocketMessageCatalog(ExtensionContext context) {
		return application != null ? application.getBean(RSocketMessageRegistry.class)
				: null;
	}

	private void setPortProperty(ApplicationContext context, int port) {
		if (context instanceof ConfigurableApplicationContext) {
			setPortProperty(((ConfigurableApplicationContext) context).getEnvironment(),
					port);
		}
		if (context.getParent() != null) {
			setPortProperty(context.getParent(), port);
		}
	}

	private void setPortProperty(ConfigurableEnvironment environment, int port) {
		MutablePropertySources sources = environment.getPropertySources();
		PropertySource<?> source = sources.get("server.ports");
		if (source == null) {
			source = new MapPropertySource("server.ports", new HashMap<>());
			sources.addFirst(source);
		}
		setPortProperty(port, source);
	}

	@SuppressWarnings("unchecked")
	private void setPortProperty(int port, PropertySource<?> source) {
		((Map<String, Object>) source.getSource()).put(PROPERTY_NAME, port);
	}

	public static ConfigurableApplicationContext run(String... args) {
		// Break cycle using reflection
		Class<?> mainClass = ClassUtils.resolveClassName(
				"org.springframework.mock.rsocket.server.RSocketServerApplication", null);
		return new SpringApplicationBuilder(mainClass).properties(
				"spring.rsocket.server.port=0", "spring.main.web-application-type=none",
				"spring.main.banner-mode=off").run(args);
	}
}
