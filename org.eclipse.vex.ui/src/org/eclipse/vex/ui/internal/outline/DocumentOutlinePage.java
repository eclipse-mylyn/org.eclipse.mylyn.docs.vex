/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Torsten Stolpmann - bug 257946 - fixed outline view to work with multipage editor.
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Carsten Hiesserich - Use EditorEventAdapter instead of IVexEditorListener
 *     Carsten Hiesserich - complete revision
 *                          Support for ToolBar and actions, performance optimization
 *******************************************************************************/
package org.eclipse.vex.ui.internal.outline;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.widget.IDocumentEditor;
import org.eclipse.vex.core.internal.widget.swt.BoxWidget;
import org.eclipse.vex.core.provisional.dom.AttributeChangeEvent;
import org.eclipse.vex.core.provisional.dom.ContentChangeEvent;
import org.eclipse.vex.core.provisional.dom.IAttribute;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentListener;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.NamespaceDeclarationChangeEvent;
import org.eclipse.vex.ui.internal.Messages;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.config.DocumentType;
import org.eclipse.vex.ui.internal.config.Style;
import org.eclipse.vex.ui.internal.editor.EditorEventAdapter;
import org.eclipse.vex.ui.internal.editor.IVexEditorListener;
import org.eclipse.vex.ui.internal.editor.SelectionProvider;
import org.eclipse.vex.ui.internal.editor.VexEditor;
import org.eclipse.vex.ui.internal.editor.VexEditorEvent;
import org.osgi.framework.Bundle;

/**
 * Outline page for documents. Determination of the outline itself is deferred to a doctype-specific implementation of
 * IOutlineProvider.
 */
public class DocumentOutlinePage extends Page implements IContentOutlinePage {

	public DocumentOutlinePage(final VexEditor vexEditor) {
		super();
		editorPart = vexEditor;
	}

	@Override
	public void createControl(final Composite parent) {

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		editorPart.addVexEditorListener(vexEditorListener);
		editorPart.getEditorSite().getSelectionProvider().addSelectionChangedListener(selectionListener);
		initToolbarActions();
		if (editorPart.isLoaded()) {
			showTreeViewer();
		} else {
			showLabel(Messages.getString("DocumentOutlinePage.loading")); //$NON-NLS-1$
		}

	}

