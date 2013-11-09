/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Carsten Hiesserich - make getGeneratedContent public
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.PseudoElement;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.ContentPositionRange;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;

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
	 * @param marker
	 *            Marker if this node represents the start sentinel or the end sentinel of the element. See also
	 *            {@link StaticTextBox#StaticTextBox(LayoutContext, INode, String, byte)}
	 */
	public static List<InlineBox> createGeneratedInlines(final LayoutContext context, final IElement pseudoElement, final byte marker) {
		final Styles styles = context.getStyleSheet().getStyles(pseudoElement);
		final INode parent = ((PseudoElement) pseudoElement).getParentNode();
		final String text = getGeneratedContent(context, styles, parent);
		final List<InlineBox> list = new ArrayList<InlineBox>();
		if (text.length() > 0) {
			list.add(new StaticTextBox(context, pseudoElement, text, marker));
		}
		return list;
	}

	/**
	 * Creates a list of generated inline boxes for the given pseudo-element.
	 * 
	 * @param context
	 *            LayoutContext in use
	 * @param pseudoElement
	 *            Element representing the generated content.
	 */
	public static List<InlineBox> createGeneratedInlines(final LayoutContext context, final IElement pseudoElement) {
		return createGeneratedInlines(context, pseudoElement, StaticTextBox.NO_MARKER);
	}

	/**
	 * Returns <code>true</code> if the given offset falls within the given element or range.
	 * 
	 * @param elementOrRange
	 *            Element or IntRange object representing a range of offsets.
	 * @param position
	 *            ContentPosition to test.
	 */
	public static boolean elementOrRangeContains(final Object elementOrRange, final ContentPosition position) {
		if (elementOrRange instanceof IElement) {
			return ((IElement) elementOrRange).containsPosition(position);
		} else if (elementOrRange instanceof ContentPositionRange) {
			return ((ContentPositionRange) elementOrRange).contains(position);
		}
		throw new IllegalArgumentException("elementOrRangeContains expects an IElement or ContentPositionRange.");

	}

	/**
	 * Creates a string representing the generated content for the given node.
	 * 
	 * @param context
	 *            LayoutContext in use
	 * @param styles
	 *            The Styles definition to get the content from
	 * @param node
	 *            The node passed to Styles#getContent (to get attr values from)
	 */
	public static String getGeneratedContent(final LayoutContext context, final Styles styles, final INode node) {
		final List<String> content = styles.getContent(node);
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
	 * @param parent
	 *            Parent containing the children over which to iterate.
	 * @param startOffset
	 *            Starting offset of the range containing nodes in which we're interested.
	 * @param endOffset
	 *            Ending offset of the range containing nodes in which we're interested.
	 * @param callback
	 *            DisplayStyleCallback through which the caller is notified of matching elements and non-matching
	 *            ranges.
	 */
	public static void iterateChildrenByDisplayStyle(final StyleSheet styleSheet, final Set<String> displayStyles, final IParent parent, final int startOffset, final int endOffset,
			final ElementOrRangeCallback callback) {

		final List<INode> nonMatching = new ArrayList<INode>();

		for (final INode node : parent.children()) {
			if (node.getEndOffset() <= startOffset) {
				continue;
			} else if (node.getStartOffset() >= endOffset) {
				break;
			} else {
				if (node instanceof IElement) {
					final IElement childElement = (IElement) node;
					final String display = styleSheet.getStyles(childElement).getDisplay();
					if (displayStyles.contains(display)) {
						if (!nonMatching.isEmpty()) {
							final INode firstNode = nonMatching.get(0);
							final INode lastNode = nonMatching.get(nonMatching.size() - 1);
							if (lastNode instanceof IElement) {
								callback.onRange(parent, firstNode.getStartOffset(), lastNode.getEndOffset() + 1);
							} else {
								callback.onRange(parent, firstNode.getStartOffset(), lastNode.getEndOffset());
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
			final INode firstNode = nonMatching.get(0);
			final INode lastNode = nonMatching.get(nonMatching.size() - 1);
			if (lastNode instanceof IElement) {
				callback.onRange(parent, firstNode.getStartOffset(), lastNode.getEndOffset() + 1);
			} else {
				callback.onRange(parent, firstNode.getStartOffset(), lastNode.getEndOffset());
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
	public static void iterateChildrenByDisplayStyle(final StyleSheet styleSheet, final Set<String> displayStyles, final IElement table, final ElementOrRangeCallback callback) {
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
	public static boolean isTableChild(final StyleSheet styleSheet, final IElement element) {
		final String display = styleSheet.getStyles(element).getDisplay();
		return TABLE_CHILD_STYLES.contains(display);
	}

	public static void iterateTableRows(final StyleSheet styleSheet, final IParent element, final int startOffset, final int endOffset, final ElementOrRangeCallback callback) {

		iterateChildrenByDisplayStyle(styleSheet, NON_ROW_STYLES, element, startOffset, endOffset, new ElementOrRangeCallback() {
			public void onElement(final IElement child, final String displayStyle) {
				if (displayStyle.equals(CSS.TABLE_ROW_GROUP) || displayStyle.equals(CSS.TABLE_HEADER_GROUP) || displayStyle.equals(CSS.TABLE_FOOTER_GROUP)) {

					// iterate rows in group
					iterateChildrenByDisplayStyle(styleSheet, ROW_STYLES, child, child.getStartOffset() + 1, child.getEndOffset(), callback);
				} else {
					// other element's can't contain rows
				}
			}

			public void onRange(final IParent parent, final int startOffset, final int endOffset) {
				// iterate over rows in range
				iterateChildrenByDisplayStyle(styleSheet, ROW_STYLES, element, startOffset, endOffset, callback);
			}
		});

	}

	public static void iterateTableCells(final StyleSheet styleSheet, final IParent element, final int startOffset, final int endOffset, final ElementOrRangeCallback callback) {
		iterateChildrenByDisplayStyle(styleSheet, CELL_STYLES, element, startOffset, endOffset, callback);
	}

	public static void iterateTableCells(final StyleSheet styleSheet, final IParent row, final ElementOrRangeCallback callback) {
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
