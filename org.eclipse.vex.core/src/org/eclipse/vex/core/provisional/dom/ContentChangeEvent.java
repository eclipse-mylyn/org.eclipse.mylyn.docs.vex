/*******************************************************************************
 * Copyright (c) 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 * 		Carsten Hiesserich - added structuralChange flag
 *******************************************************************************/
package org.eclipse.vex.core.provisional.dom;

/**
 * Notification about a change of content: content was either inserted or deleted.
 *
 * @author Florian Thienel
 */
public class ContentChangeEvent extends DocumentEvent {

	private static final long serialVersionUID = 1L;

	private final ContentRange range;
	private final boolean structuralChange;

	/**
	 * Create an event.
	 *
	 * @param document
	 *            the document that changed
	 * @param parent
	 *            the parent node containing the change
	 * @param range
	 *            the range which was changed
	 * @param structuralChange
	 *            <code>true</code> if the structure is changed (childs added or removed)
	 */
	public ContentChangeEvent(final IDocument document, final IParent parent, final ContentRange range, final boolean structuralChange) {
		super(document, parent);
		this.structuralChange = structuralChange;
		this.range = range;
	}

	/**
	 * @return the range which was changed
	 */
	public ContentRange getRange() {
		return range;
	}

	/**
	 * @return <code>true</code> when this event has been triggered by a structural change (childs added or removed).
	 */
	public boolean isStructuralChange() {
		return structuralChange;
	}

}
