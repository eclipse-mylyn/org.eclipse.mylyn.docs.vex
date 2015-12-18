/*******************************************************************************
 * Copyright (c) 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.core;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class TextUtilsTest {

	@Test
	public void provideLinesDelimitedWithLF() throws Exception {
		assertEquals(Arrays.asList("line 1", "line 2", "line 3"), Arrays.asList(TextUtils.lines("line 1\nline 2\nline 3")));
	}

	@Test
	public void provideLinesDelimitedWithCR() throws Exception {
		assertEquals(Arrays.asList("line 1", "line 2", "line 3"), Arrays.asList(TextUtils.lines("line 1\rline 2\rline 3")));
	}

	@Test
	public void provideLinesDelimitedWithCRLF() throws Exception {
		assertEquals(Arrays.asList("line 1", "line 2", "line 3"), Arrays.asList(TextUtils.lines("line 1\r\nline 2\r\nline 3")));
	}

	@Test
	public void provideLinesDelimitedWithDifferentLineSeparators() throws Exception {
		assertEquals(Arrays.asList("line 1", "line 2", "line 3", "line 4", "line 5"), Arrays.asList(TextUtils.lines("line 1\rline 2\nline 3\r\nline 4\rline 5")));
	}

	@Test
	public void provideEmptyLines() throws Exception {
		assertEquals(Arrays.asList("", "line 1", "", "line 2", "line 3", ""), Arrays.asList(TextUtils.lines("\nline 1\n\nline 2\nline 3\n")));
	}

	@Test
	public void provideSingleLine() throws Exception {
		assertEquals(Arrays.asList("line 1"), Arrays.asList(TextUtils.lines("line 1")));
	}
}
