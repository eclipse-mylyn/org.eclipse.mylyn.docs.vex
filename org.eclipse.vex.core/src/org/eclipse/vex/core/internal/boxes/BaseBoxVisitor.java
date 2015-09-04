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
public class BaseBoxVisitor implements IBoxVisitor {

	@Override
	public void visit(final RootBox box) {
		// ignore
	}

	@Override
	public void visit(final VerticalBlock box) {
		// ignore
	}

	@Override
	public void visit(final Frame box) {
		// ignore
	}

	@Override
	public void visit(final StructuralNodeReference box) {
		// ignore
	}

	@Override
	public void visit(final HorizontalBar box) {
		// ignore
	}

	@Override
	public void visit(final Paragraph box) {
		// ignore
	}

	@Override
	public void visit(final InlineContainer box) {
		// ignore
	}

	@Override
	public void visit(final StaticText box) {
		// ignore
	}

	@Override
	public void visit(final TextContent box) {
		// ignore
	}

	@Override
	public void visit(final Square box) {
		// ignore
	}
}
