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

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotatedMethods;
import static org.junit.gen5.commons.util.ReflectionUtils.invokeMethod;
import static org.junit.gen5.commons.util.ReflectionUtils.newInstance;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Condition.Result;
import org.junit.gen5.api.Executable;
import org.junit.gen5.api.TestInstance;
import org.junit.gen5.api.TestInstance.Lifecycle;
import org.junit.gen5.api.extension.AfterAllCallbacks;
import org.junit.gen5.api.extension.AfterEachCallbacks;
import org.junit.gen5.api.extension.BeforeAllCallbacks;
import org.junit.gen5.api.extension.BeforeEachCallbacks;
import org.junit.gen5.api.extension.InstancePostProcessor;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
class ClassExecutionNode extends TestExecutionNode {

	private final ClassTestDescriptor testDescriptor;

	ClassExecutionNode(ClassTestDescriptor testDescriptor) {
		this.testDescriptor = testDescriptor;
	}

	@Override
	ClassTestDescriptor getTestDescriptor() {
		return this.testDescriptor;
	}

	@Override
	void execute(ExecutionRequest request, TestExecutionContext context) {
		if (isTestDisabled(request, context)) {
			// Abort execution of the test completely at this point.
			return;
		}

		boolean instancePerClass = isInstancePerClassMode(context.getTestClass().get());
		if (instancePerClass) {
			createTestInstanceAndUpdateContext(context);
		}

		try {
			executeBeforeAllMethods(context);
			for (TestExecutionNode child : getChildren()) {
				if (!instancePerClass) {
					createTestInstanceAndUpdateContext(context);
				}
				executeChild(child, request, context, context.getTestInstance().orElse(null));
			}
		}
		catch (Exception e) {
			request.getTestExecutionListener().testFailed(getTestDescriptor(), e);
		}
		finally {
			if (!instancePerClass) {
				((DescriptorBasedTestExecutionContext) context).setTestInstance(null);
			}
			executeAfterAllMethods(request, context);
		}
	}

	@Override
	protected String buildTestSkippedMessage(Result result, TestExecutionContext context) {
		return String.format("Skipped test class [%s]; reason: %s", context.getTestClass().get().getName(),
			result.getReason().orElse("unknown"));
	}

	private boolean isInstancePerClassMode(Class<?> testClass) {
		// @formatter:off
		return AnnotationUtils.findAnnotation(testClass, TestInstance.class)
				.map(TestInstance::value)
				.map(lifecycle -> (lifecycle == Lifecycle.PER_CLASS))
				.orElse(false);
		// @formatter:on
	}

	private void executeBeforeAllMethods(TestExecutionContext context) throws Exception {
		Class<?> testClass = context.getTestClass().get();
		Object testInstance = context.getTestInstance().orElse(null);

		List<BeforeAllCallbacks> callbacks = context.getExtensions(BeforeAllCallbacks.class).collect(toList());

		callbacks.stream().forEachOrdered(callback -> callback.preBeforeAll(context));

		Class<BeforeAll> annotationType = BeforeAll.class;
		for (Method method : findAnnotatedMethods(testClass, annotationType, MethodSortOrder.HierarchyDown)) {
			validateBeforeAllOrAfterAllMethod(annotationType, method, testInstance);
			invokeMethod(method, testInstance);
		}

		callbacks.stream().forEachOrdered(callback -> callback.postBeforeAll(context));
	}

	private void executeAfterAllMethods(ExecutionRequest request, TestExecutionContext context) {
		Class<?> testClass = context.getTestClass().get();
		Object testInstance = context.getTestInstance().orElse(null);

		Class<AfterAll> annotationType = AfterAll.class;
		Throwable exception = null;

		List<AfterAllCallbacks> callbacks = context.getExtensions(AfterAllCallbacks.class).collect(toList());

		// Execute "afters" in reverse order.
		Collections.reverse(callbacks);

		for (AfterAllCallbacks callback : callbacks) {
			exception = executeAndAggregateExceptions(exception, () -> callback.preAfterAll(context));
		}

		for (Method method : findAnnotatedMethods(testClass, annotationType, MethodSortOrder.HierarchyUp)) {
			exception = executeAndAggregateExceptions(exception, () -> {
				validateBeforeAllOrAfterAllMethod(annotationType, method, testInstance);
				invokeMethod(method, testInstance);
			});
		}

		for (AfterAllCallbacks callback : callbacks) {
			exception = executeAndAggregateExceptions(exception, () -> callback.postAfterAll(context));
		}

		if (exception != null) {
			request.getTestExecutionListener().testFailed(getTestDescriptor(), exception);
		}
	}

