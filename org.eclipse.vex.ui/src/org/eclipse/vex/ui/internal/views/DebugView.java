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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.vex.ui.internal.editor.Messages;
import org.eclipse.vex.ui.internal.editor.VexEditor;

/**
 * A view that shows stats about the current Vex editor as a debugging aid.
 */
public class DebugView extends PageBookView {

	@Override
	protected IPage createDefaultPage(final PageBook book) {
		final IPageBookViewPage page = new IPageBookViewPage() {
			public void createControl(final Composite parent) {
				label = new Label(parent, SWT.NONE);
				label.setText(Messages.getString("DebugView.noActiveEditor")); //$NON-NLS-1$
			}

			public void dispose() {
			}

			public Control getControl() {
				return label;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean isImportant(final IWorkbenchPart part) {
		return part instanceof VexEditor;
	}
}
