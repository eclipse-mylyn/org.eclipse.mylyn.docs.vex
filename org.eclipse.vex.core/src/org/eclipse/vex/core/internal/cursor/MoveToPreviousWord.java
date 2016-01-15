package org.eclipse.vex.core.internal.cursor;

import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.widget.IViewPort;
import org.eclipse.vex.core.provisional.dom.IContent;

public class MoveToPreviousWord implements ICursorMove {

	@Override
	public int calculateNewOffset(final Graphics graphics, IViewPort viewPort, final ContentTopology contentTopology, final int currentOffset, final IContentBox currentBox, final Rectangle hotArea, final int preferredX) {
		final IContent content = currentBox.getContent();
		int offset = currentOffset;
		while (offset > 1 && Character.isLetterOrDigit(content.charAt(offset - 1))) {
			offset--;
		}

		while (offset > 1 && !Character.isLetterOrDigit(content.charAt(offset - 1))) {
			offset--;
		}
		return offset;
	}

	@Override
	public boolean preferX() {
		return true;
	}

	@Override
	public boolean isAbsolute() {
		return true;
	}

}
