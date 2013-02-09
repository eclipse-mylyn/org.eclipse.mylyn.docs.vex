/*******************************************************************************
 * Copyright (c) 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.dom;

/**
 * Notification about a change of content: content was either inserted or deleted.
 * 
 * @author Florian Thienel
 */
public class ContentChangeEvent extends DocumentEvent {

	private static final long serialVersionUID = 1L;

	private final ContentRange range;

	/**
	 * Create an event.
	 * 
	 * @param document
	 *            the document that changed
	 * @param parent
	 *            the parent node containing the change
	 * @param range
	 *            the range which was changed
	 */
	public ContentChangeEvent(final IDocument document, final IParent parent, final ContentRange range) {
		super(document, parent);

		this.range = range;
	}

	/**
	 * @return the range which was changed
	 */
	public ContentRange getRange() {
		return range;
	}

}
