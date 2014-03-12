/*******************************************************************************
 * Copyright (c) 2004, 2014 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - fix illegal inheritance from IPage
 *******************************************************************************/
package org.eclipse.vex.ui.internal.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.vex.ui.internal.Messages;
import org.eclipse.vex.ui.internal.editor.VexEditor;

public class DebugView extends PageBookView {

	@Override
	protected IPage createDefaultPage(final PageBook book) {
		final Page page = new Page() {
			@Override
			public void createControl(final Composite parent) {
				label = new Label(parent, SWT.NONE);
				label.setText(Messages.getString("DebugView.noActiveEditor")); //$NON-NLS-1$
			}

			@Override
			public void dispose() {
			}

			@Override
			public Control getControl() {
				return label;
			}

			@Override
			public IPageSite getSite() {
				return site;
			}

			@Override
			public void init(final IPageSite site) {
				this.site = site;
			}

			@Override
			public void setActionBars(final IActionBars actionBars) {
			}

			@Override
			public void setFocus() {
			}

			private IPageSite site;
			private Label label;
		};

		initPage(page);
		page.createControl(getPageBook());
		return page;
	}

	@Override
	protected PageRec doCreatePage(final IWorkbenchPart part) {
		final DebugViewPage page = new DebugViewPage((VexEditor) part);
		initPage(page);
		page.createControl(getPageBook());
		return new PageRec(part, page);
	}

	@Override
	protected void doDestroyPage(final IWorkbenchPart part, final PageRec pageRecord) {
		pageRecord.page.dispose();
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		return null;
	}

	@Override
	protected boolean isImportant(final IWorkbenchPart part) {
		return part instanceof VexEditor;
	}
}
