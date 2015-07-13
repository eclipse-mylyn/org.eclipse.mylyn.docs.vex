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

/**
 * @author Florian Thienel
 */
public interface IBoxVisitorWithResult<T> {

	T visit(RootBox box);

	T visit(VerticalBlock box);

	T visit(Frame box);

	T visit(NodeReference box);

	T visit(HorizontalBar box);

	T visit(Paragraph box);

	T visit(InlineContainer box);

	T visit(StaticText box);

	T visit(TextContent box);

	T visit(Square box);

}
