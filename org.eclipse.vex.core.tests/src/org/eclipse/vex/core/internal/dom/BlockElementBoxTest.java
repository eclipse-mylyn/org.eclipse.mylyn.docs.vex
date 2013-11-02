/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.io.DocumentReader;
import org.eclipse.vex.core.internal.layout.BlockElementBox;
import org.eclipse.vex.core.internal.layout.Box;
import org.eclipse.vex.core.internal.layout.CssBoxFactory;
import org.eclipse.vex.core.internal.layout.FakeGraphics;
import org.eclipse.vex.core.internal.layout.LayoutContext;
import org.eclipse.vex.core.internal.layout.RootBox;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.tests.TestResources;
import org.junit.Test;

public class BlockElementBoxTest {

	private final Graphics g;
	private final LayoutContext context;

	public BlockElementBoxTest() throws Exception {

		final StyleSheetReader ssReader = new StyleSheetReader();
		final StyleSheet ss = ssReader.read(TestResources.get("test.css"));

		g = new FakeGraphics();

		context = new LayoutContext();
		context.setBoxFactory(new CssBoxFactory());
		context.setGraphics(g);
		context.setStyleSheet(ss);
		context.setWhitespacePolicy(new CssWhitespacePolicy(ss));
	}

	@Test
	public void testPositioning() throws Exception {

		final String docString = "<root><small/><medium/><large/></root>";
		final DocumentReader docReader = new DocumentReader();
		docReader.setDebugging(true);
		final IDocument doc = docReader.read(docString);
		context.setDocument(doc);

		final RootBox parentBox = new RootBox(context, doc, 500);

		final BlockElementBox box = new BlockElementBox(context, parentBox, doc.getRootElement());
		box.setWidth(parentBox.getWidth());

		final List<Box> childrenList = box.createChildren(context);
		final Box[] children = childrenList.toArray(new Box[childrenList.size()]);
		assertNotNull("No Children created.", children);
		assertEquals(3, children.length);
	}

	public int getGap(final BlockElementBox box, final int n) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		final Method getGap = BlockElementBox.class.getDeclaredMethod("getGap", new Class[] { Integer.TYPE });
		getGap.setAccessible(true);
		return ((Integer) getGap.invoke(box, new Object[] { Integer.valueOf(n) })).intValue();
	}
}
