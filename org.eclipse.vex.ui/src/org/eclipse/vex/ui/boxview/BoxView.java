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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.vex.core.internal.boxes.Border;
import org.eclipse.vex.core.internal.boxes.Frame;
import org.eclipse.vex.core.internal.boxes.HorizontalBar;
import org.eclipse.vex.core.internal.boxes.IChildBox;
import org.eclipse.vex.core.internal.boxes.Margin;
import org.eclipse.vex.core.internal.boxes.Padding;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.Square;
import org.eclipse.vex.core.internal.boxes.StaticText;
import org.eclipse.vex.core.internal.boxes.VerticalBlock;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.widget.swt.BoxWidget;

/**
 * This is a viewer for the new box model - just to do visual experiments.
 *
 * @author Florian Thienel
 */
public class BoxView extends ViewPart {

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
		final RootBox rootBox = new RootBox();

		for (int i = 0; i < 10000; i += 1) {
			final VerticalBlock mixedBlock = new VerticalBlock();
			mixedBlock.appendChild(createHorizontalBar());
			mixedBlock.appendChild(createMixedParagraph(i));
			mixedBlock.appendChild(createHorizontalBar());
			rootBox.appendChild(createFrame(mixedBlock));

			final VerticalBlock textBlock = new VerticalBlock();
			textBlock.appendChild(createHorizontalBar());
			textBlock.appendChild(createTextParagraph());
			textBlock.appendChild(createHorizontalBar());
			rootBox.appendChild(createFrame(textBlock));
		}

		return rootBox;
	}

	private Frame createFrame(final IChildBox component) {
		final Frame frame = new Frame();
		frame.setMargin(new Margin(10, 20, 30, 40));
		frame.setBorder(new Border(10));
		frame.setPadding(new Padding(15, 25, 35, 45));
		frame.setComponent(component);
		return frame;
	}

	private HorizontalBar createHorizontalBar() {
		final HorizontalBar horizontalBar = new HorizontalBar();
		horizontalBar.setHeight(2);
		horizontalBar.setColor(Color.BLACK);
		return horizontalBar;
	}

	private Paragraph createMixedParagraph(final int i) {
		final Paragraph paragraph = new Paragraph();
		for (int j = 0; j < 20; j += 1) {
			final StaticText text = new StaticText();
			text.setText("Lorem ipsum " + i + " ");
			text.setFont(new FontSpec(new String[] { "Arial" }, 0, 10.0f + j));
			paragraph.appendChild(text);
			final Square square = new Square();
			square.setSize(5 + j);
			paragraph.appendChild(square);
		}
		return paragraph;
	}

	private Paragraph createTextParagraph() {
		final Paragraph paragraph = new Paragraph();
		final StaticText text = new StaticText();
		text.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec a diam lectus. Sed sit amet ipsum mauris. Maecenas congue ligula ac quam viverra nec consectetur ante hendrerit. Donec et mollis dolor. Praesent et diam eget libero egestas mattis sit amet vitae augue. Nam tincidunt congue enim, ut porta lorem lacinia consectetur.");
		text.setFont(new FontSpec(new String[] { "Times New Roman" }, 0, 20.0f));
		paragraph.appendChild(text);
		return paragraph;
	}
}
