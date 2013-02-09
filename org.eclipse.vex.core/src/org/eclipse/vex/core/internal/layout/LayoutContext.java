/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.vex.core.dom.IDocument;
import org.eclipse.vex.core.dom.INode;
import org.eclipse.vex.core.internal.VEXCorePlugin;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.css.StyleSheet;

/**
 * Encapsulation of all the resources needed to create a box tree. Most operations on a box tree, such as creating the
 * tree, painting the tree, and converting between spatial and model coordinates, require the context.
 */
public class LayoutContext {

	private BoxFactory boxFactory;
	private IDocument document;
	private Graphics graphics;
	private StyleSheet styleSheet;
	private int selectionStart;
	private int selectionEnd;
	private long startTime = System.currentTimeMillis();

	/**
	 * Class constructor.
	 */
	public LayoutContext() {
	}

	/**
	 * Returns the BoxFactory used to generate boxes for the layout.
	 */
	public BoxFactory getBoxFactory() {
		return boxFactory;
	}

	/**
	 * Returns the document being layed out.
	 */
	public IDocument getDocument() {
		return document;
	}

	/**
	 * Returns the <code>Graphics</code> object used for layout. Box paint methods use this graphics for painting.
	 */
	public Graphics getGraphics() {
		return graphics;
	}

	/**
	 * Returns the time the layout was started. Actually, it's the time since this context was created, as returned by
	 * System.currentTimeMills().
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Returns the <code>StyleSheet</code> used for this layout.
	 */
	public StyleSheet getStyleSheet() {
		return styleSheet;
	}

	/**
	 * Helper method that returns true if the given node is in the selected range.
	 * 
	 * @param node
	 *            Node to test. May be null, in which case this method returns false.
	 */
	public boolean isNodeSelected(final INode node) {
		return node != null && node.getStartOffset() >= getSelectionStart() && node.getEndOffset() + 1 <= getSelectionEnd();
	}

	/**
	 * Resets the start time to currentTimeMillis.
	 */
	public void resetStartTime() {
		startTime = System.currentTimeMillis();
	}

	/**
	 * Sets the BoxFactory used to generate boxes for this layout.
	 */
	public void setBoxFactory(final BoxFactory factory) {
		boxFactory = factory;
	}

	/**
	 * Sets the document being layed out.
	 */
	public void setDocument(final IDocument document) {
		this.document = document;
	}

	/**
	 * Sets the Graphics object used for this layout.
	 */
	public void setGraphics(final Graphics graphics) {
		this.graphics = graphics;
	}

	/**
	 * Sets the stylesheet used for this layout.
	 */
	public void setStyleSheet(final StyleSheet sheet) {
		styleSheet = sheet;
	}

	/**
	 * Returns the offset where the current selection ends.
	 */
	public int getSelectionEnd() {
		return selectionEnd;
	}

	/**
	 * Returns the offset where the current selection starts.
	 */
	public int getSelectionStart() {
		return selectionStart;
	}

	/**
	 * Sets the offset where the current selection ends.
	 * 
	 * @param i
	 *            the new value for selectionEnd
	 */
	public void setSelectionEnd(final int i) {
		selectionEnd = i;
	}

	/**
	 * Sets the offset where the current selection starts.
	 * 
	 * @param i
	 *            the new value for selectionStart
	 */
	public void setSelectionStart(final int i) {
		selectionStart = i;
	}

	public URL resolveUrl(final String baseUri, final String urlSpecification) {
		try {
			if (baseUri == null) {
				return new URL(urlSpecification);
			} else {
				return new URL(new URL(baseUri), urlSpecification);
			}
		} catch (final MalformedURLException e) {
			VEXCorePlugin.getInstance().getLog().log(new Status(IStatus.ERROR, VEXCorePlugin.ID, MessageFormat.format("Cannot resolve image url: {0}", urlSpecification), e));
			return null;
		}
	}
}
