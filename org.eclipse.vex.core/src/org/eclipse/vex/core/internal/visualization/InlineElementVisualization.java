/*******************************************************************************
 * Copyright (c) 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.visualization;

import static org.eclipse.vex.core.internal.boxes.BoxFactory.staticText;

import org.eclipse.vex.core.internal.boxes.IInlineBox;
import org.eclipse.vex.core.internal.boxes.InlineContainer;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.provisional.dom.IElement;

public class InlineElementVisualization extends NodeVisualization<IInlineBox> {
	private static final FontSpec TIMES_NEW_ROMAN = new FontSpec("Times New Roman", FontSpec.PLAIN, 20.0f);

	public InlineElementVisualization() {
		super(1);
	}

	@Override
	public IInlineBox visit(final IElement element) {
		if (!"b".equals(element.getLocalName())) {
			return super.visit(element);
		}

		final InlineContainer container = new InlineContainer();
		if (element.hasChildren()) {
			return visualizeChildrenInline(element.children(), container);
		} else {
			container.appendChild(staticText(" ", TIMES_NEW_ROMAN));
			return container;
		}
	}
}
