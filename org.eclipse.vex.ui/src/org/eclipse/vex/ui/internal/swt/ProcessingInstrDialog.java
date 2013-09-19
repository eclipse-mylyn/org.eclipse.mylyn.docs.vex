/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.swt;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.vex.core.IValidationResult;
import org.eclipse.vex.core.XML;
import org.eclipse.vex.ui.internal.Messages;
import org.eclipse.vex.ui.internal.PluginImages;

/**
 * The edit dialog for processing instruction targets.
 * 
 * @author chi
 */
public class ProcessingInstrDialog extends TitleAreaDialog {
	private Text textTarget;

	private String target;

	private boolean targetValid = true;
	protected Color fgColor;

	/**
	 * Create the dialog.
	 * 
	 * @param widget
	 * @wbp.parser.constructor
	 */
	public ProcessingInstrDialog(final Shell shell) {
		this(shell, "");
	}

	public ProcessingInstrDialog(final Shell shell, final String target) {
		super(shell);
		setHelpAvailable(false);
		this.target = target;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		setTitle(Messages.getString("ProcessingInstrDialog.Title"));
		final Composite area = (Composite) super.createDialogArea(parent);
		final Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new FormLayout());
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		final Label lblTarget = new Label(container, SWT.NONE);
		final FormData fd_lblTarget = new FormData();
		fd_lblTarget.top = new FormAttachment(0, 10);
		fd_lblTarget.left = new FormAttachment(0, 10);
		lblTarget.setLayoutData(fd_lblTarget);
		lblTarget.setText(Messages.getString("ProcessingInstrDialog.targetLabel"));

		textTarget = new Text(container, SWT.BORDER);
		fgColor = textTarget.getForeground(); // Store foreground color
		textTarget.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				evaluateTarget();
			}
		});

		final FormData fd_textTarget = new FormData();
		fd_textTarget.right = new FormAttachment(100, -10);
		fd_textTarget.left = new FormAttachment(lblTarget, 26);
		fd_textTarget.bottom = new FormAttachment(100, -10);
		fd_textTarget.top = new FormAttachment(0, 7);
		textTarget.setLayoutData(fd_textTarget);

		return area;
	}

	/**
	 * @return The target entered by the user
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		// this method is called after createDialogArea, so we do the initialization here
		textTarget.setText(target);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 185);
	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.getString("ProcessingInstrDialog.DialogTitle"));
		shell.setImage(PluginImages.get(PluginImages.IMG_VEX_ICON));
	}

	@Override
	protected void okPressed() {
		evaluateTarget();
		if (targetValid) {
			super.okPressed();
		}
	}

	private void evaluateTarget() {
		final String text = textTarget.getText();

		final IValidationResult resultTarget = XML.validateProcessingInstructionTarget(text);
		if (resultTarget.isOK()) {
			if (!targetValid) {
				targetValid = true;
				getButton(IDialogConstants.OK_ID).setEnabled(targetValid);
				setErrorMessage(null);
				textTarget.setForeground(fgColor);
				textTarget.redraw();
			}
			target = text;
		} else {
			targetValid = false;
			getButton(IDialogConstants.OK_ID).setEnabled(targetValid);
			setErrorMessage(text.isEmpty() ? null : resultTarget.getMessage());
			textTarget.setForeground(JFaceColors.getErrorText(getShell().getDisplay()));
			textTarget.redraw();
		}
	}
}
