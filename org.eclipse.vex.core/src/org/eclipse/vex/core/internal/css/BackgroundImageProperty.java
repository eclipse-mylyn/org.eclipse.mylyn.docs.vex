/*******************************************************************************
 * Copyright (c) 2010  Mohamadou Nassourou and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		 Mohamadou Nassourou - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.vex.core.internal.VEXCorePlugin;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.w3c.css.sac.LexicalUnit;

/**
 * @author Mohamadou Nassourou
 */
public class BackgroundImageProperty extends AbstractProperty {

	public static final String DEFAULT = null;

	public BackgroundImageProperty() {
		super(CSS.BACKGROUND_IMAGE);
	}

	@Override
	public Object calculate(final LexicalUnit lexicalUnit, final Styles parentStyles, final Styles styles, final INode node) {
		if (lexicalUnit == null) {
			return DEFAULT;
		}
		switch (lexicalUnit.getLexicalUnitType()) {
		case LexicalUnit.SAC_STRING_VALUE:
			return lexicalUnit.getStringValue();
		case LexicalUnit.SAC_ATTR:
			return node.accept(new BaseNodeVisitorWithResult<Object>(DEFAULT) {
				@Override
				public Object visit(final IElement element) {
					final String attributeValue = element.getAttributeValue(lexicalUnit.getStringValue());
					if (attributeValue != null) {
						return attributeValue;
					}
					return DEFAULT;
				}
			});
		default:
			VEXCorePlugin
					.getInstance()
					.getLog()
					.log(new Status(IStatus.WARNING, VEXCorePlugin.ID, MessageFormat.format("Unsupported lexical unit type in ''background-image: {0}'' (type: {1})", lexicalUnit.toString(),
							lexicalUnit.getLexicalUnitType())));
			return DEFAULT;
		}
	}
}
