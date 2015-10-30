/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import java.lang.reflect.InvocationTargetException;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
class MethodTestExecutionNode extends TestExecutionNode {

	private final MethodTestDescriptor testDescriptor;

	MethodTestExecutionNode(MethodTestDescriptor testDescriptor) {
		this.testDescriptor = testDescriptor;
	}

	@Override
	public MethodTestDescriptor getTestDescriptor() {
		return this.testDescriptor;
	}

	@Override
	public void execute(EngineExecutionContext context) {
		try {
			context.getTestExecutionListener().testStarted(getTestDescriptor());
			Object testInstance = context.getAttributes().get(ClassTestExecutionNode.TEST_INSTANCE_ATTRIBUTE_NAME);
			ReflectionUtils.invokeMethod(getTestDescriptor().getTestMethod(), testInstance);
			context.getTestExecutionListener().testSucceeded(getTestDescriptor());
		}
		catch (InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();
			if (targetException instanceof TestSkippedException) {
				context.getTestExecutionListener().testSkipped(getTestDescriptor(), targetException);
			}
			else if (targetException instanceof TestAbortedException) {
				context.getTestExecutionListener().testAborted(getTestDescriptor(), targetException);
			}
			else {
				context.getTestExecutionListener().testFailed(getTestDescriptor(), targetException);
			}
		}
		catch (Exception ex) {
			context.getTestExecutionListener().testFailed(getTestDescriptor(), ex);
		}
	}

}