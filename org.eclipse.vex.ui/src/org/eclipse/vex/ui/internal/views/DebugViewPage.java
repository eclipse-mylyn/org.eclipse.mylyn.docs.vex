/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.views;

import java.lang.reflect.Field;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.layout.Box;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.ui.internal.editor.VexEditor;

/**
 * Page in the debug view.
 */
class DebugViewPage implements IPageBookViewPage {

	public DebugViewPage(final VexEditor vexEditor) {
		editorPart = vexEditor;
	}

	@Override
	public void createControl(final Composite parent) {

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		if (editorPart.isLoaded()) {
			createDebugPanel();
		} else {
			loadingLabel = new Label(composite, SWT.NONE);
			loadingLabel.setText("Loading...");
		}

		editorPart.getEditorSite().getSelectionProvider().addSelectionChangedListener(selectionListener);
	}

	@Override
	public void dispose() {
		// TODO find this information elsewhere
		//		if (documentEditor != null && !documentEditor.isDisposed()) {
		//			documentEditor.removeMouseMoveListener(mouseMoveListener);
		//		}
		editorPart.getEditorSite().getSelectionProvider().removeSelectionChangedListener(selectionListener);
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public IPageSite getSite() {
		return site;
	}

	@Override
	public void init(final IPageSite site) throws PartInitException {
		this.site = site;
	}

	@Override
	public void setActionBars(final IActionBars actionBars) {
	}

	@Override
	public void setFocus() {
	}

	// ================================================== PRIVATE

	private static final int X = 1;
	private static final int Y = 2;
	private static final int WIDTH = 3;
	private static final int HEIGHT = 4;

	// TODO find this information elsewhere
	//	private static Field implField;
	//	private static Field rootBoxField;
	//	private static Field caretField;
	//	private static Field hostComponentField;
	//	private static Method findInnermostBoxMethod;
	//
	//	static {
	//		try {
	//			implField = VexWidget.class.getDeclaredField("impl");
	//			implField.setAccessible(true);
	//			rootBoxField = BaseVexWidget.class.getDeclaredField("rootBox");
	//			rootBoxField.setAccessible(true);
	//			caretField = BaseVexWidget.class.getDeclaredField("caret");
	//			caretField.setAccessible(true);
	//			hostComponentField = BaseVexWidget.class.getDeclaredField("hostComponent");
	//			hostComponentField.setAccessible(true);
	//			findInnermostBoxMethod = BaseVexWidget.class.getDeclaredMethod("findInnermostBox", IBoxFilter.class);
	//			findInnermostBoxMethod.setAccessible(true);
	//		} catch (final Exception e) {
	//			e.printStackTrace();
	//			// TODO: handle exception
	//		}
	//	}

	private IPageSite site;
	private final VexEditor editorPart;
	private IDocumentEditor documentEditor;
	private Composite composite;

	private Label loadingLabel;

	private Composite content;
	private Table table;
	private Table textTable;
	private TableItem documentItem;
	private TableItem viewportItem;
	private TableItem boxItem;
	private TableItem caretOffsetItem;
	private TableItem caretOffsetContentItem;
	private TableItem caretAbsItem;
	private TableItem caretRelItem;
	private TableItem mouseAbsItem;
	private TableItem mouseRelItem;

	private void createDebugPanel() {

		if (loadingLabel != null) {
			loadingLabel.dispose();
			loadingLabel = null;
		}

		documentEditor = editorPart.getVexWidget();
		//		try {
		//			impl = (BaseVexWidget) implField.get(vexWidget);
		//		} catch (final IllegalArgumentException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (final IllegalAccessException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		final GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);

		final ScrolledComposite sc = new ScrolledComposite(composite, SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		final GridDataFactory dataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).hint(SWT.DEFAULT, 50).grab(true, true);
		dataFactory.applyTo(sc);

		content = new Composite(sc, SWT.NONE);
		final GridLayout contentLayout = new GridLayout(1, false);
		content.setLayout(contentLayout);

		sc.setContent(content);

		table = new Table(content, SWT.NO_SCROLL);
		table.setHeaderVisible(true);
		final GridData tableGrid = new GridData();
		tableGrid.grabExcessHorizontalSpace = true;
		tableGrid.horizontalAlignment = GridData.FILL;
		table.setLayoutData(tableGrid);

		TableColumn column;
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Item");
		column = new TableColumn(table, SWT.LEFT);
		column.setText("X");
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Y");
		column = new TableColumn(table, SWT.RIGHT);
		column.setText("Width");
		column = new TableColumn(table, SWT.RIGHT);
		column.setText("Height");

		documentItem = new TableItem(table, SWT.NONE);
		documentItem.setText(0, "Document");
		viewportItem = new TableItem(table, SWT.NONE);
		viewportItem.setText(0, "Viewport");
		caretAbsItem = new TableItem(table, SWT.NONE);
		caretAbsItem.setText(0, "Caret Abs.");
		caretRelItem = new TableItem(table, SWT.NONE);
		caretRelItem.setText(0, "Caret Rel.");
		mouseAbsItem = new TableItem(table, SWT.NONE);
		mouseAbsItem.setText(0, "Mouse Abs.");
		mouseRelItem = new TableItem(table, SWT.NONE);
		mouseRelItem.setText(0, "Mouse Rel.");

		textTable = new Table(content, SWT.NO_SCROLL);
		textTable.setHeaderVisible(true);
		final GridData textTableGrid = new GridData();
		textTableGrid.grabExcessHorizontalSpace = true;
		textTableGrid.horizontalAlignment = GridData.FILL;
		textTable.setLayoutData(textTableGrid);
		textTable.setSize(textTable.computeSize(SWT.DEFAULT, 300));

		textTable.addControlListener(controlListener);

		column = new TableColumn(textTable, SWT.LEFT);
		column.setText("Item");
		column = new TableColumn(textTable, SWT.LEFT);
		column.setText("Value");

		caretOffsetItem = new TableItem(textTable, SWT.NONE);
		caretOffsetItem.setText(0, "Caret Offset");
		boxItem = new TableItem(textTable, SWT.NONE);
		boxItem.setText(0, "Current Box");
		caretOffsetContentItem = new TableItem(textTable, SWT.NONE);
		caretOffsetContentItem.setText(0, "Content at Caret");

		content.setSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sc.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		final Button updateButton = new Button(composite, SWT.PUSH);
		updateButton.setText("Refresh");
		updateButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				repopulate();
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
		});

