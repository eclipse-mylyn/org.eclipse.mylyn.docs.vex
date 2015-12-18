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
package org.eclipse.vex.core.provisional.dom;

import java.util.ArrayList;

public class MultilineText {

	private final ArrayList<Line> lines = new ArrayList<Line>();

	public void appendLine(final String text, final ContentRange range) {
		lines.add(new Line(text, range));
	}

	public int size() {
		return lines.size();
	}

	public String getText(final int lineIndex) {
		final Line line = lines.get(lineIndex);
		return line.text;
	}

	public ContentRange getRange(final int lineIndex) {
		final Line line = lines.get(lineIndex);
		return line.range;
	}

	private static class Line {
		public final String text;
		public final ContentRange range;

		public Line(final String text, final ContentRange range) {
			this.text = text;
			this.range = range;
		}
	}
}
