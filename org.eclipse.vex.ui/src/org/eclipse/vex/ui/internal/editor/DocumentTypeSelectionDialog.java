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
package org.eclipse.vex.ui.internal.editor;

import java.text.MessageFormat;
import java.util.Arrays;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.vex.ui.internal.Messages;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.config.DocumentType;

/**
 * Dialog presented to the user to select a document type it cannot be determined from the document being opened.
 */
public class DocumentTypeSelectionDialog extends MessageDialog {

	/**
	 * Class constructor.
	 * 
	 * @param parentShell
	 *            Parent Shell with respect to which this dialog is modal.
	 */
	protected DocumentTypeSelectionDialog(final Shell parentShell, final String title, final String message) {
		super(parentShell, title, null, message, MessageDialog.QUESTION,
				new String[] { Messages.getString("DocumentTypeSelectionDialog.ok"), Messages.getString("DocumentTypeSelectionDialog.cancel") }, 0); //$NON-NLS-1$ //$NON-NLS-2$
		setShellStyle(SWT.RESIZE);
	}

	/**
	 * Creates a new instance of the dialog. The caller must call open() on the returned dialog to prompt the user. The
	 * open() method blocks until the user has closed the window. Once open() returns, the caller should call
	 * getDoctype() to get the selected doctype (or null if the dialog was canceled.
	 * 
	 * @param parentShell
	 *            Parent Shell of the dialog.
	 * @param publicId
	 *            Public ID of the document being opened, or null if the document does not have a PUBLIC DOCTYPE
	 *            declaration.
	 */
	public static DocumentTypeSelectionDialog create(final Shell parentShell, final String publicId) {
		String message;

		if (publicId == null) {
			message = Messages.getString("DocumentTypeSelectionDialog.noDoctype"); //$NON-NLS-1$
		} else {
			message = Messages.getString("DocumentTypeSelectionDialog.unknownDoctype"); //$NON-NLS-1$
		}

		return new DocumentTypeSelectionDialog(parentShell, Messages.getString("DocumentTypeSelectionDialog.selectDoctype"), //$NON-NLS-1$
				MessageFormat.format(message, new Object[] { publicId }));
	}

	@Override
	protected Control createCustomArea(final Composite parent) {

		typeList = new ListViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		final List list = typeList.getList();

		list.addMouseListener(mouseListener);

		final GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;
		gd.heightHint = 120;
		list.setLayoutData(gd);

		alwaysUseButton = new Button(parent, SWT.CHECK);
		alwaysUseButton.setText(Messages.getString("DocumentTypeSelectionDialog.alwaysUse")); //$NON-NLS-1$

		final DocumentType[] doctypes = VexPlugin.getDefault().getConfigurationRegistry().getDocumentTypesWithStyles();
		Arrays.sort(doctypes);
		typeList.add(doctypes);

		return list;
	}

	@Override
	protected void buttonPressed(final int buttonId) {
		if (buttonId == 0) {
			final IStructuredSelection selection = (IStructuredSelection) typeList.getSelection();
			doctype = (DocumentType) selection.getFirstElement();
			alwaysUseThisDoctype = alwaysUseButton.getSelection();
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Returns the document type selected by the user, or null if none was selected.
	 */
	public DocumentType getDoctype() {
		return doctype;
	}

	/**
	 * Returns true if Vex should always use this document type for the selected file.
	 */
	public boolean alwaysUseThisDoctype() {
		return alwaysUseThisDoctype;
	}

	// ======================================================= PRIVATE

	private DocumentType doctype;
	private boolean alwaysUseThisDoctype;

	private ListViewer typeList;
	private Button alwaysUseButton;

	private final MouseListener mouseListener = new MouseListener() {
		public void mouseDoubleClick(final MouseEvent e) {
			buttonPressed(0);
		}

		public void mouseDown(final MouseEvent e) {
		}

		public void mouseUp(final MouseEvent e) {
		}
	};

}
