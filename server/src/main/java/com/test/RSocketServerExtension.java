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

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import reactor.core.publisher.Hooks;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Dave Syer
 *
 */
public class RSocketServerExtension implements BeforeAllCallback, AfterAllCallback {

	private ConfigurableApplicationContext application;

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		if (application != null && application.isRunning()) {
			application.close();
		}
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		Hooks.onOperatorDebug();
		application = TestApplication.run();
		try {
			// TODO: wait for server to start
			Thread.sleep(2000);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
