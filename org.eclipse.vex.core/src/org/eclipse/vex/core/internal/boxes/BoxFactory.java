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

import java.net.URL;

import org.eclipse.vex.core.internal.boxes.NodeTag.Kind;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.TextAlign;
import org.eclipse.vex.core.internal.css.BulletStyle;
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

	public static StructuralFrame frame(final IStructuralBox component) {
		final StructuralFrame frame = new StructuralFrame();
		frame.setComponent(component);
		return frame;
	}

	public static StructuralFrame frame(final IStructuralBox component, final Margin margin, final Border border, final Padding padding, final Color backgroundColor) {
		final StructuralFrame frame = new StructuralFrame();
		frame.setComponent(component);
		frame.setMargin(margin);
		frame.setBorder(border);
		frame.setPadding(padding);
		frame.setBackgroundColor(backgroundColor);
		return frame;
	}

	public static InlineFrame frame(final IInlineBox component) {
		final InlineFrame frame = new InlineFrame();
		frame.setComponent(component);
		return frame;
	}

	public static InlineFrame frame(final IInlineBox component, final Margin margin, final Border border, final Padding padding, final Color backgroundColor) {
		final InlineFrame frame = new InlineFrame();
		frame.setComponent(component);
		frame.setMargin(margin);
		frame.setBorder(border);
		frame.setPadding(padding);
		frame.setBackgroundColor(backgroundColor);
		return frame;
	}

	public static StructuralNodeReference nodeReference(final INode node, final IStructuralBox component) {
		final StructuralNodeReference structuralNodeReference = new StructuralNodeReference();
		structuralNodeReference.setNode(node);
		structuralNodeReference.setComponent(component);
		return structuralNodeReference;
	}

	public static StructuralNodeReference nodeReferenceWithInlineContent(final INode node, final IStructuralBox component) {
		final StructuralNodeReference structuralNodeReference = new StructuralNodeReference();
		structuralNodeReference.setNode(node);
		structuralNodeReference.setContainsInlineContent(true);
		structuralNodeReference.setComponent(component);
		return structuralNodeReference;
	}

	public static StructuralNodeReference nodeReferenceWithText(final INode node, final IStructuralBox component) {
		final StructuralNodeReference structuralNodeReference = new StructuralNodeReference();
		structuralNodeReference.setNode(node);
		structuralNodeReference.setCanContainText(true);
		structuralNodeReference.setContainsInlineContent(true);
		structuralNodeReference.setComponent(component);
		return structuralNodeReference;
	}

	public static InlineNodeReference nodeReference(final INode node, final IInlineBox component) {
		final InlineNodeReference inlineNodeReference = new InlineNodeReference();
		inlineNodeReference.setNode(node);
		inlineNodeReference.setComponent(component);
		return inlineNodeReference;
	}

	public static InlineNodeReference nodeReferenceWithText(final INode node, final IInlineBox component) {
		final InlineNodeReference inlineNodeReference = new InlineNodeReference();
		inlineNodeReference.setNode(node);
		inlineNodeReference.setCanContainText(true);
		inlineNodeReference.setComponent(component);
		return inlineNodeReference;
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

	public static List list(final IStructuralBox component, final BulletStyle bulletStyle, final IBulletFactory bulletFactory) {
		final List list = new List();
		list.setBulletStyle(bulletStyle);
		list.setBulletFactory(bulletFactory);
		list.setComponent(component);
		return list;
	}

	public static ListItem listItem(final IStructuralBox component) {
		final ListItem listItem = new ListItem();
		listItem.setComponent(component);
		return listItem;
	}

	public static Table table(final IStructuralBox... children) {
		final Table table = new Table();
		for (final IStructuralBox child : children) {
			table.appendChild(child);
		}
		return table;
	}

	public static TableRowGroup tableRowGroup(final IStructuralBox... children) {
		final TableRowGroup tableRowGroup = new TableRowGroup();
		for (final IStructuralBox child : children) {
			tableRowGroup.appendChild(child);
		}
		return tableRowGroup;
	}

	public static TableColumnSpec tableColumnSpec(final String name, final int startIndex, final int endIndex, final String startName, final String endName, final IStructuralBox component) {
		final TableColumnSpec tableColumnSpec = new TableColumnSpec();
		tableColumnSpec.setName(name);
		tableColumnSpec.setStartIndex(startIndex);
		tableColumnSpec.setEndIndex(endIndex);
		tableColumnSpec.setStartName(startName);
		tableColumnSpec.setEndName(endName);
		tableColumnSpec.setComponent(component);
		return tableColumnSpec;
	}

	public static TableRow tableRow(final IStructuralBox... children) {
		final TableRow tableRow = new TableRow();
		for (final IStructuralBox child : children) {
			tableRow.appendChild(child);
		}
		return tableRow;
	}

	public static TableCell tableCell(final IStructuralBox... children) {
		final TableCell tableCell = new TableCell();
		for (final IStructuralBox child : children) {
			tableCell.appendChild(child);
		}
		return tableCell;
	}

	public static Paragraph paragraph(final IInlineBox... children) {
		final Paragraph paragraph = new Paragraph();
		for (final IInlineBox child : children) {
			paragraph.appendChild(child);
		}
		return paragraph;
	}

	public static Paragraph paragraph(final TextAlign textAlign, final IInlineBox... children) {
		final Paragraph paragraph = new Paragraph();
		for (final IInlineBox child : children) {
			paragraph.appendChild(child);
		}
		paragraph.setTextAlign(textAlign);
		return paragraph;
	}

	public static InlineContainer inlineContainer(final IInlineBox... children) {
		final InlineContainer inlineContainer = new InlineContainer();
		for (final IInlineBox child : children) {
			inlineContainer.appendChild(child);
		}
		return inlineContainer;
	}

	public static TextContent textContent(final IContent content, final ContentRange range, final FontSpec font, final Color color) {
		final TextContent textContent = new TextContent();
		textContent.setContent(content, range);
		textContent.setFont(font);
		textContent.setColor(color);
		return textContent;
	}

	public static NodeEndOffsetPlaceholder endOffsetPlaceholder(final INode node, final FontSpec font) {
		final NodeEndOffsetPlaceholder contentPlaceholder = new NodeEndOffsetPlaceholder();
		contentPlaceholder.setNode(node);
		contentPlaceholder.setFont(font);
		return contentPlaceholder;
	}

	public static StaticText staticText(final String text, final FontSpec font, final Color color) {
		final StaticText staticText = new StaticText();
		staticText.setText(text);
		staticText.setFont(font);
		staticText.setColor(color);
		return staticText;
	}

	public static Image image(final URL imageUrl) {
		final Image image = new Image();
		image.setImageUrl(imageUrl);
		return image;
	}

	public static Square square(final int size, final Color color) {
		final Square square = new Square();
		square.setSize(size);
		square.setColor(color);
		return square;
	}

	public static GraphicalBullet graphicalBullet(final BulletStyle.Type type, final FontSpec font, final Color color) {
		final GraphicalBullet bullet = new GraphicalBullet();
		bullet.setType(type);
		bullet.setFont(font);
		bullet.setColor(color);
		return bullet;
	}

	public static NodeTag nodeTag(final Kind kind, final INode node, final Color foreground, final boolean showText, final float fontSize) {
		final NodeTag nodeTag = new NodeTag();
		nodeTag.setKind(kind);
		nodeTag.setNode(node);
		nodeTag.setColor(foreground);
		nodeTag.setShowText(showText);
		nodeTag.setFontSize(fontSize);
		return nodeTag;
	}
}
