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
package org.eclipse.vex.core.internal.layout;

import java.util.List;

import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;

/**
 * @author Florian Thienel
 */
public class ListItemBox extends BlockElementBox {

	// hspace between list-item bullet and block as fraction of font-size
	private static final float BULLET_SPACE = 0.5f;

	private BlockBox itemMarker;

	public ListItemBox(final LayoutContext context, final BlockBox parent, final INode node) {
		super(context, parent, node);
	}

	@Override
	public void paint(final LayoutContext context, final int x, final int y) {
		super.paint(context, x, y);

		if (itemMarker != null) {
			itemMarker.paint(context, x + itemMarker.getX(), y + itemMarker.getY());
		}
	}

	@Override
	protected int positionChildren(final LayoutContext context) {
		final int repaintStart = super.positionChildren(context);

		final Styles styles = context.getStyleSheet().getStyles(getNode());
		if (itemMarker != null) {
			final int x = -itemMarker.getWidth() - Math.round(BULLET_SPACE * styles.getFontSize());
			int y = getFirstLineTop(context);
			final LineBox firstLine = getFirstLine();
			if (firstLine != null) {
				y += firstLine.getBaseline() - itemMarker.getFirstLine().getBaseline();
			}

			itemMarker.setX(x);
			itemMarker.setY(y);
		}

		return repaintStart;
	}

	@Override
	public List<Box> createChildren(final LayoutContext context) {
		final Styles styles = context.getStyleSheet().getStyles(getNode());
		if (!styles.getListStyleType().equals(CSS.NONE)) {
			itemMarker = createItemMarker(context, getNode());
		}
		return super.createChildren(context);
	}

	private static BlockBox createItemMarker(final LayoutContext context, final INode item) {
		final Styles styles = context.getStyleSheet().getStyles(item);
		final String listStyleType = styles.getListStyleType();
		final float fontSize = styles.getFontSize();
		if (listStyleType.equals(CSS.NONE)) {
			return null;
		}

		final InlineBox markerInline;
		if (isEnumeratedListStyleType(listStyleType)) {
			markerInline = createEnumeratedMarker(context, item, listStyleType);
		} else if (listStyleType.equals(CSS.CIRCLE)) {
			markerInline = createCircleBullet(item, fontSize);
		} else if (listStyleType.equals(CSS.SQUARE)) {
			markerInline = createSquareBullet(item, fontSize);
		} else {
			markerInline = createDiscBullet(item, fontSize);
		}

		return ParagraphBox.create(context, item, new InlineBox[] { markerInline }, Integer.MAX_VALUE);
	}

	private static InlineBox createEnumeratedMarker(final LayoutContext context, final INode item, final String listStyleType) {
		final String numberString = numberToString(getItemNumber(item), listStyleType);
		return new StaticTextBox(context, item, numberString + ".");
	}

	private static InlineBox createCircleBullet(final INode node, final float fontSize) {
		return new DrawableBox(new CircleBullet(fontSize), node);
	}

	private static InlineBox createDiscBullet(final INode node, final float fontSize) {
		return new DrawableBox(new DiscBullet(fontSize), node);
	}

	private static InlineBox createSquareBullet(final INode node, final float fontSize) {
		return new DrawableBox(new SquareBullet(fontSize), node);
	}

	private static int getItemNumber(final INode item) {
		final IParent parent = item.getParent();

		if (parent == null) {
			return 1;
		}

		int number = 1;
		for (final INode child : parent.children()) {
			if (child == item) {
				return number;
			}
			if (child.isKindOf(item)) {
				number++;
			}
		}

		throw new IllegalStateException();
	}

	private static String numberToString(final int number, final String listStyleType) {
		if (listStyleType.equals(CSS.DECIMAL_LEADING_ZERO)) {
			if (number < 10) {
				return "0" + Integer.toString(number);
			} else {
				return Integer.toString(number);
			}
		} else if (listStyleType.equals(CSS.LOWER_ALPHA) || listStyleType.equals(CSS.LOWER_LATIN)) {
			return numberAsAlpha(number);
		} else if (listStyleType.equals(CSS.LOWER_ROMAN)) {
			return numberAsRoman(number);
		} else if (listStyleType.equals(CSS.UPPER_ALPHA) || listStyleType.equals(CSS.UPPER_LATIN)) {
			return numberAsAlpha(number).toUpperCase();
		} else if (listStyleType.equals(CSS.UPPER_ROMAN)) {
			return numberAsRoman(number).toUpperCase();
		} else {
			return Integer.toString(number);
		}
	}

	private static String numberAsAlpha(final int number) {
		final String alphabet = "abcdefghijklmnopqrstuvwxyz";
		return String.valueOf(alphabet.charAt((number - 1) % 26));
	}

	private static String numberAsRoman(final int number) {
		final String[] ones = { "", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix" };
		final String[] tens = { "", "x", "xx", "xxx", "xl", "l", "lx", "lxx", "lxxx", "xc" };
		final String[] hundreds = { "", "c", "cc", "ccc", "cd", "d", "dc", "dcc", "dccc", "cm" };
		final StringBuilder romanNumber = new StringBuilder();
		for (int i = 0; i < number / 1000; i++) {
			romanNumber.append("m");
		}
		romanNumber.append(hundreds[number / 100 % 10]);
		romanNumber.append(tens[number / 10 % 10]);
		romanNumber.append(ones[number % 10]);
		return romanNumber.toString();
	}

	private static boolean isEnumeratedListStyleType(final String s) {
		return s.equals(CSS.ARMENIAN) || s.equals(CSS.CJK_IDEOGRAPHIC) || s.equals(CSS.DECIMAL) || s.equals(CSS.DECIMAL_LEADING_ZERO) || s.equals(CSS.GEORGIAN) || s.equals(CSS.HEBREW)
				|| s.equals(CSS.HIRAGANA) || s.equals(CSS.HIRAGANA_IROHA) || s.equals(CSS.KATAKANA) || s.equals(CSS.KATAKANA_IROHA) || s.equals(CSS.LOWER_ALPHA) || s.equals(CSS.LOWER_GREEK)
				|| s.equals(CSS.LOWER_LATIN) || s.equals(CSS.LOWER_ROMAN) || s.equals(CSS.UPPER_ALPHA) || s.equals(CSS.UPPER_LATIN) || s.equals(CSS.UPPER_ROMAN);
	}

}