		composite.layout();

		// TODO find this information elsewhere
		//		documentEditor.addMouseMoveListener(mouseMoveListener);

		repopulate();
	}

	private final ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(final SelectionChangedEvent event) {
			if (documentEditor == null) {
				createDebugPanel();
			}
			repopulate();
		}
	};

	private final ControlListener controlListener = new ControlListener() {
		@Override
		public void controlMoved(final ControlEvent e) {
		}

		@Override
		public void controlResized(final ControlEvent e) {
			resizeTables();
		}
	};

	private void resizeTables() {
		table.getColumn(0).pack();
		int width = table.getSize().x - table.getColumn(0).getWidth();
		final int numWidth = Math.round(width / 4);
		table.getColumn(1).setWidth(numWidth);
		table.getColumn(2).setWidth(numWidth);
		table.getColumn(3).setWidth(numWidth);
		table.getColumn(4).setWidth(numWidth);

		textTable.getColumn(0).pack();
		width = textTable.getSize().x - textTable.getColumn(0).getWidth();
		textTable.getColumn(1).setWidth(width);
	}

	private final MouseMoveListener mouseMoveListener = new MouseMoveListener() {
		@Override
		public void mouseMove(final MouseEvent e) {
			final Rectangle rect = new Rectangle(e.x, e.y, 0, 0);
			final Rectangle viewport = getViewport();
			setItemFromRect(mouseAbsItem, rect);
			setItemRel(mouseRelItem, viewport, rect);
		}
	};

	private Rectangle getCaretBounds() {
		// TODO find this information elsewhere
		//		final Caret caret = (Caret) getFieldValue(caretField, impl);
		//		return caret.getBounds();
		return Rectangle.NULL;
	}

	private Rectangle getRootBoxBounds() {
		// TODO find this information elsewhere
		//		final Box rootBox = (Box) getFieldValue(rootBoxField, impl);
		//		return new Rectangle(rootBox.getX(), rootBox.getY(), rootBox.getWidth(), rootBox.getHeight());
		return Rectangle.NULL;
	}

	private Rectangle getViewport() {
		// TODO find this information elsewhere
		//		final IHostComponent hc = (IHostComponent) getFieldValue(hostComponentField, impl);
		//		return hc.getViewport();
		return Rectangle.NULL;
	}

	private Object getFieldValue(final Field field, final Object o) {
		try {
			return field.get(o);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void repopulate() {
		setItemFromRect(documentItem, getRootBoxBounds());
		setFromInnermostBox(boxItem, getInnermostBox());
		final Rectangle viewport = getViewport();
		caretOffsetItem.setText(1, documentEditor.getCaretPosition().toString());
		caretOffsetContentItem.setText(1, getContent());
		setItemFromRect(viewportItem, viewport);
		setItemFromRect(caretAbsItem, getCaretBounds());
		setItemRel(caretRelItem, viewport, getCaretBounds());
	}

	private static void setFromInnermostBox(final TableItem item, final Box innermostBox) {
		if (innermostBox == null) {
			item.setText(1, "n/a");
			return;
		}

		item.setText(1, innermostBox.getClass().getSimpleName() + ": " + innermostBox);
	}

	private Box getInnermostBox() {
		// TODO find this information elsewhere
		//		try {
		//			return (Box) findInnermostBoxMethod.invoke(impl, IBoxFilter.TRUE);
		//		} catch (final IllegalArgumentException e) {
		//			return null;
		//		} catch (final IllegalAccessException e) {
		//			return null;
		//		} catch (final InvocationTargetException e) {
		//			return null;
		//		}
		return null;
	}

	private String getContent() {
		final ContentPosition offset = documentEditor.getCaretPosition();
		final IDocument doc = documentEditor.getDocument();
		final int len = 8;

		final StringBuilder result = new StringBuilder();
		final ContentRange range = new ContentRange(offset.moveBy(-len), offset.moveBy(len)).intersection(doc.getRange());
		final String content = doc.getContent().getRawText(range);

		final int caretIndex = offset.getOffset() - range.getStartOffset();

		result.append(content.substring(0, caretIndex).replace("\0", "#"));
		result.append("|");
		result.append(content.substring(caretIndex, content.length()).replace("\0", "#"));
		return result.toString();
	}

	private static void setItemFromRect(final TableItem item, final Rectangle rect) {
		item.setText(X, Integer.toString(rect.getX()));
		item.setText(Y, Integer.toString(rect.getY()));
		item.setText(WIDTH, Integer.toString(rect.getWidth()));
		item.setText(HEIGHT, Integer.toString(rect.getHeight()));
	}

	private static void setItemRel(final TableItem item, final Rectangle viewport, final Rectangle rect) {
		item.setText(X, Integer.toString(rect.getX() - viewport.getX()));
		item.setText(Y, Integer.toString(rect.getY() - viewport.getY()));
		item.setText(WIDTH, Integer.toString(rect.getWidth()));
		item.setText(HEIGHT, Integer.toString(rect.getHeight()));
	}

}