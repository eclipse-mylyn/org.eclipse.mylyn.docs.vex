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

import static org.eclipse.vex.core.internal.boxes.BoxFactory.frame;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.nodeReferenceWithText;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.paragraph;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.staticText;

import org.eclipse.vex.core.internal.boxes.Border;
import org.eclipse.vex.core.internal.boxes.IStructuralBox;
import org.eclipse.vex.core.internal.boxes.Margin;
import org.eclipse.vex.core.internal.boxes.Padding;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.provisional.dom.IElement;

public final class ParagraphVisualization extends NodeVisualization<IStructuralBox> {
	private static final FontSpec TIMES_NEW_ROMAN = new FontSpec("Times New Roman", FontSpec.PLAIN, 20.0f);

	public ParagraphVisualization() {
		super(1);
	}

	@Override
	public IStructuralBox visit(final IElement element) {
		if (!"para".equals(element.getLocalName())) {
			return super.visit(element);
		}

		final Paragraph paragraph = visualizeParagraphElement(element);
		return nodeReferenceWithText(element, frame(paragraph, Margin.NULL, Border.NULL, new Padding(5, 4), null));
	}

	private Paragraph visualizeParagraphElement(final IElement element) {
		if (element.hasChildren()) {
			return visualizeElementWithChildren(element);
		} else {
			return visualizeEmptyElement(element);
		}
	}

	private Paragraph visualizeElementWithChildren(final IElement element) {
		final Paragraph paragraph = paragraph();
		visualizeChildrenInline(element.children(), paragraph);
		return paragraph;
	}

	private Paragraph visualizeEmptyElement(final IElement element) {
		final Paragraph paragraph = paragraph();
		paragraph.appendChild(staticText(" ", TIMES_NEW_ROMAN, Color.BLACK));
		return paragraph;
	}
}
