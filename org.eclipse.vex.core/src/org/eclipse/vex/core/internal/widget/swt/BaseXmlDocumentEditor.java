package org.eclipse.vex.core.internal.widget.swt;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.visualization.IBoxModelBuilder;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;

public abstract class BaseXmlDocumentEditor extends Canvas implements IDocumentEditor, ISelectionProvider {

	public BaseXmlDocumentEditor(final Composite parent, final int style) {
		super(parent, style);
	}

	@Override
	public abstract IVexSelection getSelection();

	public abstract void setBoxModelBuilder(IBoxModelBuilder boxModelBuilder);

	public abstract Rectangle getCaretArea();

}
