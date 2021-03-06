/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

import static org.junit.gen5.api.Assertions.assertAll;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertNotEquals;
import static org.junit.gen5.api.Assertions.assertNotNull;
import static org.junit.gen5.api.Assertions.assertNotSame;
import static org.junit.gen5.api.Assertions.assertNull;
import static org.junit.gen5.api.Assertions.assertSame;
import static org.junit.gen5.api.Assertions.assertThrows;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.expectThrows;
import static org.junit.gen5.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

/**
 * Unit tests for JUnit 5 {@link Assertions}.
 *
 * @since 5.0
 */
public class AssertionsTests {

	// --- fail ----------------------------------------------------------

	@Test
	void failWithString() {
		try {
			fail("test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void failWithMessageSupplier() {
		try {
			fail(() -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void failWithNullString() {
		try {
			fail((String) null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageIsNull(ex);
		}
	}

	@Test
	void failWithNullMessageSupplier() {
		try {
			fail((Supplier<String>) null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageIsNull(ex);
		}
	}

	// --- assertTrue ----------------------------------------------------

	@Test
	void assertTrueWithBooleanTrue() {
		assertTrue(true);
	}

	@Test
	void assertTrueWithBooleanSupplierTrue() {
		assertTrue(() -> true);
	}

	@Test
	void assertTrueWithBooleanTrueAndString() {
		assertTrue(true, "test");
	}

	@Test
	void assertTrueWithBooleanFalse() {
		try {
			assertTrue(false);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageIsNull(ex);
		}
	}

	@Test
	void assertTrueWithBooleanFalseAndString() {
		try {
			assertTrue(false, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void assertTrueWithBooleanSupplierFalseAndString() {
		try {
			assertTrue(() -> false, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void assertTrueWithBooleanSupplierFalseAndMessageSupplier() {
		try {
			assertTrue(() -> false, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	// --- assertFalse ---------------------------------------------------

	@Test
	void assertFalseWithBooleanFalse() {
		assertFalse(false);
	}

	@Test
	void assertFalseWithBooleanSupplierFalse() {
		assertFalse(() -> false);
	}

	@Test
	void assertFalseWithBooleanTrueAndString() {
		try {
			assertFalse(true, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void assertFalseWithBooleanTrueAndMessageSupplier() {
		try {
			assertFalse(true, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void assertFalseWithBooleanSupplierTrueAndString() {
		try {
			assertFalse(() -> true, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void assertFalseWithBooleanSupplierTrueAndMessageSupplier() {
		try {
			assertFalse(() -> true, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	// --- assertNull ----------------------------------------------------

	@Test
	void assertNullWithNull() {
		assertNull(null);
	}

	@Test
	void assertNullWithNonNullObject() {
		try {
			assertNull("foo");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEndsWith(ex, "expected: <null> but was: <foo>");
		}
	}

	@Test
	void assertNullWithNonNullObjectAndMessage() {
		try {
			assertNull("foo", "a message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "a message");
			assertMessageEndsWith(ex, "expected: <null> but was: <foo>");
		}
	}

	@Test
	void assertNullWithNonNullObjectAndMessageSupplier() {
		try {
			assertNull("foo", () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageEndsWith(ex, "expected: <null> but was: <foo>");
		}
	}

	// --- assertNotNull -------------------------------------------------

	@Test
	void assertNotNullWithNonNullObject() {
		assertNotNull("foo");
	}

	@Test
	void assertNotNullWithNull() {
		try {
			assertNotNull(null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: not <null>");
		}
	}

	@Test
	void assertNotNullWithNullAndMessageSupplier() {
		try {
			assertNotNull(null, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageEndsWith(ex, "expected: not <null>");
		}
	}

	// --- assertEquals -------------------------------------------------

	@Test
	void assertEqualsWithTwoNulls() {
		assertEquals(null, null);
	}

	@Test
	void assertEqualsWithSameObject() {
		Object foo = new Object();
		assertEquals(foo, foo);
	}

	@Test
	void assertEqualsWithEquivalentStrings() {
		assertEquals(new String("foo"), new String("foo"));
	}

	@Test
	void assertEqualsWithNullVsObject() {
		try {
			assertEquals(null, "foo");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: <null> but was: <foo>");
		}
	}

	@Test
	void assertEqualsWithObjectVsNull() {
		try {
			assertEquals("foo", null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: <foo> but was: <null>");
		}
	}

	@Test
	void assertEqualsWithNullVsObjectAndMessageSupplier() {
		try {
			assertEquals(null, "foo", () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageEndsWith(ex, "expected: <null> but was: <foo>");
		}
	}

	@Test
	void assertEqualsWithObjectVsNullAndMessageSupplier() {
		try {
			assertEquals("foo", null, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageEndsWith(ex, "expected: <foo> but was: <null>");
		}
	}

	// --- assertNotEquals -------------------------------------------------

	@Test
	void assertNotEqualsWithNullVsObject() {
		assertNotEquals(null, "foo");
	}

	@Test
	void assertNotEqualsWithObjectVsNull() {
		assertNotEquals("foo", null);
	}

	@Test
	void assertNotEqualsWithDifferentObjects() {
		assertNotEquals(new Object(), new Object());
	}

	@Test
	void assertNotEqualsWithEquivalentStringsAndMessage() {
		try {
			assertNotEquals(new String("foo"), new String("foo"), "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageEndsWith(ex, "expected: not equal but was: <foo>");
		}
	}

	@Test
	void assertNotEqualsWithEquivalentStringsAndMessageSupplier() {
		try {
			assertNotEquals(new String("foo"), new String("foo"), () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageEndsWith(ex, "expected: not equal but was: <foo>");
		}
	}

	// --- assertSame ----------------------------------------------------

	@Test
	void assertSameWithTwoNulls() {
		assertSame(null, null);
	}

	@Test
	void assertSameWithSameObject() {
		Object foo = new Object();
		assertSame(foo, foo);
	}

	@Test
	void assertSameWithObjectVsNull() {
		try {
			assertSame(new Object(), null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "expected: <java.lang.Object@");
			assertMessageContains(ex, "but was: <null>");
		}
	}

	@Test
	void assertSameWithNullVsObject() {
		try {
			assertSame(null, new Object());
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "expected: <null>");
			assertMessageContains(ex, "but was: <java.lang.Object@");
		}
	}

	@Test
	void assertSameWithDifferentObjects() {
		try {
			assertSame(new Object(), new Object());
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "expected: <java.lang.Object@");
			assertMessageContains(ex, "but was: <java.lang.Object@");
		}
	}

	@Test
	void assertSameWithEquivalentStringsAndMessageSupplier() {
		try {
			assertSame(new String("foo"), new String("foo"), () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageContains(ex, "expected: java.lang.String@");
			assertMessageContains(ex, "but was: java.lang.String@");
		}
	}

	// --- assertNotSame -------------------------------------------------

	@Test
	void assertNotSameWithDifferentObjects() {
		assertNotSame(new Object(), new Object());
	}

	@Test
	void assertNotSameWithObjectVsNull() {
		assertNotSame(new Object(), null);
	}

	@Test
	void assertNotSameWithNullVsObject() {
		assertNotSame(null, new Object());
	}

	@Test
	void assertNotSameWithTwoNulls() {
		try {
			assertNotSame(null, null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: not same but was: <null>");
		}
	}

	@Test
	void assertNotSameWithSameObjectAndMessage() {
		try {
			Object foo = new Object();
			assertNotSame(foo, foo, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageContains(ex, "expected: not same but was: <java.lang.Object@");
		}
	}

	@Test
	void assertNotSameWithSameObjectAndMessageSupplier() {
		try {
			Object foo = new Object();
			assertNotSame(foo, foo, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageContains(ex, "expected: not same but was: <java.lang.Object@");
		}
	}

	// --- assertAll -----------------------------------------------------

	@Test
	void assertAllWithExecutablesThatDoNotThrowExceptions() {
		// @formatter:off
		assertAll(
			() -> assertTrue(true),
			() -> assertFalse(false),
			() -> assertTrue(true)
		);
		// @formatter:on
	}

	@Test
	void assertAllWithExecutableThatThrowsAssertionError() {
		MultipleFailuresError multipleFailuresError = expectThrows(MultipleFailuresError.class,
			() -> assertAll(() -> assertFalse(true)));
		assertTrue(multipleFailuresError != null);
		List<AssertionError> failures = multipleFailuresError.getFailures();
		assertTrue(failures.size() == 1);
		assertTrue(failures.get(0).getClass().equals(AssertionFailedError.class));
	}

	@Test
	void assertAllWithExecutableThatThrowsThrowable() {
		assertThrows(EnigmaThrowable.class, () -> assertAll(() -> {
			throw new EnigmaThrowable();
		}));
	}

	@Test
	void assertAllWithExecutableThatThrowsCheckedException() {
		assertThrows(IOException.class, () -> assertAll(() -> {
			throw new IOException();
		}));
	}

	@Test
	void assertAllWithExecutableThatThrowsRuntimeException() {
		assertThrows(IllegalStateException.class, () -> assertAll(() -> {
			throw new IllegalStateException();
		}));
	}

	@Test
	void assertAllWithExecutableThatThrowsError() {
		assertThrows(StackOverflowError.class, () -> assertAll(this::recurseIndefinitely));
	}

	// --- assertThrows --------------------------------------------------

	@Test
	void assertThrowsThrowable() {
		assertThrows(EnigmaThrowable.class, () -> {
			throw new EnigmaThrowable();
		});
	}

	@Test
	void assertThrowsCheckedException() {
		assertThrows(IOException.class, () -> {
			throw new IOException();
		});
	}

	@Test
	void assertThrowsRuntimeException() {
		assertThrows(IllegalStateException.class, () -> {
			throw new IllegalStateException();
		});
	}

	@Test
	void assertThrowsError() {
		assertThrows(StackOverflowError.class, this::recurseIndefinitely);
	}

	// --- expectThrows --------------------------------------------------

	@Test
	void expectThrowsThrowable() {
		EnigmaThrowable enigmaThrowable = expectThrows(EnigmaThrowable.class, () -> {
			throw new EnigmaThrowable();
		});
		assertNotNull(enigmaThrowable);
	}

	@Test
	void expectThrowsCheckedException() {
		IOException exception = expectThrows(IOException.class, () -> {
			throw new IOException();
		});
		assertNotNull(exception);
	}

	@Test
	void expectThrowsRuntimeException() {
		IllegalStateException illegalStateException = expectThrows(IllegalStateException.class, () -> {
			throw new IllegalStateException();
		});
		assertNotNull(illegalStateException);
	}

	@Test
	void expectThrowsError() {
		StackOverflowError stackOverflowError = expectThrows(StackOverflowError.class, this::recurseIndefinitely);
		assertNotNull(stackOverflowError);
	}

	@Test
	void expectThrowsWithExecutableThatDoesNotThrowAnException() {
		try {
			expectThrows(IllegalStateException.class, () -> {
			});
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "Expected java.lang.IllegalStateException to be thrown");
		}
	}

	@Test
	void expectThrowsWithExecutableThatThrowsAnUnexpectedException() {
		try {
			expectThrows(IllegalStateException.class, () -> {
				throw new NumberFormatException();
			});
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "Unexpected exception type thrown");
			assertMessageContains(ex, "expected: <java.lang.IllegalStateException>");
			assertMessageContains(ex, "but was: <java.lang.NumberFormatException>");
		}
	}

	// -------------------------------------------------------------------

	private void recurseIndefinitely() {
		// simulate infinite recursion
		throw new StackOverflowError();
	}

	private static void expectAssertionFailedError() {
		throw new AssertionError("Should have thrown an " + AssertionFailedError.class.getName());
	}

	private static void assertMessageIsNull(AssertionFailedError ex) throws AssertionError {
		if (ex.getMessage() != null) {
			throw new AssertionError("Message in AssertionFailedError should be null");
		}
	}

	private static void assertMessageEquals(AssertionFailedError ex, String msg) throws AssertionError {
		if (!msg.equals(ex.getMessage())) {
			throw new AssertionError(
				"Message in AssertionFailedError should be [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

	private static void assertMessageEndsWith(AssertionFailedError ex, String msg) throws AssertionError {
		if (!ex.getMessage().endsWith(msg)) {
			throw new AssertionError(
				"Message in AssertionFailedError should end with [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

	private static void assertMessageStartsWith(AssertionFailedError ex, String msg) throws AssertionError {
		if (!ex.getMessage().startsWith(msg)) {
			throw new AssertionError(
				"Message in AssertionFailedError should start with [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

	private static void assertMessageContains(AssertionFailedError ex, String msg) throws AssertionError {
		if (!ex.getMessage().contains(msg)) {
			throw new AssertionError(
				"Message in AssertionFailedError should contain [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

	@SuppressWarnings("serial")
	private static class EnigmaThrowable extends Throwable {
	}

}
