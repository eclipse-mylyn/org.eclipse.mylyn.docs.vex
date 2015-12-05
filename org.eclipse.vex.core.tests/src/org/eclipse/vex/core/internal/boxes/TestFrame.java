/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.boxes;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class TestFrame {

	private StructuralFrame box;

	@Before
	public void setUp() throws Exception {
		box = new StructuralFrame();
	}

	@Test
	public void whenCreatedHasNoMargin() throws Exception {
		assertEquals(Margin.NULL, box.getMargin());
	}

	@Test
	public void marginIsMutable() throws Exception {
		final Margin margin = new Margin(1, 2, 3, 4);
		box.setMargin(margin);
		assertEquals(margin, box.getMargin());
	}

	@Test
	public void whenCreatedHasNoBorder() throws Exception {
		assertEquals(Border.NULL, box.getBorder());
	}

	@Test
	public void borderIsMutable() throws Exception {
		final Border border = new Border(1, 2, 3, 4);
		box.setBorder(border);
		assertEquals(border, box.getBorder());
	}

	@Test
	public void whenCreatedHasNoPadding() throws Exception {
		assertEquals(Padding.NULL, box.getPadding());
	}

	@Test
	public void PaddingIsMutable() throws Exception {
		final Padding padding = new Padding(1, 2, 3, 4);
		box.setPadding(padding);
		assertEquals(padding, box.getPadding());
	}

}
