/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.samples.junit4;

import static org.junit.Assert.fail;

import org.junit.Test;

public class MalformedJUnit4TestCase {

	@Test
	/* not public */ void nonPublicTest() {
		fail("this should never be called");
	}

}
