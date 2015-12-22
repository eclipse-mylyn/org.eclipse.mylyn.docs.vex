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
 *     Florian Thienel - font cache, color cache
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget.swt;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.vex.core.internal.VEXCorePlugin;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.FontMetrics;
import org.eclipse.vex.core.internal.core.FontResource;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Image;
import org.eclipse.vex.core.internal.core.LineStyle;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * Implementation of the Vex Graphics interface, mapping it to a org.eclipse.swt.graphics.GC object.
 */
public class SwtGraphics implements Graphics {

	private final GC gc;
	private int offsetX;
	private int offsetY;

	private final HashMap<FontSpec, FontResource> fonts = new HashMap<FontSpec, FontResource>();
	private final HashMap<Color, ColorResource> colors = new HashMap<Color, ColorResource>();

	private SwtFont currentFont;
	private SwtFontMetrics currentFontMetrics;
	private LineStyle lineStyle = LineStyle.SOLID;

	/**
	 * @param gc
	 *            SWT GC to which we are drawing.
	 */
	public SwtGraphics(final GC gc) {
		this.gc = gc;
		currentFont = new SwtFont(gc.getFont());
	}

	@Override
	public void dispose() {
		for (final FontResource font : fonts.values()) {
			font.dispose();
		}
		fonts.clear();
		for (final ColorResource color : colors.values()) {
			color.dispose();
		}
		colors.clear();

		// TODO should not dispose something that comes from outside!
		gc.dispose();
	}

	@Override
	public void moveOrigin(final int offsetX, final int offsetY) {
		this.offsetX += offsetX;
		this.offsetY += offsetY;
	}

	@Override
	public int asAbsoluteX(final int relativeX) {
		return relativeX + offsetX;
	}

	@Override
	public int asAbsoluteY(final int relativeY) {
		return relativeY + offsetY;
	}

	@Override
	public int asRelativeX(final int absoluteX) {
		return absoluteX - offsetX;
	}

	@Override
	public int asRelativeY(final int absoluteY) {
		return absoluteY - offsetY;
	}

	@Override
	public void drawChars(final char[] chars, final int offset, final int length, final int x, final int y) {
		drawString(new String(chars, offset, length), x, y);
	}

	@Override
	public void drawLine(final int x1, final int y1, final int x2, final int y2) {
		gc.drawLine(x1 + offsetX, y1 + offsetY, x2 + offsetX, y2 + offsetY);
	}

	@Override
	public void drawOval(final int x, final int y, final int width, final int height) {
		gc.drawOval(x + offsetX, y + offsetY, width, height);
	}

	@Override
	public void drawRect(final int x, final int y, final int width, final int height) {
		gc.drawRectangle(x + offsetX, y + offsetY, width, height);
	}

	@Override
	public void drawRoundRect(final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight) {
		gc.drawRoundRectangle(x + offsetX, y + offsetY, width, height, arcWidth, arcHeight);
	}

	@Override
	public void drawString(final String s, final int x, final int y) {
		gc.drawString(s, x + offsetX, y + offsetY, true);
	}

	@Override
	public void drawImage(final Image image, final int x, final int y, final int width, final int height) {
		Assert.isTrue(image instanceof SwtImage);
		final org.eclipse.swt.graphics.Image swtImage = new org.eclipse.swt.graphics.Image(gc.getDevice(), ((SwtImage) image).imageData);
		try {
			gc.drawImage(swtImage, 0, 0, image.getWidth(), image.getHeight(), x + offsetX, y + offsetY, width, height);
		} finally {
			swtImage.dispose();
		}
	}

	/**
	 * Fills the given oval with the <em>foreground</em> color. This overrides the default SWT behaviour to be more like
	 * Swing.
	 */
	@Override
	public void fillOval(final int x, final int y, final int width, final int height) {
		gc.fillOval(x + offsetX, y + offsetY, width, height);
	}

	/**
	 * Fills the given rectangle with the <em>foreground</em> color. This overrides the default SWT behaviour to be more
	 * like Swing.
	 */
	@Override
	public void fillRect(final int x, final int y, final int width, final int height) {
		gc.fillRectangle(x + offsetX, y + offsetY, width, height);
	}

	@Override
	public void fillRoundRect(final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight) {
		gc.fillRoundRectangle(x + offsetX, y + offsetY, width, height, arcWidth, arcHeight);
	}

	@Override
	public Rectangle getClipBounds() {
		final org.eclipse.swt.graphics.Rectangle r = gc.getClipping();
		return new Rectangle(r.x - offsetX, r.y - offsetY, r.width, r.height);
	}

	@Override
	public FontResource getCurrentFont() {
		return currentFont;
	}

	@Override
	public FontResource setCurrentFont(final FontResource font) {
		if (font == currentFont) {
			return currentFont;
		}

		final FontResource oldFont = getCurrentFont();
		currentFont = (SwtFont) font;
		gc.setFont(currentFont.getSwtFont());
		currentFontMetrics = new SwtFontMetrics(gc.getFontMetrics());
		return oldFont;
	}

