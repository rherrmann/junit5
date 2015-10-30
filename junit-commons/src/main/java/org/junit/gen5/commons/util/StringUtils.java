/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public final class StringUtils {

	private StringUtils() {
		/* no-op */
	}

	public static boolean isEmpty(CharSequence charSequence) {
		return (charSequence == null || charSequence.length() == 0);
	}

	public static boolean isBlank(String str) {
		return (str == null || str.trim().length() == 0);
	}

}