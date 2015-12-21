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
public class BaseBoxVisitorWithResult<T> implements IBoxVisitorWithResult<T> {

	private final T defaultValue;

	public BaseBoxVisitorWithResult() {
		this(null);
	}

	public BaseBoxVisitorWithResult(final T defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public T visit(final RootBox box) {
		return defaultValue;
	}

	@Override
	public T visit(final VerticalBlock box) {
		return defaultValue;
	}

	@Override
	public T visit(final StructuralFrame box) {
		return defaultValue;
	}

	@Override
	public T visit(final StructuralNodeReference box) {
		return defaultValue;
	}

	@Override
	public T visit(final HorizontalBar box) {
		return defaultValue;
	}

	@Override
	public T visit(final Paragraph box) {
		return defaultValue;
	}

	@Override
	public T visit(final InlineNodeReference box) {
		return defaultValue;
	}

	@Override
	public T visit(final InlineContainer box) {
		return defaultValue;
	}

	@Override
	public T visit(final InlineFrame box) {
		return defaultValue;
	}

	@Override
	public T visit(final StaticText box) {
		return defaultValue;
	}

	@Override
	public T visit(final TextContent box) {
		return defaultValue;
	}

	@Override
	public T visit(final NodeEndOffsetPlaceholder box) {
		return defaultValue;
	}

	@Override
	public T visit(final Square box) {
		return defaultValue;
	}

}
