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
package org.eclipse.vex.ui.boxview;

import static org.eclipse.vex.core.internal.boxes.BoxFactory.frame;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.paragraph;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.rootBox;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.square;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.staticText;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.verticalBlock;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.vex.core.internal.boxes.Border;
import org.eclipse.vex.core.internal.boxes.BoxFactory;
import org.eclipse.vex.core.internal.boxes.HorizontalBar;
import org.eclipse.vex.core.internal.boxes.Margin;
import org.eclipse.vex.core.internal.boxes.Padding;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.widget.swt.BoxWidget;

/**
 * This is a viewer for the new box model - just to do visual experiments.
 *
 * @author Florian Thienel
 */
public class BoxView extends ViewPart {

	private static final FontSpec TIMES_NEW_ROMAN = new FontSpec(new String[] { "Times New Roman" }, 0, 20.0f);

	private static final String LOREM_IPSUM_LONG = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec a diam lectus. Sed sit amet ipsum mauris. Maecenas congue ligula ac quam viverra nec consectetur ante hendrerit. Donec et mollis dolor. Praesent et diam eget libero egestas mattis sit amet vitae augue. Nam tincidunt congue enim, ut porta lorem lacinia consectetur.";

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
		boxWidget.setContent(createTestModel());
		parent.layout();
	}

	private void cleanStaleReferenceInShell() {
		/*
		 * Shell keeps a reference to the boxWidget in Shell.savedFocus. parent.setFocus() forces Shell to store a
		 * reference to parent instead.
		 */
		parent.setFocus();
	}

	private RootBox createTestModel() {
		final RootBox rootBox = rootBox();

		for (int i = 0; i < 10000; i += 1) {
			rootBox.appendChild(frame(verticalBlock(horizontalBar(), mixedParagraph(i), horizontalBar()), new Margin(10), new Border(5), new Padding(10, 20, 30, 40)));
			rootBox.appendChild(frame(verticalBlock(horizontalBar(), textParagraph(), horizontalBar()), new Margin(50), Border.NULL, Padding.NULL));
		}

		return rootBox;
	}

	private HorizontalBar horizontalBar() {
		return BoxFactory.horizontalBar(2, Color.BLACK);
	}

	private Paragraph mixedParagraph(final int i) {
		final Paragraph paragraph = new Paragraph();
		for (int j = 0; j < 20; j += 1) {
			paragraph.appendChild(staticText("Lorem ipsum " + i + " ", new FontSpec(new String[] { "Arial" }, 0, 10.0f + j)));
			paragraph.appendChild(square(5 + j));
		}
		return paragraph;
	}

	private Paragraph textParagraph() {
		return paragraph(staticText(LOREM_IPSUM_LONG, TIMES_NEW_ROMAN));
	}
}
