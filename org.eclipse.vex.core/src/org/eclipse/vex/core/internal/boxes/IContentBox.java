/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.boxes;

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IContent;

/**
 * @author Florian Thienel
 */
public interface IContentBox extends IChildBox {

	IContent getContent();

	int getStartOffset();

	int getEndOffset();

	ContentRange getRange();

	boolean isEmpty();

	boolean isAtStart(int offset);

	boolean isAtEnd(int offset);

	Rectangle getPositionArea(Graphics graphics, int offset);

	int getOffsetForCoordinates(Graphics graphics, int x, int y);

	void highlight(Graphics graphics, Color foreground, Color background);

}
