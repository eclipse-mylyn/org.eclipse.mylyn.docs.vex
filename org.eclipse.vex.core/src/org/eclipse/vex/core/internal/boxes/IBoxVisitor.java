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
public interface IBoxVisitor {

	void visit(RootBox box);

	void visit(VerticalBlock box);

	void visit(Frame box);

	void visit(HorizontalBar box);

	void visit(Paragraph box);

	void visit(StaticText box);

	void visit(TextContent box);

	void visit(Square box);
}
