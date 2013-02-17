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
package org.eclipse.vex.core.internal.css;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.vex.core.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.dom.IElement;
import org.eclipse.vex.core.dom.INode;
import org.eclipse.vex.core.internal.VEXCorePlugin;
import org.eclipse.vex.core.internal.core.DisplayDevice;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Parser;

/**
 * A property that represents lengths, such as a margin or padding.
 */
public class LengthProperty extends AbstractProperty {

	private final Axis axis;

	public LengthProperty(final String name, final Axis axis) {
		super(name);
		this.axis = axis;
	}

	public Object calculate(final LexicalUnit lu, final Styles parentStyles, final Styles styles, final INode node) {
		final int ppi = getPpi();
		if (isAttr(lu)) {
			return node.accept(new BaseNodeVisitorWithResult<Object>(RelativeLength.createAbsolute(0)) {
				@Override
				public Object visit(final IElement element) {
					return calculate(parseAttribute(lu, element), parentStyles, styles, element);
				}
			});
		}
		if (isLength(lu)) {
			final int length = getIntLength(lu, styles.getFontSize(), ppi);
			return RelativeLength.createAbsolute(length);
		} else if (isPercentage(lu)) {
			return RelativeLength.createRelative(lu.getFloatValue() / 100);
		} else if (isInherit(lu) && parentStyles != null) {
			return parentStyles.get(getName());
		} else {
			// not specified, "auto", or other unknown value
			return RelativeLength.createAbsolute(0);
		}
	}

	private static boolean isAttr(final LexicalUnit lexicalUnit) {
		return lexicalUnit != null && lexicalUnit.getLexicalUnitType() == LexicalUnit.SAC_ATTR;
	}

	private static LexicalUnit parseAttribute(final LexicalUnit lexicalUnit, final IElement element) {
		final String attributeName = lexicalUnit.getStringValue();
		final String attributeValue = element.getAttributeValue(attributeName);
		if (attributeValue == null) {
			return null;
		}
		final Parser parser = StyleSheetReader.createParser();
		try {
			return parser.parsePropertyValue(new InputSource(new StringReader(attributeValue)));
		} catch (final CSSException e) {
			VEXCorePlugin.getInstance().getLog().log(new Status(IStatus.ERROR, VEXCorePlugin.ID, e.getMessage(), e));
			return null;
		} catch (final IOException e) {
			VEXCorePlugin.getInstance().getLog().log(new Status(IStatus.ERROR, VEXCorePlugin.ID, e.getMessage(), e));
			return null;
		}
	}

	private int getPpi() {
		final DisplayDevice device = DisplayDevice.getCurrent();
		final int ppi = axis == Axis.HORIZONTAL ? device.getHorizontalPPI() : device.getVerticalPPI();
		return ppi;
	}
}
