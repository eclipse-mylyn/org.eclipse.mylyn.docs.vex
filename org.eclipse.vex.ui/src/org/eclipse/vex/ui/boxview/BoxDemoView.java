/*******************************************************************************
 * Copyright (c) 2014, 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.boxview;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.io.UniversalTestDocument;
import org.eclipse.vex.core.internal.visualization.CSSBasedBoxModelBuilder;
import org.eclipse.vex.core.internal.widget.DOMController;
import org.eclipse.vex.core.internal.widget.swt.BoxWidget;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;

/**
 * This is a viewer for the new box model - just to do visual experiments.
 *
 * @author Florian Thienel
 */
public class BoxDemoView extends ViewPart {

	private static final int SAMPLE_COUNT = 25;

	private Composite parent;
	private BoxWidget boxWidget;

	@Override
	public void createPartControl(final Composite parent) {
		this.parent = parent;
		refresh();
	}

	@Override
	public void dispose() {
		super.dispose();
		boxWidget = null;
	}

	@Override
	public void setFocus() {
		boxWidget.setFocus();
	}

	public void refresh() {
		if (boxWidget != null) {
			boxWidget.dispose();
			boxWidget = null;
			cleanStaleReferenceInShell();
		}
		boxWidget = new BoxWidget(parent, SWT.V_SCROLL);

		boxWidget.setContent(UniversalTestDocument.createTestDocumentWithInlineElements(SAMPLE_COUNT));
		boxWidget.setBoxModelBuilder(new CSSBasedBoxModelBuilder(readStyleSheet()));
		parent.layout();
	}

	private void cleanStaleReferenceInShell() {
		/*
		 * Shell keeps a reference to the boxWidget in Shell.savedFocus. parent.setFocus() forces Shell to store a
		 * reference to parent instead.
		 */
		parent.setFocus();
	}

	private StyleSheet readStyleSheet() {
		try {
			final InputSource inputSource = new InputSource(new InputStreamReader(getClass().getResourceAsStream("box-demo.css"), "utf-8"));
			return new StyleSheetReader().read(inputSource, null);
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (final CSSException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void insertBold() {
		final DOMController controller = boxWidget.getDOMController();
		controller.insertElement(new QualifiedName(null, "b"));
	}

	public void insertItalic() {
		final DOMController controller = boxWidget.getDOMController();
		controller.insertElement(new QualifiedName(null, "i"));
	}

}