	/**
	 * Execute the supplied {@link Executable} and aggregate any exception
	 * thrown by the executable.
	 *
	 * <p>If the supplied {@code exception} is {@code null}, this method
	 * will return any exception thrown by the executable. Otherwise, this
	 * method will {@linkplain Throwable#addSuppressed suppress} any thrown
	 * exception and return the supplied exception.
	 */
	Throwable executeAndAggregateExceptions(Throwable exception, Executable executable) {
		try {
			executable.execute();
		}
		catch (Throwable ex) {
			if (exception == null) {
				exception = ex;
			}
			else {
				exception.addSuppressed(ex);
			}
		}
		return exception;
	}

	private void validateBeforeAllOrAfterAllMethod(Class<? extends Annotation> annotationType, Method method,
			Object target) {
		if (target == null && !Modifier.isStatic(method.getModifiers())) {
			throw new IllegalStateException(String.format(
				"Failed to invoke @%s method [%s]. Either declare it as static or annotate the test class with @TestInstance(PER_CLASS).",
				annotationType.getSimpleName(), method.toGenericString()));
		}
	}

	/**
	 * Create the test instance, store it in the supplied context, and
	 * then {@link #postProcessTestInstance post process} it.
	 */
	protected void createTestInstanceAndUpdateContext(TestExecutionContext context) {
		final Class<?> testClass = getTestDescriptor().getTestClass();
		try {
			Object testInstance = newInstance(testClass);
			((DescriptorBasedTestExecutionContext) context).setTestInstance(testInstance);
		}
		catch (Exception ex) {
			String message = String.format(
				"Failed to create test instance of type [%s] for test descriptor with unique ID [%s]",
				testClass.getName(), getTestDescriptor().getUniqueId());
			throw new IllegalStateException(message, ex);
		}
		postProcessTestInstance(context);
	}

	protected void postProcessTestInstance(TestExecutionContext context) {
		try {
			// @formatter:off
			context.getExtensions(InstancePostProcessor.class)
					.forEach(postProcessor -> postProcessor.postProcessTestInstance(context));
			// @formatter:on
		}
		catch (Exception ex) {
			String message = String.format(
				"Failed to post-process test instance of type [%s] for test descriptor with unique ID [%s]",
				context.getTestClass().get().getName(), getTestDescriptor().getUniqueId());
			throw new IllegalStateException(message, ex);
		}
	}

	@Override
	void executeBeforeEachTest(TestExecutionContext methodContext, TestExecutionContext resolutionContext,
			Object testInstance) {

		List<BeforeEachCallbacks> callbacks = resolutionContext.getExtensions(BeforeEachCallbacks.class).collect(
			toList());

		callbacks.stream().forEachOrdered(callback -> callback.preBeforeEach(methodContext));

		for (Method method : getBeforeEachMethods()) {
			invokeMethodInContext(method, methodContext, resolutionContext, testInstance);
		}

		callbacks.stream().forEachOrdered(callback -> callback.postBeforeEach(methodContext));
	}

	protected List<Method> getBeforeEachMethods() {
		return findAnnotatedMethods(getTestDescriptor().getTestClass(), BeforeEach.class,
			MethodSortOrder.HierarchyDown);
	}

	@Override
	void executeAfterEachTest(TestExecutionContext methodContext, TestExecutionContext resolutionContext,
			Object testInstance, List<Throwable> exceptionCollector) {

		List<AfterEachCallbacks> callbacks = resolutionContext.getExtensions(AfterEachCallbacks.class).collect(
			toList());

		// Execute "afters" in reverse order.
		Collections.reverse(callbacks);

		callbacks.stream().forEachOrdered(callback -> callback.preAfterEach(methodContext));

		for (Method method : getAfterEachMethods()) {
			invokeMethodInContextWithAggregatingExceptions(method, methodContext, resolutionContext, testInstance,
				exceptionCollector);
		}

		callbacks.stream().forEachOrdered(callback -> callback.postAfterEach(methodContext));
	}

	protected List<Method> getAfterEachMethods() {
		return findAnnotatedMethods(getTestDescriptor().getTestClass(), AfterEach.class, MethodSortOrder.HierarchyUp);
	}

}