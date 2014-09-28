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
package org.eclipse.vex.core.internal.core;

import java.net.URL;

/**
 * @author Florian Thienel
 */
public class RelocatedGraphics implements Graphics {

	private final Graphics delegate;
	private final int offsetX;
	private final int offsetY;

	public RelocatedGraphics(final Graphics delegate, final int offsetX, final int offsetY) {
		this.delegate = delegate;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public int charsWidth(final char[] data, final int offset, final int length) {
		return delegate.charsWidth(data, offset, length);
	}

	public ColorResource createColor(final Color rgb) {
		return delegate.createColor(rgb);
	}

	public FontResource createFont(final FontSpec fontSpec) {
		return delegate.createFont(fontSpec);
	}

	public void dispose() {
		delegate.dispose();
	}

	public void drawChars(final char[] chars, final int offset, final int length, final int x, final int y) {
		delegate.drawChars(chars, offset, length, x + offsetX, y + offsetY);
	}

	public void drawLine(final int x1, final int y1, final int x2, final int y2) {
		delegate.drawLine(x1 + offsetX, y1 + offsetY, x2 + offsetX, y2 + offsetY);
	}

	public void drawString(final String s, final int x, final int y) {
		delegate.drawString(s, x + offsetX, y + offsetY);
	}

	public void drawOval(final int x, final int y, final int width, final int height) {
		delegate.drawOval(x + offsetX, y + offsetY, width, height);
	}

	public void drawRect(final int x, final int y, final int width, final int height) {
		delegate.drawRect(x + offsetX, y + offsetY, width, height);
	}

	public void drawImage(final Image image, final int x, final int y, final int width, final int height) {
		delegate.drawImage(image, x + offsetX, y + offsetY, width, height);
	}

	public void fillOval(final int x, final int y, final int width, final int height) {
		delegate.fillOval(x + offsetX, y + offsetY, width, height);
	}

	public void fillRect(final int x, final int y, final int width, final int height) {
		delegate.fillRect(x + offsetX, y + offsetY, width, height);
	}

	public Rectangle getClipBounds() {
		final int x = delegate.getClipBounds().getX();
		final int y = delegate.getClipBounds().getY();
		final int width = delegate.getClipBounds().getWidth();
		final int height = delegate.getClipBounds().getHeight();
		return new Rectangle(x - offsetX, y - offsetY, width, height);
	}

	public ColorResource getColor() {
		return delegate.getColor();
	}

	public FontResource getFont() {
		return delegate.getFont();
	}

	public int getLineStyle() {
		return delegate.getLineStyle();
	}

	public int getLineWidth() {
		return delegate.getLineWidth();
	}

	public ColorResource getSystemColor(final int id) {
		return delegate.getSystemColor(id);
	}

	public FontMetrics getFontMetrics() {
		return delegate.getFontMetrics();
	}

	public Image getImage(final URL url) {
		return delegate.getImage(url);
	}

	public boolean isAntiAliased() {
		return delegate.isAntiAliased();
	}

	public void setAntiAliased(final boolean antiAliased) {
		delegate.setAntiAliased(antiAliased);
	}

	public ColorResource setColor(final ColorResource color) {
		return delegate.setColor(color);
	}

	public FontResource setFont(final FontResource font) {
		return delegate.setFont(font);
	}

	public void setLineStyle(final int style) {
		delegate.setLineStyle(style);
	}

	public void setLineWidth(final int width) {
		delegate.setLineWidth(width);
	}

	public int stringWidth(final String s) {
		return delegate.stringWidth(s);
	}

}