	@Override
	public void dispose() {
		editorPart.removeVexEditorListener(vexEditorListener);
		editorPart.getEditorSite().getSelectionProvider().removeSelectionChangedListener(selectionListener);
		if (filterActionGroup != null) {
			filterActionGroup.dispose();
		}
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void init(final IPageSite pageSite) {
		super.init(pageSite);
	}

	@Override
	public void setFocus() {
		if (treeViewer != null) {
			treeViewer.getControl().setFocus();
		}
	}

	@Override
	public void addSelectionChangedListener(final ISelectionChangedListener listener) {
		selectionProvider.addSelectionChangedListener(listener);
	}

	@Override
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

	@Override
	public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
		selectionProvider.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(final ISelection selection) {
		selectionProvider.setSelection(selection);
	}

	/**
	 * Updates the state of the outline view and refreshes the tree viewer.
	 *
	 * @see IToolBarContributor
	 */
	public void setViewState(final String stateId, final boolean newValue) {
		if (outlineProvider instanceof IToolBarContributor) {
			((IToolBarContributor) outlineProvider).setState(stateId, newValue);
		}

		if (treeViewer != null) {
			treeViewer.getControl().setRedraw(false);
			BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), new Runnable() {
				@Override
				public void run() {
					treeViewer.refresh();
				}
			});
			treeViewer.getControl().setRedraw(true);
		}
	};

	// ===================================================== PRIVATE

	private Composite composite;

	private Label label;
	private TreeViewer treeViewer;
	private OutlineFilterActionGroup filterActionGroup;

	private final VexEditor editorPart;

	private IOutlineProvider outlineProvider;

	private final SelectionProvider selectionProvider = new SelectionProvider();

	private void showLabel(final String message) {

		if (treeViewer != null) {
			treeViewer.removeSelectionChangedListener(selectionListener);
			treeViewer.getTree().dispose();
			getSite().getActionBars().getToolBarManager().removeAll();
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

		final DocumentType doctype = editorPart.getDocumentType();

		if (doctype == null) {
			return;
		}

		filterActionGroup.fillActionBars(getSite().getActionBars());

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

		if (outlineProvider instanceof IToolBarContributor) {
			((IToolBarContributor) outlineProvider).registerToolBarActions(this, getSite().getActionBars());
		}

		outlineProvider.init(editorPart);

		treeViewer.setContentProvider(outlineProvider.getContentProvider());
		treeViewer.setLabelProvider(outlineProvider.getLabelProvider());
		treeViewer.setAutoExpandLevel(2);

		filterActionGroup.setViewer(treeViewer);

		treeViewer.setUseHashlookup(true);
		final IDocument document = editorPart.getVexWidget().getDocument();
		treeViewer.setInput(document);
		document.addDocumentListener(documentListener);

		treeViewer.addSelectionChangedListener(selectionListener);
		treeViewer.addDoubleClickListener(doubleClickListener);
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
		private Object[] lastExpandedElements = null;
		private INode selectedTreeNode;

		@Override
		public void selectionChanged(final SelectionChangedEvent event) {
			if (event.getSource() instanceof BoxWidget) {
				final BoxWidget vexWidget = (BoxWidget) event.getSource();
				if (vexWidget.isFocusControl() && getTreeViewer() != null) {
					final INode element = vexWidget.getCurrentNode();

					if (element == null || element.equals(selectedTreeNode)) {
						// If we're still in the same element, there is no need to refresh
						return;
					}

					if (element.getDocument().getRootElement() == element) {
						return;
					}

					selectedTreeNode = element;
					while (selectedTreeNode != null && filterActionGroup.isElementFiltered(selectedTreeNode)) {
						// If the selected element is not visible, try to find a visible parent
						selectedTreeNode = selectedTreeNode.getParent();
					}
					if (selectedTreeNode != null) {
						getTreeViewer().getControl().setRedraw(false);
						BusyIndicator.showWhile(getTreeViewer().getControl().getDisplay(), new Runnable() {
							@Override
							public void run() {
								// restore the expanded state
								if (lastExpandedElements != null) {
									getTreeViewer().setExpandedElements(lastExpandedElements);
									lastExpandedElements = null;
								}
								if (!getTreeViewer().getExpandedState(selectedTreeNode.getParent())) {
									// ELement is not visible - save the tree state
									lastExpandedElements = getTreeViewer().getExpandedElements();
								}
								getTreeViewer().setSelection(new StructuredSelection(selectedTreeNode));
							}
						});
						getTreeViewer().getControl().setRedraw(true);
					} else {
						getTreeViewer().setSelection(new StructuredSelection());
						selectedTreeNode = null;
					}
				}
			} else {
				// it's our tree control being selected
				final TreeViewer treeViewer = (TreeViewer) event.getSource();
				if (treeViewer.getTree().isFocusControl()) {
					final TreeItem[] selected = treeViewer.getTree().getSelection();
					if (selected.length > 0) {
						lastExpandedElements = null;
						final INode node = (INode) selected[0].getData();
						selectedTreeNode = node;
						final IDocumentEditor documentEditor = editorPart.getVexWidget();

						// Moving to the end of the element first is a cheap
						// way to make sure we end up with the
						// caret at the top of the viewport
						documentEditor.moveTo(node.getEndPosition());
						documentEditor.moveTo(node.getStartPosition().moveBy(1), true);
					}
				}
			}
		}
	};

	/**
	 * Receives double click events from our TreeViewer. We simply give focus to the VexWidget here. The first click
	 * already moved the caret to the selected element.
	 */
	private final IDoubleClickListener doubleClickListener = new IDoubleClickListener() {

		@Override
		public void doubleClick(final DoubleClickEvent event) {
			if (treeViewer.getTree().isFocusControl()) {
				final TreeItem[] selected = treeViewer.getTree().getSelection();
				if (selected.length > 0) {
					final INode node = (INode) selected[0].getData();
					final IDocumentEditor documentEditor = editorPart.getVexWidget();
					documentEditor.moveTo(node.getStartPosition().moveBy(1));
					editorPart.setFocus();
				}
			}
		}

	};

	private final IVexEditorListener vexEditorListener = new EditorEventAdapter() {

		@Override
		public void documentLoaded(final VexEditorEvent event) {
			showTreeViewer();
		}

		@Override
		public void documentUnloaded(final VexEditorEvent event) {
			showLabel(Messages.getString("DocumentOutlinePage.reloading")); //$NON-NLS-1$
		}

		@Override
		public void styleChanged(final VexEditorEvent event) {
			filterActionGroup.setStyleSheet(event.getVexEditor().getStyle().getStyleSheet());
		};
	};

	private final IDocumentListener documentListener = new IDocumentListener() {

		@Override
		public void attributeChanged(final AttributeChangeEvent event) {

			// This cast is save because this event is only fired due to the attribute changes of elements.
			final IElement parent = (IElement) event.getParent();
			final IAttribute attr = parent.getAttribute(event.getAttributeName());
			if (editorPart.getStyle().getStyleSheet().getStyles(parent).getOutlineContent() == attr) {
				// Parent has to be refreshed, since it uses this attribute as outline content
				getTreeViewer().refresh(outlineProvider.getOutlineElement(parent));
			}
		}

		@Override
		public void namespaceChanged(final NamespaceDeclarationChangeEvent event) {
		}

		@Override
		public void beforeContentDeleted(final ContentChangeEvent event) {
		}

		@Override
		public void beforeContentInserted(final ContentChangeEvent event) {
		}

		@Override
		public void contentDeleted(final ContentChangeEvent event) {
			final IParent outlineElement = event.getParent();
			refreshOutlineElement(outlineElement);
		}

		@Override
		public void contentInserted(final ContentChangeEvent event) {
			final IParent outlineElement = event.getParent();
			refreshOutlineElement(outlineElement);
		}

		private void refreshOutlineElement(final IParent outlineElement) {
			if (outlineElement.getDocument().getRootElement().equals(outlineElement)) {
				getTreeViewer().refresh();
			} else if (outlineElement instanceof IElement) {
				// This SHOULD always be the case
				final IElement parent = ((IElement) outlineElement).getParentElement();
				if (parent != null && editorPart.getStyle().getStyleSheet().getStyles(parent).getOutlineContent() == outlineElement) {
					// Parent has to be refreshed, since it uses this element as content
					getTreeViewer().refresh(outlineProvider.getOutlineElement(parent));
				} else {
					getTreeViewer().refresh(outlineProvider.getOutlineElement((IElement) outlineElement));
				}
			}
		}
	};

	private void initToolbarActions() {

		final Style style = editorPart.getStyle();
		if (style != null) {
			filterActionGroup = new OutlineFilterActionGroup(style.getStyleSheet());
		} else {
			// Style might be null if no document is loaded
			filterActionGroup = new OutlineFilterActionGroup(StyleSheet.NULL);
		}
	}
}
