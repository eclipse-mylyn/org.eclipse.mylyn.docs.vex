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
import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.layout.Box;
import org.eclipse.vex.core.internal.widget.HostComponent;
import org.eclipse.vex.core.internal.widget.VexWidgetImpl;
import org.eclipse.vex.ui.internal.editor.VexEditor;
import org.eclipse.vex.ui.internal.swt.VexWidget;

/**
 * Page in the debug view.
 */
class DebugViewPage implements IPageBookViewPage {

	public DebugViewPage(final VexEditor vexEditor) {
		this.vexEditor = vexEditor;
	}

	public void createControl(final Composite parent) {

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		if (vexEditor.isLoaded()) {
			createDebugPanel();
		} else {
			loadingLabel = new Label(composite, SWT.NONE);
			loadingLabel.setText("Loading...");
		}

		vexEditor.getEditorSite().getSelectionProvider().addSelectionChangedListener(selectionListener);
	}

	public void dispose() {
		if (vexWidget != null && !vexWidget.isDisposed()) {
			vexWidget.removeMouseMoveListener(mouseMoveListener);
		}
		vexEditor.getEditorSite().getSelectionProvider().removeSelectionChangedListener(selectionListener);
	}

	public Control getControl() {
		return composite;
	}

	public IPageSite getSite() {
		return site;
	}

	public void init(final IPageSite site) throws PartInitException {
		this.site = site;
	}

	public void setActionBars(final IActionBars actionBars) {
	}

	public void setFocus() {
	}

	// ================================================== PRIVATE

	private static final int X = 1;
	private static final int Y = 2;
	private static final int WIDTH = 3;
	private static final int HEIGHT = 4;

	private static Field implField;
	private static Field rootBoxField;
	private static Field caretField;
	private static Field hostComponentField;

	static {
		try {
			implField = VexWidget.class.getDeclaredField("impl");
			implField.setAccessible(true);
			rootBoxField = VexWidgetImpl.class.getDeclaredField("rootBox");
			rootBoxField.setAccessible(true);
			caretField = VexWidgetImpl.class.getDeclaredField("caret");
			caretField.setAccessible(true);
			hostComponentField = VexWidgetImpl.class.getDeclaredField("hostComponent");
			hostComponentField.setAccessible(true);
		} catch (final Exception e) {
			// TODO: handle exception
		}
	}

	private IPageSite site;
	private final VexEditor vexEditor;
	private VexWidget vexWidget;
	private VexWidgetImpl impl;
	private Composite composite;

	private Label loadingLabel;

	private Table table;
	private TableItem documentItem;
	private TableItem viewportItem;
	private TableItem caretOffsetItem;
	private TableItem caretAbsItem;
	private TableItem caretRelItem;
	private TableItem mouseAbsItem;
	private TableItem mouseRelItem;

	private void createDebugPanel() {

		if (loadingLabel != null) {
			loadingLabel.dispose();
			loadingLabel = null;
		}

		vexWidget = vexEditor.getVexWidget();
		try {
			impl = (VexWidgetImpl) implField.get(vexWidget);
		} catch (final IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		GridData gd;

		final ScrolledComposite sc = new ScrolledComposite(composite, SWT.V_SCROLL);
		table = new Table(sc, SWT.NONE);
		table.setHeaderVisible(true);
		sc.setContent(table);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		sc.setLayoutData(gd);

		TableColumn column;
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Item");
		column = new TableColumn(table, SWT.RIGHT);
		column.setText("X");
		column = new TableColumn(table, SWT.RIGHT);
		column.setText("Y");
		column = new TableColumn(table, SWT.RIGHT);
		column.setText("Width");
		column = new TableColumn(table, SWT.RIGHT);
		column.setText("Height");

		table.addControlListener(controlListener);

		documentItem = new TableItem(table, SWT.NONE);
		documentItem.setText(0, "Document");
		viewportItem = new TableItem(table, SWT.NONE);
		viewportItem.setText(0, "Viewport");
		caretOffsetItem = new TableItem(table, SWT.NONE);
		caretOffsetItem.setText(0, "Caret Offset");
		caretAbsItem = new TableItem(table, SWT.NONE);
		caretAbsItem.setText(0, "Caret Abs.");
		caretRelItem = new TableItem(table, SWT.NONE);
		caretRelItem.setText(0, "Caret Rel.");
		mouseAbsItem = new TableItem(table, SWT.NONE);
		mouseAbsItem.setText(0, "Mouse Abs.");
		mouseRelItem = new TableItem(table, SWT.NONE);
		mouseRelItem.setText(0, "Mouse Rel.");

		final Button updateButton = new Button(composite, SWT.PUSH);
		updateButton.setText("Refresh");
		updateButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(final SelectionEvent e) {
				repopulate();
			}

			public void widgetDefaultSelected(final SelectionEvent e) {
			}
		});

		composite.layout();

		vexWidget.addMouseMoveListener(mouseMoveListener);

		repopulate();

	}

	private final ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		public void selectionChanged(final SelectionChangedEvent event) {
			if (vexWidget == null) {
				createDebugPanel();
			}
			repopulate();
		}
	};

	private final ControlListener controlListener = new ControlListener() {
		public void controlMoved(final ControlEvent e) {
		}

		public void controlResized(final ControlEvent e) {
			final int width = table.getSize().x;
			final int numWidth = Math.round(width * 0.125f);
			table.getColumn(0).setWidth(width / 2);
			table.getColumn(1).setWidth(numWidth);
			table.getColumn(2).setWidth(numWidth);
			table.getColumn(3).setWidth(numWidth);
		}
	};

	private final MouseMoveListener mouseMoveListener = new MouseMoveListener() {

		public void mouseMove(final MouseEvent e) {
			final Rectangle rect = new Rectangle(e.x, e.y, 0, 0);
			final Rectangle viewport = getViewport();
			setItemFromRect(mouseAbsItem, rect);
			setItemRel(mouseRelItem, viewport, rect);
		}

	};

	private Rectangle getCaretBounds() {
		final Caret caret = (Caret) getFieldValue(caretField, impl);
		return caret.getBounds();
	}

	private Rectangle getRootBoxBounds() {
		final Box rootBox = (Box) getFieldValue(rootBoxField, impl);
		return new Rectangle(rootBox.getX(), rootBox.getY(), rootBox.getWidth(), rootBox.getHeight());
	}

	private Rectangle getViewport() {
		final HostComponent hc = (HostComponent) getFieldValue(hostComponentField, impl);
		return hc.getViewport();
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
		final Rectangle viewport = getViewport();
		caretOffsetItem.setText(1, Integer.toString(impl.getCaretOffset()));
		setItemFromRect(viewportItem, viewport);
		setItemFromRect(caretAbsItem, getCaretBounds());
		setItemRel(caretRelItem, viewport, getCaretBounds());
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