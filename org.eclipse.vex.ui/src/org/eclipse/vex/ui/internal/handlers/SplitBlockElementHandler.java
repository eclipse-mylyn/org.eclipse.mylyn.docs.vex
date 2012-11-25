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
import org.eclipse.vex.core.internal.VEXCorePlugin;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.internal.dom.CopyVisitor;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentFragment;
import org.eclipse.vex.core.internal.dom.Element;
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
		Element element = widget.getCurrentElement();
		Styles styles = widget.getStyleSheet().getStyles(element);
		while (!styles.isBlock()) {
			element = element.getParentElement();
			if (element == null || element.getParent() instanceof Document) {
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
	 * @param element
	 *            Element to be split.
	 */
	protected void splitElement(final IVexWidget vexWidget, final Element element) {

		vexWidget.doWork(new Runnable() {
			public void run() {

				long start = 0;
				if (VEXCorePlugin.getInstance().isDebugging()) {
					start = System.currentTimeMillis();
				}

				final Styles styles = vexWidget.getStyleSheet().getStyles(element);

				if (styles.getWhiteSpace().equals(CSS.PRE)) {
					// can't call vexWidget.insertText() or we'll get an
					// infinite loop
					final Document doc = vexWidget.getDocument();
					final int offset = vexWidget.getCaretOffset();
					doc.insertText(offset, "\n");
					vexWidget.moveTo(offset + 1);
				} else {

					// There may be a number of child elements below the given
					// element. We cut out the tails of each of these elements
					// and put them in a list of fragments to be reconstructed
					// when
					// we clone the element.
					final List<Element> children = new ArrayList<Element>();
					final List<DocumentFragment> frags = new ArrayList<DocumentFragment>();
					Element child = vexWidget.getCurrentElement();
					while (true) {
						children.add(child);
						vexWidget.moveTo(child.getEndOffset(), true);
						frags.add(vexWidget.getSelectedFragment());
						vexWidget.deleteSelection();
						vexWidget.moveTo(child.getEndOffset() + 1);
						if (child == element) {
							break;
						}
						child = child.getParentElement();
					}

					final CopyVisitor copyVisitor = new CopyVisitor();
					for (int i = children.size() - 1; i >= 0; i--) {
						child = children.get(i);
						final DocumentFragment frag = frags.get(i);
						child.accept(copyVisitor);
						vexWidget.insertElement(copyVisitor.<Element> getCopy());
						final int offset = vexWidget.getCaretOffset();
						if (frag != null) {
							vexWidget.insertFragment(frag);
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
