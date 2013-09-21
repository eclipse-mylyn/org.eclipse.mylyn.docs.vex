/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import java.util.NoSuchElementException;

import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.Filters;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.w3c.css.sac.LexicalUnit;

/**
 * The -vex-content-outline CSS property. This property decides which content is shown in the outline view.<br />
 * The value is either the name of a child element, an attribute or 'none'. As default the elements text content is used
 * in the outline display.<br />
 * Theis property can hold several element names as a "fallback" system. 'none' can be used to prevent the default usage
 * of the element's text content.<br />
 * <br />
 * Example:
 * 
 * <pre>
 * chapter {
 *   -vex-outline-content: titleabbrev, title;
 * }
 * </pre>
 * 
 */
public class OutlineContentProperty extends AbstractProperty {

	/**
	 * Class constructor.
	 */
	public OutlineContentProperty() {
		super(CSS.OUTLINE_CONTENT);
	}

	/**
	 * Returns the node, which text content (or value, if the node is an attribute) should be used in the outline
	 * display as content for the given node. We don't return the actual text here, because then we had to recalculate
	 * the styles every time the text changes.
	 */
	public Object calculate(final LexicalUnit lu, final Styles parentStyles, final Styles styles, final INode node) {

		if (node == null || !node.isAssociated()) {
			return null;
		}

		return node.accept(new BaseNodeVisitorWithResult<Object>(null) {
			@Override
			public Object visit(final IElement element) {
				LexicalUnit lexicalUnit = lu;
				while (lexicalUnit != null) {
					if (lexicalUnit.getLexicalUnitType() == LexicalUnit.SAC_ATTR) {
						final String attrName = lexicalUnit.getStringValue();
						final String attrValue = element.getAttributeValue(attrName);
						if (attrValue != null) {
							return element.getAttribute(attrName);
						}
					} else if (lexicalUnit.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
						if (lexicalUnit.getStringValue().equals(CSS.NONE)) {
							return null;
						}
						// lu is the name of a possible child
						final String childName = lexicalUnit.getStringValue();
						try {
							final IElement childElement = element.childElements().matching(Filters.elementsNamed(childName)).first();
							if (!childElement.getText().isEmpty()) {
								return childElement;
							}
						} catch (final NoSuchElementException ex) {
						}
					}

					lexicalUnit = lexicalUnit.getNextLexicalUnit();
				}

				// Return element text content as default
				return element;
			}
			
			@Override
			public Object visit(IProcessingInstruction pi) {
				return pi;
			}
		});
	}
}
