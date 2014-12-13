/*******************************************************************************
 * Copyright (c) 2004, 2014 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Mohamadou Nassourou - Bug 298912 - rudimentary support for images
 *     Florian Thienel - foreground, background
 *******************************************************************************/
package org.eclipse.vex.core.internal.core;

import java.net.URL;

/**
 * Interface through which Vex performs graphics operations. Implemented by adapters to the java.awt.Graphics and
 * org.eclipse.swt.graphics.GC classes.
 */
public interface Graphics {

	public static final int LINE_SOLID = 0;
	public static final int LINE_DASH = 1;
	public static final int LINE_DOT = 2;

	public int charsWidth(char[] data, int offset, int length);

	public ColorResource createColor(Color rgb);

	public FontResource getFont(FontSpec fontSpec);

	public void dispose();

	public void moveOrigin(int offsetX, int offsetY);

	public int asAbsoluteX(int relativeX);

	public int asAbsoluteY(int relativeY);

	public int asRelativeX(int absoluteX);

	public int asRelativeY(int absoluteY);

	public void start(Object o);

	public void finish(Object o);

	public void addListener(GraphicsListener listener);

	public void removeListener(GraphicsListener listener);

	public void drawChars(char[] chars, int offset, int length, int x, int y);

	public void drawLine(int x1, int y1, int x2, int y2);

	public void drawString(String s, int x, int y);

	public void drawOval(int x, int y, int width, int height);

	public void drawRect(int x, int y, int width, int height);

	public void drawImage(Image image, int x, int y, int width, int height);

	public void fillOval(int x, int y, int width, int height);

	public void fillRect(int x, int y, int width, int height);

	public Rectangle getClipBounds();

	public FontResource getCurrentFont();

	public int getLineStyle();

	public int getLineWidth();

	public ColorResource getSystemColor(int id);

	public FontMetrics getFontMetrics();

	public Image getImage(URL url);

	public boolean isAntiAliased();

	public void setAntiAliased(boolean antiAliased);

	public ColorResource getColor();

	public ColorResource setColor(ColorResource color);

	public ColorResource getForeground();

	public ColorResource setForeground(ColorResource color);

	public ColorResource getBackground();

	public ColorResource setBackground(ColorResource color);

	public FontResource setCurrentFont(FontResource font);

	public void setLineStyle(int style);

	public void setLineWidth(int width);

	public int stringWidth(String s);

}
