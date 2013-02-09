/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.ui.internal.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.vex.core.dom.IDocument;
import org.eclipse.vex.core.dom.IDocumentFragment;
import org.eclipse.vex.core.dom.IElement;
import org.eclipse.vex.core.dom.INode;
import org.eclipse.vex.core.internal.VEXCorePlugin;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.internal.dom.CopyOfElement;
import org.eclipse.vex.core.internal.widget.IVexWidget;
import org.eclipse.vex.ui.internal.swt.VexWidget;

/**
 * Splits the current block element, for instance to create new block/paragraph or table cell (usually by hitting the
 * {@code Return} key).
 * 
 * @see SplitItemHandler
 */
public class SplitBlockElementHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(final VexWidget widget) throws ExecutionException {
		IElement element = widget.getCurrentElement();
		if (element == null) {
			return; // we are not in an element, so we bail out here
		}
		Styles styles = widget.getStyleSheet().getStyles(element);
		while (!styles.isBlock()) {
			element = element.getParentElement();
			if (element == null || element.getParent() instanceof IDocument) {
				return; // we reached the root element which cannot be split 
			}
			styles = widget.getStyleSheet().getStyles(element);
		}
		splitElement(widget, element);
	}

	/**
	 * Splits the given element.
	 * 
	 * @param vexWidget
	 *            IVexWidget containing the document.
	 * @param node
	 *            Node to be split.
	 */
	protected void splitElement(final IVexWidget vexWidget, final INode node) {

		vexWidget.doWork(new Runnable() {
			public void run() {

				long start = 0;
				if (VEXCorePlugin.getInstance().isDebugging()) {
					start = System.currentTimeMillis();
				}

				final Styles styles = vexWidget.getStyleSheet().getStyles(node);

				if (styles.getWhiteSpace().equals(CSS.PRE)) {
					vexWidget.insertText("\n");
				} else {

					// There may be a number of child elements below the given
					// element. We cut out the tails of each of these elements
					// and put them in a list of fragments to be reconstructed
					// when
					// we clone the element.
					final List<IElement> children = new ArrayList<IElement>();
					final List<IDocumentFragment> fragments = new ArrayList<IDocumentFragment>();
					IElement child = vexWidget.getCurrentElement();
					while (true) {
						children.add(child);
						vexWidget.moveTo(child.getEndOffset(), true);
						fragments.add(vexWidget.getSelectedFragment());
						vexWidget.deleteSelection();
						vexWidget.moveTo(child.getEndOffset() + 1);
						if (child == node) {
							break;
						}
						child = child.getParentElement();
					}

					for (int i = children.size() - 1; i >= 0; i--) {
						child = children.get(i);
						final IDocumentFragment fragment = fragments.get(i);
						final IElement newChild = vexWidget.insertElement(child.getQualifiedName());
						newChild.accept(new CopyOfElement(child));
						final int offset = vexWidget.getCaretOffset();
						if (fragment != null) {
							vexWidget.insertFragment(fragment);
						}
						vexWidget.moveTo(offset);
					}
				}

				if (VEXCorePlugin.getInstance().isDebugging()) {
					final long end = System.currentTimeMillis();
					System.out.println("split() took " + (end - start) + "ms");
				}
			}
		});
	}
}