	@Override
	public FontMetrics getFontMetrics() {
		return currentFontMetrics;
	}

	@Override
	public LineStyle getLineStyle() {
		return lineStyle;
	}

	@Override
	public int getLineWidth() {
		return gc.getLineWidth();
	}

	@Override
	public Image getImage(final URL url) {
		final ImageData[] imageData = loadImageData(url);
		if (imageData != null && imageData.length > 0) {
			return new SwtImage(imageData[0]);
		}
		return new SwtImage(Display.getDefault().getSystemImage(SWT.ICON_ERROR).getImageData());
	}

	private static ImageData[] loadImageData(final URL url) {
		final ImageLoader imageLoader = new ImageLoader();
		try {
			final InputStream in = url.openStream();
			try {
				return imageLoader.load(in);
			} finally {
				in.close();
			}
		} catch (final SWTException e) {
			VEXCorePlugin.getInstance().getLog().log(new Status(IStatus.ERROR, VEXCorePlugin.ID, MessageFormat.format("Cannot load image from url: {0}", url), e));
			return null;
		} catch (final IOException e) {
			VEXCorePlugin.getInstance().getLog().log(new Status(IStatus.ERROR, VEXCorePlugin.ID, MessageFormat.format("Cannot load image from url: {0}", url), e));
			return null;
		}
	}

	@Override
	public boolean isAntiAliased() {
		return false;
	}

	@Override
	public void setAntiAliased(final boolean antiAliased) {
	}

	@Override
	public ColorResource getColor() {
		return getForeground();
	}

	@Override
	public ColorResource setColor(final ColorResource color) {
		final ColorResource oldColor = getColor();
		gc.setForeground(((SwtColor) color).getSwtColor());
		gc.setBackground(((SwtColor) color).getSwtColor());
		return oldColor;
	}

	@Override
	public ColorResource getForeground() {
		return new SwtColor(gc.getForeground());
	}

	@Override
	public ColorResource setForeground(final ColorResource color) {
		final ColorResource oldColor = getForeground();
		gc.setForeground(((SwtColor) color).getSwtColor());
		return oldColor;
	}

	@Override
	public ColorResource getBackground() {
		return new SwtColor(gc.getBackground());
	}

	@Override
	public ColorResource setBackground(final ColorResource color) {
		final ColorResource oldColor = getBackground();
		gc.setBackground(((SwtColor) color).getSwtColor());
		return oldColor;
	}

	@Override
	public void setLineStyle(final LineStyle style) {
		lineStyle = style;
		switch (style) {
		case DASHED:
			gc.setLineStyle(SWT.LINE_DASH);
			break;
		case DOTTED:
			gc.setLineStyle(SWT.LINE_DOT);
			break;
		default:
			gc.setLineStyle(SWT.LINE_SOLID);
			break;
		}
	}

	@Override
	public void setLineWidth(final int lineWidth) {
		gc.setLineWidth(lineWidth);
	}

	@Override
	public int charsWidth(final char[] data, final int offset, final int length) {
		return stringWidth(new String(data, offset, length));
	}

	@Override
	public ColorResource getColor(final Color rgb) {
		ColorResource color = colors.get(rgb);
		if (color == null) {
			color = createColor(rgb);
			colors.put(rgb, color);
		}
		return color;
	}

	private SwtColor createColor(final Color rgb) {
		return new SwtColor(new org.eclipse.swt.graphics.Color(null, rgb.getRed(), rgb.getGreen(), rgb.getBlue()));
	}

	@Override
	public FontResource getFont(final FontSpec fontSpec) {
		FontResource font = fonts.get(fontSpec);
		if (font == null) {
			font = createFont(fontSpec);
			fonts.put(fontSpec, font);
		}
		return font;
	}

	private FontResource createFont(final FontSpec fontSpec) {
		int style = SWT.NORMAL;
		if ((fontSpec.getStyle() & FontSpec.BOLD) > 0) {
			style |= SWT.BOLD;
		}
		if ((fontSpec.getStyle() & FontSpec.ITALIC) > 0) {
			style |= SWT.ITALIC;
		}
		final int size = Math.round(fontSpec.getSize() * 72 / 90); // TODO: fix. SWT
		// uses pts, AWT uses device units
		final String[] names = fontSpec.getNames();
		final FontData[] fd = new FontData[names.length];
		for (int i = 0; i < names.length; i++) {
			fd[i] = new FontData(names[i], size, style);
		}
		return new SwtFont(new org.eclipse.swt.graphics.Font(null, fd));
	}

	@Override
	public ColorResource getSystemColor(final int id) {

		if (id == ColorResource.SELECTION_BACKGROUND) {
			return new SwtColor(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION));
		} else if (id == ColorResource.SELECTION_FOREGROUND) {
			return new SwtColor(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
		} else {
			return new SwtColor(Display.getCurrent().getSystemColor(-1));
		}
	}

	@Override
	public int stringWidth(final String s) {
		return gc.stringExtent(s).x;
	}

}
