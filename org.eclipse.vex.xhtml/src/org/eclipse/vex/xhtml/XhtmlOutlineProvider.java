/*******************************************************************************
 * Copyright (c) 2004, 2010 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.xhtml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.vex.core.provisional.dom.Filters;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.ui.internal.editor.VexEditor;
import org.eclipse.vex.ui.internal.outline.IOutlineProvider;

/**
 * Provides an outline of the sections of an XHTML document.
 */
public class XhtmlOutlineProvider implements IOutlineProvider {

	@Override
	public void init(final VexEditor editor) {
	}

	@Override
	public ITreeContentProvider getContentProvider() {
		return contentProvider;
	}

	@Override
	public IBaseLabelProvider getLabelProvider() {
		return labelProvider;
	}

	@Override
	public IElement getOutlineElement(final IElement child) {
		IElement element = child;
		while (element.getParentElement() != null) {

			// TODO: compare to all structural element names

			final String name = element.getLocalName();
			if (name.equals("h1") || name.equals("h2") || name.equals("h3") || name.equals("h4") || name.equals("h5") || name.equals("h6")) {
				return element;
			}

			element = element.getParentElement();
		}
		return element;
	}

	//===================================================== PRIVATE

	private final ITreeContentProvider contentProvider = new ITreeContentProvider() {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		}

		@Override
		public Object[] getChildren(final Object parentElement) {
			return getOutlineChildren((IElement) parentElement);
		}

		@Override
		public Object getParent(final Object element) {
			final IElement parent = ((IElement) element).getParentElement();
			if (parent == null) {
				return element;
			} else {
				return getOutlineElement(parent);
			}
		}

		@Override
		public boolean hasChildren(final Object element) {
			return getOutlineChildren((IElement) element).length > 0;
		}

		@Override
		public Object[] getElements(final Object inputElement) {
			final IDocument document = (IDocument) inputElement;
			return new Object[] { document.getRootElement() };
		}

	};

	/**
	 * Returns an array of the children of the given element that represent nodes in the outline. These are structural
	 * elements such as "section".
	 *
	 * @param element
	 * @return
	 */
	private IElement[] getOutlineChildren(final IElement element) {
		final List<IElement> children = new ArrayList<IElement>();
		if (element.getParent() == null) {
			final IElement body = findChild(element, "body");
			if (body != null) {
				final List<? extends IElement> childElements = body.childElements().asList();

				// First, find the lowest numbered h tag available
				String lowH = "h6";
				for (final IElement child : childElements) {
					if (isHTag(child) && child.getLocalName().compareTo(lowH) < 0) {
						lowH = child.getLocalName();
					}
				}

				// Now, get all body children at that level
				for (final IElement child : childElements) {
					if (child.getLocalName().equals(lowH)) {
						children.add(child);
					}
				}
			}
		} else if (isHTag(element)) {
			// get siblings with the next lower number
			// between this element and the next element at the same level
			final int level = Integer.parseInt(element.getLocalName().substring(1));
			final String childName = "h" + (level + 1);
			boolean foundSelf = false;
			for (final IElement child : element.getParentElement().childElements()) {
				if (child == element) {
					foundSelf = true;
				} else if (!foundSelf) {
					continue;
				} else if (child.getLocalName().equals(childName)) {
					children.add(child);
				} else if (child.getLocalName().equals(element.getLocalName())) {
					// terminate at next sibling at same level
					break;
				}
			}
		}
		return children.toArray(new IElement[children.size()]);
	}

	private final ILabelProvider labelProvider = new LabelProvider() {
		@Override
		public String getText(final Object o) {
			String text;
			final IElement element = (IElement) o;
			if (element.getParent() == null) {
				text = "html";
				final IElement head = findChild(element, "head");
				if (head != null) {
					final IElement title = findChild(head, "title");
					if (title != null) {
						text = title.getText();
					}
				}
			} else {
				text = element.getText();
			}
			return text;
		}
	};

	private IElement findChild(final IElement parent, final String childName) {
		for (final IElement child : parent.childElements().matching(Filters.elementsNamed(childName))) {
			return child;
		}
		return null;
	}

	private boolean isHTag(final IElement element) {
		final String name = element.getLocalName();
		return name.equals("h1") || name.equals("h2") || name.equals("h3") || name.equals("h4") || name.equals("h5") || name.equals("h6");
	}

}