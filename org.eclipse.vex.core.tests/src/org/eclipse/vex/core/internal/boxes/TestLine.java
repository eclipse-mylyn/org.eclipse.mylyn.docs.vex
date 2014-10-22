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

import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class TestLine {

	@Test
	public void whenAppendingChild_shouldAddUpChildrensWidth() throws Exception {
		final Square square1 = new Square();
		square1.setWidth(10);
		final Square square2 = new Square();
		square2.setWidth(13);
		final Line line = new Line();

		line.appendChild(square1);
		assertEquals(10, line.getWidth());

		line.appendChild(square2);
		assertEquals(23, line.getWidth());
	}

	@Test
	public void whenPrependingChild_shouldAddUpChildrensWidth() throws Exception {
		final Square square1 = new Square();
		square1.setWidth(10);
		final Square square2 = new Square();
		square2.setWidth(13);
		final Line line = new Line();

		line.prependChild(square1);
		assertEquals(10, line.getWidth());

		line.prependChild(square2);
		assertEquals(23, line.getWidth());
	}

}
