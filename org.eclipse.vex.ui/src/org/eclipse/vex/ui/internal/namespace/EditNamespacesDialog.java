/*******************************************************************************
 * Copyright (c) 2011 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.namespace;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Florian Thienel
 */
public class EditNamespacesDialog extends TitleAreaDialog {

	private final EditNamespacesController controller;
	private Text defaultNamespaceText;
	private TableViewer namespacesTable;

	public EditNamespacesDialog(final Shell parentShell, final EditNamespacesController controller) {
		super(parentShell);
		this.controller = controller;
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Edit Namespaces");
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	@Override
	protected Point getInitialSize() {
		final Point shellSize = super.getInitialSize();
		return new Point(shellSize.x, Math.max(convertVerticalDLUsToPixels(200), shellSize.y));
	}

	@Override
	protected Control createContents(final Composite parent) {
		final Composite result = (Composite) super.createContents(parent);
		setTitle("Edit Namespaces");
		setMessage("Edit the namespaces of the selected element.");
		return result;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite superRoot = (Composite) super.createDialogArea(parent);
		final Composite root = new Composite(superRoot, SWT.NONE);
		root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		root.setLayout(new GridLayout(2, false));

		final Label defaultNamespaceLabel = new Label(root, SWT.NONE);
		defaultNamespaceLabel.setText("Default Namespace");
		defaultNamespaceLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		defaultNamespaceText = new Text(root, SWT.SINGLE | SWT.BORDER);
		defaultNamespaceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		defaultNamespaceText.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				controller.setDefaultNamespaceURI(defaultNamespaceText.getText());
			}
		});

		new Label(root, SWT.NONE); // just a placeholder

		final Label namespaceTableLabel = new Label(root, SWT.NONE);
		namespaceTableLabel.setText("Other Namespaces");
		namespaceTableLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		final Composite tableRoot = new Composite(root, SWT.NONE);
		tableRoot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));
		final TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableRoot.setLayout(tableColumnLayout);

		namespacesTable = new TableViewer(tableRoot, SWT.BORDER | SWT.FULL_SELECTION);
		namespacesTable.getTable().setHeaderVisible(true);
		namespacesTable.getTable().setLinesVisible(true);
		namespacesTable.setContentProvider(ArrayContentProvider.getInstance());

		final TableViewerColumn prefixColumn = new TableViewerColumn(namespacesTable, SWT.NONE);
		prefixColumn.getColumn().setText("Prefix");
		prefixColumn.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				cell.setText(((EditableNamespaceDefinition) cell.getElement()).getPrefix());
			}
		});
		prefixColumn.setEditingSupport(new EditingSupport(namespacesTable) {
			@Override
			protected CellEditor getCellEditor(final Object element) {
				return new TextCellEditor(namespacesTable.getTable());
			}

			@Override
			protected boolean canEdit(final Object element) {
				return true;
			}

			@Override
			protected Object getValue(final Object element) {
				return ((EditableNamespaceDefinition) element).getPrefix();
			}

			@Override
			protected void setValue(final Object element, final Object value) {
				if (value != null) {
					((EditableNamespaceDefinition) element).setPrefix(value.toString());
				} else {
					((EditableNamespaceDefinition) element).setPrefix("");
				}
				namespacesTable.refresh(element);
			}
		});

		final TableViewerColumn uriColumn = new TableViewerColumn(namespacesTable, SWT.NONE);
		uriColumn.getColumn().setText("URI");
		uriColumn.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				cell.setText(((EditableNamespaceDefinition) cell.getElement()).getUri());
			}
		});
		uriColumn.setEditingSupport(new EditingSupport(namespacesTable) {
			@Override
			protected CellEditor getCellEditor(final Object element) {
				return new TextCellEditor(namespacesTable.getTable());
			}

			@Override
			protected boolean canEdit(final Object element) {
				return true;
			}

			@Override
			protected Object getValue(final Object element) {
				return ((EditableNamespaceDefinition) element).getUri();
			}

			@Override
			protected void setValue(final Object element, final Object value) {
				if (value != null) {
					((EditableNamespaceDefinition) element).setUri(value.toString());
				} else {
					((EditableNamespaceDefinition) element).setUri("");
				}
				namespacesTable.refresh(element);
			}
		});

		tableColumnLayout.setColumnData(prefixColumn.getColumn(), new ColumnWeightData(1, 100, true));
		tableColumnLayout.setColumnData(uriColumn.getColumn(), new ColumnWeightData(3, 100, true));

		final Button addNamespaceButton = new Button(root, SWT.PUSH);
		addNamespaceButton.setText("Add");
		addNamespaceButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		addNamespaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				addNamespacePressed();
			}
		});

		final Button removeNamespaceButton = new Button(root, SWT.PUSH);
		removeNamespaceButton.setText("Remove");
		removeNamespaceButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		removeNamespaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeNamespacePressed();
			}
		});

		populateFromController();

		return superRoot;
	}

	private void populateFromController() {
		defaultNamespaceText.setText(controller.getDefaultNamespaceURI());
		namespacesTable.setInput(controller.getNamespaceDefinitions());
	}

	private void addNamespacePressed() {
		final EditableNamespaceDefinition newDefinition = controller.addNamespaceDefinition();
		namespacesTable.refresh();
		namespacesTable.setSelection(new StructuredSelection(newDefinition), true);
	}

	private void removeNamespacePressed() {
		final IStructuredSelection selection = (IStructuredSelection) namespacesTable.getSelection();
		if (selection.isEmpty()) {
			return;
		}
		final EditableNamespaceDefinition selectedDefinition = (EditableNamespaceDefinition) selection.getFirstElement();
		controller.removeNamespaceDefinition(selectedDefinition);
		namespacesTable.refresh();
	}
}
