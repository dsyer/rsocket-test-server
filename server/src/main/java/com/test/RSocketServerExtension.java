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

import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.springframework.boot.rsocket.context.RSocketServerBootstrap;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Dave Syer
 *
 */
public class RSocketServerExtension implements BeforeAllCallback, AfterAllCallback {

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
		application = TestApplication.run();
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
	}

}
