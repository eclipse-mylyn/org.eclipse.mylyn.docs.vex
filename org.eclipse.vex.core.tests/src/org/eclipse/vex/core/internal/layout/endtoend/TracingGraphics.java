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
package org.eclipse.vex.core.internal.layout.endtoend;

import java.net.URL;

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.FontMetrics;
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Image;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.layout.FakeImage;

/**
 * @author Florian Thienel
 */
public class TracingGraphics implements Graphics {

	public static final int CHAR_WIDTH = 6;

	public static final FontMetrics FONT_METRICS = new FontMetrics() {
		@Override
		public int getAscent() {
			return 8;
		}

		@Override
		public int getDescent() {
			return 4;
		}

		@Override
		public int getHeight() {
			return getDescent() + getAscent() + getLeading();
		}

		@Override
		public int getLeading() {
			return 3;
		}
	};

	private final Tracer tracer;

	private boolean antiAliased;
	private int lineWidth;
	private int lineStyle;
	private FontResource font;
	private ColorResource color;
	private ColorResource foreground;
	private ColorResource background;
	private final List<GraphicsListener> listeners = new CopyOnWriteArrayList<GraphicsListener>();

	public TracingGraphics(final Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public void moveOrigin(final int offsetX, final int offsetY) {
		tracer.trace("Graphics.moveOrigin({0}, {1})", offsetX, offsetY);
	}

	@Override
	public int charsWidth(final char[] data, final int offset, final int length) {
		return CHAR_WIDTH * length;
	}

	@Override
	public ColorResource createColor(final Color color) {
		tracer.trace("Graphics.createColor({0})", color);
		return new TracingColorResource(tracer, color);
	}

	@Override
	public FontResource getFont(final FontSpec fontSpec) {
		tracer.trace("Graphics.createFont({0})", fontSpec);
		return new TracingFontResource(tracer, fontSpec);
	}

	@Override
	public void dispose() {
		tracer.trace("Graphics.dispose()");
	}

	@Override
	public void drawChars(final char[] chars, final int offset, final int length, final int x, final int y) {
		tracer.trace("Graphics.drawChars({0}, {1,number,#}, {2,number,#}, {3,number,#}, {4,number,#})", new String(chars), offset, length, x, y);
	}

	@Override
	public void drawLine(final int x1, final int y1, final int x2, final int y2) {
		tracer.trace("Graphics.drawLine({0,number,#}, {1,number,#}, {2,number,#}, {3,number,#})", x1, y1, x2, y2);
	}

	@Override
	public void drawString(final String s, final int x, final int y) {
		tracer.trace("Graphics.drawString({0}, {1,number,#}, {2,number,#})", s, x, y);
	}

	@Override
	public void drawOval(final int x, final int y, final int width, final int height) {
		tracer.trace("Graphics.drawOval({0,number,#}, {1,number,#}, {2,number,#}, {3,number,#})", x, y, width, height);
	}

	@Override
	public void drawRect(final int x, final int y, final int width, final int height) {
		tracer.trace("Graphics.drawRect({0,number,#}, {1,number,#}, {2,number,#}, {3,number,#})", x, y, width, height);
	}

	@Override
	public void drawImage(final Image image, final int x, final int y, final int width, final int height) {
		tracer.trace("Graphics.drawImage({0}, {1,number,#}, {2,number,#}, {3,number,#}, {4,number,#})", image, x, y, width, height);
	}

	@Override
	public void fillOval(final int x, final int y, final int width, final int height) {
		tracer.trace("Graphics.fillOval({0,number,#}, {1,number,#}, {2,number,#}, {3,number,#})", x, y, width, height);
	}

	@Override
	public void fillRect(final int x, final int y, final int width, final int height) {
		tracer.trace("Graphics.fillRect({0,number,#}, {1,number,#}, {2,number,#}, {3,number,#})", x, y, width, height);
	}

	@Override
	public Rectangle getClipBounds() {
		return new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public FontResource getCurrentFont() {
		return font;
	}

	@Override
	public int getLineStyle() {
		return lineStyle;
	}

	@Override
	public int getLineWidth() {
		return lineWidth;
	}

	@Override
	public ColorResource getSystemColor(final int id) {
		tracer.trace("Graphics.getSystemColor({0})", id);
		return new TracingColorResource(tracer, id);
	}

	@Override
	public FontMetrics getFontMetrics() {
		return FONT_METRICS;
	}

	@Override
	public Image getImage(final URL url) {
		tracer.trace("Graphics.getImage({0})", url);
		return new FakeImage(url);
	}

	@Override
	public boolean isAntiAliased() {
		return antiAliased;
	}

	@Override
	public void setAntiAliased(final boolean antiAliased) {
		tracer.trace("Graphics.setAntialiased({0})", antiAliased);
		this.antiAliased = antiAliased;
	}

	@Override
	public ColorResource getColor() {
		return color;
	}

	@Override
	public ColorResource setColor(final ColorResource color) {
		tracer.trace("Graphics.setColor({0})", color);
		final ColorResource oldColor = getColor();
		this.color = color;
		return oldColor;
	}

	@Override
	public ColorResource getForeground() {
		return foreground;
	}

	@Override
	public ColorResource setForeground(final ColorResource color) {
		tracer.trace("Graphics.setForeground({0})", color);
		final ColorResource oldColor = getForeground();
		foreground = color;
		return oldColor;
	}

	@Override
	public ColorResource getBackground() {
		return background;
	}

	@Override
	public ColorResource setBackground(final ColorResource color) {
		tracer.trace("Graphics.setBackground({0})", color);
		final ColorResource oldColor = getBackground();
		background = color;
		return oldColor;
	}

	@Override
	public FontResource setCurrentFont(final FontResource font) {
		tracer.trace("Graphics.setFont({0})", font);
		final FontResource oldFont = getCurrentFont();
		this.font = font;
		return oldFont;
	}

	@Override
	public void setLineStyle(final int style) {
		tracer.trace("Graphics.setLineStyle({0})", style);
		lineStyle = style;
	}

	@Override
	public void setLineWidth(final int width) {
		tracer.trace("Graphics.setLineWidth({0,number,#})", width);
		lineWidth = width;
	}

	@Override
	public int stringWidth(final String s) {
		return CHAR_WIDTH * s.length();
	}

}
