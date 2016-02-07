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

	T visit(StructuralFrame box);

	T visit(StructuralNodeReference box);

	T visit(HorizontalBar box);

	T visit(ListItem box);

	T visit(Paragraph box);

	T visit(InlineNodeReference box);

	T visit(InlineContainer box);

	T visit(InlineFrame box);

	T visit(StaticText box);

	T visit(Image box);

	T visit(TextContent box);

	T visit(NodeEndOffsetPlaceholder box);

	T visit(Square box);

	T visit(NodeTag box);
}
