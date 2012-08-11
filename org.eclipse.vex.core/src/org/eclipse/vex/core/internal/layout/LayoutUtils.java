/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.vex.core.internal.core.IntRange;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.Node;

/**
 * Tools for layout and rendering of CSS-styled boxes
 */
public class LayoutUtils {

	/**
	 * Creates a list of generated inline boxes for the given pseudo-element.
	 * 
	 * @param context
	 *            LayoutContext in use
	 * @param pseudoElement
	 *            Element representing the generated content.
	 */
	public static List<InlineBox> createGeneratedInlines(final LayoutContext context, final Element pseudoElement) {
		final String text = getGeneratedContent(context, pseudoElement);
		final List<InlineBox> list = new ArrayList<InlineBox>();
		if (text.length() > 0) {
			list.add(new StaticTextBox(context, pseudoElement, text));
		}
		return list;
	}

	/**
	 * Returns <code>true</code> if the given offset falls within the given element or range.
	 * 
	 * @param elementOrRange
	 *            Element or IntRange object representing a range of offsets.
	 * @param offset
	 *            Offset to test.
	 */
	public static boolean elementOrRangeContains(final Object elementOrRange, final int offset) {
		if (elementOrRange instanceof Element) {
			final Element element = (Element) elementOrRange;
			return offset > element.getStartOffset() && offset <= element.getEndOffset();
		} else {
			final IntRange range = (IntRange) elementOrRange;
			return offset >= range.getStart() && offset <= range.getEnd();
		}
	}

	/**
	 * Creates a string representing the generated content for the given pseudo-element.
	 * 
	 * @param context
	 *            LayoutContext in use
	 * @param pseudoElement
	 *            PseudoElement for which the generated content is to be returned.
	 */
	private static String getGeneratedContent(final LayoutContext context, final Element pseudoElement) {
		final Styles styles = context.getStyleSheet().getStyles(pseudoElement);
		final List<String> content = styles.getContent();
		final StringBuffer sb = new StringBuffer();
		for (final String string : content) {
			sb.append(string); // TODO: change to ContentPart
		}
		return sb.toString();
	}

