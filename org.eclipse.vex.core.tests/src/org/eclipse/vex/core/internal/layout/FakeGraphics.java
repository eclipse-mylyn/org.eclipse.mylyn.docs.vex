/*******************************************************************************
 * Copyright (c) 2004, 2010 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Mohamadou Nassourou - Bug 298912 - rudimentary support for images
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.net.URL;

import org.eclipse.core.runtime.Assert;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.DisplayDevice;
import org.eclipse.vex.core.internal.core.FontMetrics;
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Image;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * A pseudo-Graphics class that returns a known set of font metrics.
 */
public class FakeGraphics implements Graphics {

	private final int charWidth = 6;

	private URL lastDrawnImageUrl = null;

	public FakeGraphics() {
		DisplayDevice.setCurrent(new DisplayDevice() {
			@Override
			public int getHorizontalPPI() {
				return 72;
			}

			@Override
			public int getVerticalPPI() {
				return 72;
			}
		});
	}

	private final FontMetrics fontMetrics = new FontMetrics() {
		public int getAscent() {
			return 10;
		}

		public int getDescent() {
			return 3;
		}

		public int getHeight() {
			return 13;
		}

		public int getLeading() {
			return 2;
		}
	};

	public int charsWidth(final char[] data, final int offset, final int length) {
		return length * charWidth;
	}

	public ColorResource createColor(final Color rgb) {
		return new ColorResource() {
			public void dispose() {
			}
		};
	}

	public FontResource createFont(final FontSpec fontSpec) {
		return new FontResource() {
			public void dispose() {
			}
		};
	}

	public void dispose() {
	}

	public void drawChars(final char[] chars, final int offset, final int length, final int x, final int y) {
	}

	public void drawLine(final int x1, final int y1, final int x2, final int y2) {
	}

	public void drawString(final String s, final int x, final int y) {
	}

	public void drawOval(final int x, final int y, final int width, final int height) {
	}

	public void drawRect(final int x, final int y, final int width, final int height) {
	}

	public void drawImage(final Image image, final int x, final int y, final int width, final int height) {
		Assert.isTrue(image instanceof FakeImage);
		lastDrawnImageUrl = ((FakeImage) image).url;
	}

	public URL getLastDrawnImageUrl() {
		return lastDrawnImageUrl;
	}

	public void fillOval(final int x, final int y, final int width, final int height) {
	}

	public void fillRect(final int x, final int y, final int width, final int height) {
	}

	public Rectangle getClipBounds() {
		return new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public ColorResource getBackgroundColor() {
		return null;
	}

	public ColorResource getColor() {
		return null;
	}

	public FontResource getFont() {
		return null;
	}

	public int getLineStyle() {
		return 0;
	}

	public int getLineWidth() {
		return 0;
	}

	public ColorResource getSystemColor(final int id) {
		return null;
	}

	public FontMetrics getFontMetrics() {
		return fontMetrics;
	}

	public Image getImage(final URL url) {
		return new FakeImage(url);
	}

	public boolean isAntiAliased() {
		return false;
	}

	public void setAntiAliased(final boolean antiAliased) {
	}

	public ColorResource setBackgroundColor(final ColorResource color) {
		return null;
	}

	public ColorResource setColor(final ColorResource color) {
		return null;
	}

	public FontResource setFont(final FontResource font) {
		return null;
	}

	public void setLineStyle(final int style) {
	}

	public void setLineWidth(final int width) {
	}

	public int stringWidth(final String s) {
		return charWidth * s.length();
	}

	public int getCharWidth() {
		return charWidth;
	}

	public void setXORMode(final boolean xorMode) {
	}
}
