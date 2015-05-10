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

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.INode;

/**
 * This factory allows the conventient creation of box structures using nested method calls to factory methods.
 *
 * @author Florian Thienel
 */
public class BoxFactory {

	public static RootBox rootBox(final IStructuralBox... children) {
		final RootBox rootBox = new RootBox();
		for (final IStructuralBox child : children) {
			rootBox.appendChild(child);
		}
		return rootBox;
	}

	public static VerticalBlock verticalBlock(final IStructuralBox... children) {
		final VerticalBlock verticalBlock = new VerticalBlock();
		for (final IStructuralBox child : children) {
			verticalBlock.appendChild(child);
		}
		return verticalBlock;
	}

	public static Frame frame(final IStructuralBox component) {
		final Frame frame = new Frame();
		frame.setComponent(component);
		return frame;
	}

	public static Frame frame(final IStructuralBox component, final Margin margin, final Border border, final Padding padding) {
		final Frame frame = new Frame();
		frame.setComponent(component);
		frame.setMargin(margin);
		frame.setBorder(border);
		frame.setPadding(padding);
		return frame;
	}

	public static NodeReference nodeReference(final INode node) {
		final NodeReference nodeReference = new NodeReference();
		nodeReference.setNode(node);
		return nodeReference;
	}

	public static NodeReference nodeReference(final INode node, final IStructuralBox component) {
		final NodeReference nodeReference = new NodeReference();
		nodeReference.setNode(node);
		nodeReference.setComponent(component);
		return nodeReference;
	}

	public static NodeReference nodeReferenceWithText(final INode node) {
		final NodeReference nodeReference = new NodeReference();
		nodeReference.setNode(node);
		nodeReference.setCanContainText(true);
		return nodeReference;
	}

	public static NodeReference nodeReferenceWithText(final INode node, final IStructuralBox component) {
		final NodeReference nodeReference = new NodeReference();
		nodeReference.setNode(node);
		nodeReference.setCanContainText(true);
		nodeReference.setComponent(component);
		return nodeReference;
	}

	public static HorizontalBar horizontalBar(final int height) {
		final HorizontalBar horizontalBar = new HorizontalBar();
		horizontalBar.setHeight(height);
		return horizontalBar;
	}

	public static HorizontalBar horizontalBar(final int height, final Color color) {
		final HorizontalBar horizontalBar = new HorizontalBar();
		horizontalBar.setHeight(height);
		horizontalBar.setColor(color);
		return horizontalBar;
	}

	public static Paragraph paragraph(final IInlineBox... children) {
		final Paragraph paragraph = new Paragraph();
		for (final IInlineBox child : children) {
			paragraph.appendChild(child);
		}
		return paragraph;
	}

	public static TextContent textContent(final IContent content, final ContentRange range, final FontSpec font) {
		final TextContent textContent = new TextContent();
		textContent.setContent(content, range);
		textContent.setFont(font);
		return textContent;
	}

	public static StaticText staticText(final String text, final FontSpec font) {
		final StaticText staticText = new StaticText();
		staticText.setText(text);
		staticText.setFont(font);
		return staticText;
	}

	public static Square square(final int size) {
		final Square square = new Square();
		square.setSize(size);
		return square;
	}
}
