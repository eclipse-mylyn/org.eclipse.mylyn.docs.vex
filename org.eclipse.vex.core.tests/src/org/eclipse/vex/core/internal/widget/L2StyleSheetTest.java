/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import static org.eclipse.vex.core.internal.widget.VexWidgetTest.PARA;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.TITLE;
import static org.eclipse.vex.core.internal.widget.VexWidgetTest.createDocumentWithDTD;
import static org.eclipse.vex.core.tests.TestResources.TEST_DTD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.tests.TestResources;
import org.junit.Before;
import org.junit.Test;

public class L2StyleSheetTest {
	private IVexWidget widget;

	@Before
	public void setUp() throws Exception {
		widget = new BaseVexWidget(new MockHostComponent());

		widget.setDocument(createDocumentWithDTD(TEST_DTD, "section"), readTestStyleSheet());
		widget.setWhitespacePolicy(new CssWhitespacePolicy(widget.getStyleSheet()));
	}

	private static StyleSheet readTestStyleSheet() throws IOException {
		return new StyleSheetReader().read(TestResources.get("test.css"));
	}

	@Test
	public void deletingElement_shouldRemoveElementFromCache() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement element = widget.insertElement(PARA);
		final Styles styles = widget.getStyleSheet().getStyles(element);

		assertNotNull("Styles for inserted element", styles);

		widget.moveTo(element.getStartPosition());
		widget.moveTo(element.getEndPosition(), true);
		widget.deleteSelection();

		assertFalse("Styles for deleted element should be removed from cache.", widget.getStyleSheet().testGetStylesCache().containsKey(element));
	}

	@Test
	public void insertingElement_shouldRecomputeStyles() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement element = widget.insertElement(PARA);
		final Styles styles = widget.getStyleSheet().getStyles(element);
		assertNotNull("Styles for inserted element", styles);
		assertEquals("Background color from <para> style", new Color(255, 255, 255), styles.getBackgroundColor());

		// Insert a new PARA before the existing one
		widget.moveTo(element.getStartPosition());
		final IElement newElement = widget.insertElement(PARA);
		final Styles newStyles = widget.getStyleSheet().getStyles(element);
		assertNotNull("New styles for element", styles);
		assertEquals("Background color from <para + para> style", new Color(255, 0, 0), newStyles.getBackgroundColor());
		final Styles newElementStyles = widget.getStyleSheet().getStyles(newElement);
		assertNotNull("Styles for new element", newElementStyles);
		assertEquals("Background color from <para> style", new Color(255, 255, 255), newElementStyles.getBackgroundColor());
	}

	@Test
	public void deletingElement_shouldRecomputeStyles() throws Exception {
		widget.insertElement(TITLE);
		widget.moveBy(1);
		final IElement element1 = widget.insertElement(PARA);
		widget.moveBy(1);
		final IElement element2 = widget.insertElement(PARA);
		final Styles styles = widget.getStyleSheet().getStyles(element2);
		assertNotNull("Styles for element", styles);
		assertEquals("Background color from <para + para> style", new Color(255, 0, 0), styles.getBackgroundColor());

		// Delete the first PARA
		widget.moveTo(element1.getStartPosition());
		widget.moveTo(element1.getEndPosition(), true);
		widget.deleteSelection();

		final Styles newStyles = widget.getStyleSheet().getStyles(element2);
		assertNotNull("New styles for element", newStyles);
		assertEquals("Background color from <para> style after deletion", new Color(255, 255, 255), newStyles.getBackgroundColor());
	}
}
