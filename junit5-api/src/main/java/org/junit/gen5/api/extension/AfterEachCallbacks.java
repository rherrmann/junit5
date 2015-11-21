/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

/**
 * {@code AfterEachCallbacks} defines the API for {@link TestExtension
 * TestExtensions} that wish to provide additional behavior to tests
 * {@linkplain #preAfterEach before} all {@code @AfterEach} methods have
 * been invoked <em>or</em> {@linkplain #postAfterEach after} all
 * {@code @AfterEach} methods have been invoked.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @author Sam Brannen
 * @since 5.0
 * @see org.junit.gen5.api.AfterEach
 */
public interface AfterEachCallbacks extends TestExtension {

	/**
	 * Callback that is invoked <em>before</em> all {@code @AfterEach}
	 * methods have been invoked.
	 *
	 * @param testExecutionContext the current test execution context
	 */
	default void preAfterEach(TestExecutionContext testExecutionContext) {
		/* no-op */
	}

	/**
	 * Callback that is invoked <em>after</em> all {@code @AfterEach}
	 * methods have been invoked.
	 *
	 * @param testExecutionContext the current test execution context
	 */
	default void postAfterEach(TestExecutionContext testExecutionContext) {
		/* no-op */
	}

}