	/**
	 * Call the given callback for each child matching one of the given display styles. Any nodes that do not match one
	 * of the given display types cause the onRange callback to be called, with a range covering all such contiguous
	 * nodes.
	 * 
	 * @param context
	 *            LayoutContext to use.
	 * @param displayStyles
	 *            Display types to be explicitly recognized.
	 * @param element
	 *            Element containing the children over which to iterate.
	 * @param startOffset
	 *            Starting offset of the range containing nodes in which we're interested.
	 * @param endOffset
	 *            Ending offset of the range containing nodes in which we're interested.
	 * @param callback
	 *            DisplayStyleCallback through which the caller is notified of matching elements and non-matching
	 *            ranges.
	 */
	public static void iterateChildrenByDisplayStyle(final StyleSheet styleSheet, final Set<String> displayStyles, final Element element, final int startOffset, final int endOffset,
			final ElementOrRangeCallback callback) {

		final List<Node> nonMatching = new ArrayList<Node>();

		final List<Node> nodes = element.getChildNodes();
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i).getEndOffset() <= startOffset) {
				continue;
			} else if (nodes.get(i).getStartOffset() >= endOffset) {
				break;
			} else {
				final Node node = nodes.get(i);

				if (node instanceof Element) {
					final Element childElement = (Element) node;
					final String display = styleSheet.getStyles(childElement).getDisplay();
					if (displayStyles.contains(display)) {
						if (!nonMatching.isEmpty()) {
							final Node firstNode = nonMatching.get(0);
							final Node lastNode = nonMatching.get(nonMatching.size() - 1);
							if (lastNode instanceof Element) {
								callback.onRange(element, firstNode.getStartOffset(), lastNode.getEndOffset() + 1);
							} else {
								callback.onRange(element, firstNode.getStartOffset(), lastNode.getEndOffset());
							}
							nonMatching.clear();
						}
						callback.onElement(childElement, display);
					} else {
						nonMatching.add(node);
					}
				} else {
					nonMatching.add(node);
				}
			}
		}

		if (!nonMatching.isEmpty()) {
			final Node firstNode = nonMatching.get(0);
			final Node lastNode = nonMatching.get(nonMatching.size() - 1);
			if (lastNode instanceof Element) {
				callback.onRange(element, firstNode.getStartOffset(), lastNode.getEndOffset() + 1);
			} else {
				callback.onRange(element, firstNode.getStartOffset(), lastNode.getEndOffset());
			}
		}
	}

	/**
	 * Call the given callback for each child matching one of the given display styles. Any nodes that do not match one
	 * of the given display types cause the onRange callback to be called, with a range covering all such contiguous
	 * nodes.
	 * 
	 * @param context
	 *            LayoutContext to use.
	 * @param displayStyles
	 *            Display types to be explicitly recognized.
	 * @param table
	 *            Element containing the children over which to iterate.
	 * @param callback
	 *            DisplayStyleCallback through which the caller is notified of matching elements and non-matching
	 *            ranges.
	 */
	public static void iterateChildrenByDisplayStyle(final StyleSheet styleSheet, final Set<String> displayStyles, final Element table, final ElementOrRangeCallback callback) {
		iterateChildrenByDisplayStyle(styleSheet, displayStyles, table, table.getStartOffset() + 1, table.getEndOffset(), callback);
	}

	/**
	 * Returns true if the given styles represent an element that can be the child of a table element.
	 * 
	 * @param styleSheet
	 *            StyleSheet to use.
	 * @param element
	 *            Element to test.
	 */
	public static boolean isTableChild(final StyleSheet styleSheet, final Element element) {
		final String display = styleSheet.getStyles(element).getDisplay();
		return TABLE_CHILD_STYLES.contains(display);
	}

	public static void iterateTableRows(final StyleSheet styleSheet, final Element element, final int startOffset, final int endOffset, final ElementOrRangeCallback callback) {

		iterateChildrenByDisplayStyle(styleSheet, NON_ROW_STYLES, element, startOffset, endOffset, new ElementOrRangeCallback() {
			public void onElement(final Element child, final String displayStyle) {
				if (displayStyle.equals(CSS.TABLE_ROW_GROUP) || displayStyle.equals(CSS.TABLE_HEADER_GROUP) || displayStyle.equals(CSS.TABLE_FOOTER_GROUP)) {

					// iterate rows in group
					iterateChildrenByDisplayStyle(styleSheet, ROW_STYLES, child, child.getStartOffset() + 1, child.getEndOffset(), callback);
				} else {
					// other element's can't contain rows
				}
			}

			public void onRange(final Element parent, final int startOffset, final int endOffset) {
				// iterate over rows in range
				iterateChildrenByDisplayStyle(styleSheet, ROW_STYLES, element, startOffset, endOffset, callback);
			}
		});

	}

	public static void iterateTableCells(final StyleSheet styleSheet, final Element element, final int startOffset, final int endOffset, final ElementOrRangeCallback callback) {
		iterateChildrenByDisplayStyle(styleSheet, CELL_STYLES, element, startOffset, endOffset, callback);
	}

	public static void iterateTableCells(final StyleSheet styleSheet, final Element row, final ElementOrRangeCallback callback) {
		iterateChildrenByDisplayStyle(styleSheet, CELL_STYLES, row, row.getStartOffset(), row.getEndOffset(), callback);
	}

	/**
	 * Set of CSS display values that represent elements that can be children of table elements.
	 */
	public static final Set<String> TABLE_CHILD_STYLES = new HashSet<String>();

	private static final Set<String> NON_ROW_STYLES = new HashSet<String>();
	private static final Set<String> ROW_STYLES = new HashSet<String>();
	private static final Set<String> CELL_STYLES = new HashSet<String>();

	static {
		NON_ROW_STYLES.add(CSS.TABLE_CAPTION);
		NON_ROW_STYLES.add(CSS.TABLE_COLUMN);
		NON_ROW_STYLES.add(CSS.TABLE_COLUMN_GROUP);
		NON_ROW_STYLES.add(CSS.TABLE_ROW_GROUP);
		NON_ROW_STYLES.add(CSS.TABLE_HEADER_GROUP);
		NON_ROW_STYLES.add(CSS.TABLE_FOOTER_GROUP);

		ROW_STYLES.add(CSS.TABLE_ROW);

		CELL_STYLES.add(CSS.TABLE_CELL);

		TABLE_CHILD_STYLES.addAll(NON_ROW_STYLES);
		TABLE_CHILD_STYLES.addAll(ROW_STYLES);
		TABLE_CHILD_STYLES.addAll(CELL_STYLES);
	}

}
