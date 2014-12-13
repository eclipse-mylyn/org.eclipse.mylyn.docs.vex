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
		@Override
		public int getAscent() {
			return 10;
		}

		@Override
		public int getDescent() {
			return 3;
		}

		@Override
		public int getHeight() {
			return 13;
		}

		@Override
		public int getLeading() {
			return 2;
		}
	};

	public void moveOrigin(final int offsetX, final int offsetY) {
	}

	@Override
	public int charsWidth(final char[] data, final int offset, final int length) {
		return length * charWidth;
	}

	@Override
	public ColorResource createColor(final Color rgb) {
		return new ColorResource() {
			@Override
			public void dispose() {
			}
		};
	}

	@Override
	public FontResource getFont(final FontSpec fontSpec) {
		return new FontResource() {
			@Override
			public void dispose() {
			}
		};
	}

	@Override
	public void dispose() {
	}

	@Override
	public void drawChars(final char[] chars, final int offset, final int length, final int x, final int y) {
	}

	@Override
	public void drawLine(final int x1, final int y1, final int x2, final int y2) {
	}

	@Override
	public void drawString(final String s, final int x, final int y) {
	}

	@Override
	public void drawOval(final int x, final int y, final int width, final int height) {
	}

	@Override
	public void drawRect(final int x, final int y, final int width, final int height) {
	}

	@Override
	public void drawImage(final Image image, final int x, final int y, final int width, final int height) {
		Assert.isTrue(image instanceof FakeImage);
		lastDrawnImageUrl = ((FakeImage) image).url;
	}

	public URL getLastDrawnImageUrl() {
		return lastDrawnImageUrl;
	}

	@Override
	public void fillOval(final int x, final int y, final int width, final int height) {
	}

	@Override
	public void fillRect(final int x, final int y, final int width, final int height) {
	}

	@Override
	public Rectangle getClipBounds() {
		return new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public ColorResource getBackgroundColor() {
		return null;
	}

	@Override
	public FontResource getCurrentFont() {
		return null;
	}

	@Override
	public int getLineStyle() {
		return 0;
	}

	@Override
	public int getLineWidth() {
		return 0;
	}

	@Override
	public ColorResource getSystemColor(final int id) {
		return null;
	}

	@Override
	public FontMetrics getFontMetrics() {
		return fontMetrics;
	}

	@Override
	public Image getImage(final URL url) {
		return new FakeImage(url);
	}

	@Override
	public boolean isAntiAliased() {
		return false;
	}

	@Override
	public void setAntiAliased(final boolean antiAliased) {
	}

	public ColorResource setBackgroundColor(final ColorResource color) {
		return null;
	}

	@Override
	public ColorResource getColor() {
		return null;
	}

	@Override
	public ColorResource setColor(final ColorResource color) {
		return null;
	}

	@Override
	public ColorResource getForeground() {
		return null;
	}

	@Override
	public ColorResource setForeground(final ColorResource color) {
		return null;
	}

	@Override
	public ColorResource getBackground() {
		return null;
	}

	@Override
	public ColorResource setBackground(final ColorResource color) {
		return null;
	}

	@Override
	public FontResource setCurrentFont(final FontResource font) {
		return null;
	}

	@Override
	public void setLineStyle(final int style) {
	}

	@Override
	public void setLineWidth(final int width) {
	}

	@Override
	public int stringWidth(final String s) {
		return charWidth * s.length();
	}

	public int getCharWidth() {
		return charWidth;
	}

	public void setXORMode(final boolean xorMode) {
	}
}
