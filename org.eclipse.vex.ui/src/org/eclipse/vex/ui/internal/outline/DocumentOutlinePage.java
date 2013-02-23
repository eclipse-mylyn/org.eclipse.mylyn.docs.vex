/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Torsten Stolpmann - bug 257946 - fixed outline view to work with multipage editor.
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.ui.internal.outline;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.vex.core.internal.widget.swt.VexWidget;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.config.DocumentType;
import org.eclipse.vex.ui.internal.editor.IVexEditorListener;
import org.eclipse.vex.ui.internal.editor.Messages;
import org.eclipse.vex.ui.internal.editor.SelectionProvider;
import org.eclipse.vex.ui.internal.editor.VexEditor;
import org.eclipse.vex.ui.internal.editor.VexEditorEvent;
import org.osgi.framework.Bundle;

/**
 * Outline page for documents. Determination of the outline itself is deferred to a doctype-specific implementation of
 * IOutlineProvider.
 */
public class DocumentOutlinePage extends Page implements IContentOutlinePage {

	@Override
	public void createControl(final Composite parent) {

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		if (vexEditor.isLoaded()) {
			showTreeViewer();
		} else {
			showLabel(Messages.getString("DocumentOutlinePage.loading")); //$NON-NLS-1$
		}

	}

	@Override
	public void dispose() {
		vexEditor.removeVexEditorListener(vexEditorListener);
		vexEditor.getEditorSite().getSelectionProvider().removeSelectionChangedListener(selectionListener);
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void init(final IPageSite pageSite) {
		super.init(pageSite);
		final IEditorPart editor = pageSite.getPage().getActiveEditor();
		if (editor instanceof VexEditor) {
			vexEditor = (VexEditor) editor;
		}
		vexEditor.addVexEditorListener(vexEditorListener);
		vexEditor.getEditorSite().getSelectionProvider().addSelectionChangedListener(selectionListener);
	}

	@Override
	public void setFocus() {
		if (treeViewer != null) {
			treeViewer.getControl().setFocus();
		}
	}

	public void addSelectionChangedListener(final ISelectionChangedListener listener) {
		selectionProvider.addSelectionChangedListener(listener);
	}

	public ISelection getSelection() {
		return selectionProvider.getSelection();
	}

	/**
	 * Returns the TreeViewer associated with this page. May return null, if VexPlugin has not yet loaded its
	 * configuration.
	 */
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
		selectionProvider.removeSelectionChangedListener(listener);

	}

	public void setSelection(final ISelection selection) {
		selectionProvider.setSelection(selection);
	}

	// ===================================================== PRIVATE

	private Composite composite;

	private Label label;
	private TreeViewer treeViewer;

	private VexEditor vexEditor;

	private IOutlineProvider outlineProvider;

	private final SelectionProvider selectionProvider = new SelectionProvider();

	private void showLabel(final String message) {

		if (treeViewer != null) {
			treeViewer.removeSelectionChangedListener(selectionListener);
			treeViewer.getTree().dispose();
			treeViewer = null;
		}

		if (label == null) {
			label = new Label(composite, SWT.NONE);
			label.setText(message);
			composite.layout(true);
		}

		label.setText(message);
	}

	private void showTreeViewer() {

		if (treeViewer != null) {
			return;
		}

		if (label != null) {
			label.dispose();
			label = null;
		}

		treeViewer = new TreeViewer(composite, SWT.NONE);
		composite.layout();

		final DocumentType doctype = vexEditor.getDocumentType();

		if (doctype == null) {
			return;
		}

		final String ns = doctype.getConfig().getUniqueIdentifer();
		final Bundle bundle = Platform.getBundle(ns);
		final String providerClassName = doctype.getOutlineProvider();
		if (bundle != null && providerClassName != null) {
			try {
				final Class<?> clazz = bundle.loadClass(providerClassName);
				outlineProvider = (IOutlineProvider) clazz.newInstance();
			} catch (final Exception ex) {
				final String message = Messages.getString("DocumentOutlinePage.loadingError"); //$NON-NLS-1$
				VexPlugin.getDefault().log(IStatus.WARNING, MessageFormat.format(message, new Object[] { providerClassName, ns, ex }));
			}
		}

		if (outlineProvider == null) {
			outlineProvider = new DefaultOutlineProvider();
		}

		outlineProvider.init(vexEditor);

		treeViewer.setContentProvider(outlineProvider.getContentProvider());
		treeViewer.setLabelProvider(outlineProvider.getLabelProvider());
		treeViewer.setAutoExpandLevel(2);

		treeViewer.setInput(vexEditor.getVexWidget().getDocument());

		treeViewer.addSelectionChangedListener(selectionListener);

	}

	/**
	 * Receives selection changed events from both our TreeViewer and the VexWidget. Generally, we use this to
	 * synchronize the selection between the two, but we also do the following...
	 * 
	 * - when a notification comes from VexWidget, we create the treeViewer if needed (that is, if the part was created
	 * before VexPlugin was done loading its configuration.
	 * 
	 * - notifications from the TreeViewer are passed on to our SelectionChangedListeners.
	 */
	private final ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		public void selectionChanged(final SelectionChangedEvent event) {
			if (event.getSource() instanceof VexWidget) {
				final VexWidget vexWidget = (VexWidget) event.getSource();
				if (vexWidget.isFocusControl() && getTreeViewer() != null) {
					final IElement element = vexWidget.getCurrentElement();
					if (element != null) {
						final IElement outlineElement = outlineProvider.getOutlineElement(element);
						getTreeViewer().refresh(outlineElement);
						getTreeViewer().setSelection(new StructuredSelection(outlineElement), true);
					} else {
						getTreeViewer().setSelection(new StructuredSelection(), true);
					}
				}
			} else {
				// it's our tree control being selected
				final TreeViewer treeViewer = (TreeViewer) event.getSource();
				if (treeViewer.getTree().isFocusControl()) {
					final TreeItem[] selected = treeViewer.getTree().getSelection();
					if (selected.length > 0) {

						final IElement element = (IElement) selected[0].getData();
						final VexWidget vexWidget = vexEditor.getVexWidget();

						// Moving to the end of the element first is a cheap
						// way to make sure we end up with the
						// caret at the top of the viewport
						vexWidget.moveTo(element.getEndOffset());
						vexWidget.moveTo(element.getStartOffset() + 1);
					}
				}
			}
		}
	};

	private final IVexEditorListener vexEditorListener = new IVexEditorListener() {

		public void documentLoaded(final VexEditorEvent event) {
			showTreeViewer();
		}

		public void documentUnloaded(final VexEditorEvent event) {
			showLabel(Messages.getString("DocumentOutlinePage.reloading")); //$NON-NLS-1$
		}

	};
}
