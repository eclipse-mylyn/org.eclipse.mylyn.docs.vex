/*******************************************************************************
 * Copyright (c) 2014, 2015 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.boxview;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.io.UniversalTestDocument;
import org.eclipse.vex.core.internal.visualization.CSSBasedBoxModelBuilder;
import org.eclipse.vex.core.internal.widget.DOMController;
import org.eclipse.vex.core.internal.widget.swt.BoxWidget;
import org.eclipse.vex.core.internal.widget.swt.IVexSelection;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;

/**
 * This is a viewer for the new box model - just to do visual experiments.
 *
 * @author Florian Thienel
 */
public class BoxDemoView extends ViewPart {

	private static final int SAMPLE_COUNT = 25;
	private static final IPath CSS_WORKSPACE_FILE = new Path("/test/box-demo.css");

	private Composite boxWidgetParent;
	private BoxWidget boxWidget;

	private Label offsetLabel;

	private final ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(final SelectionChangedEvent event) {
			final ISelection selection = event.getSelection();
			offsetLabel.setText(caretPositionAsText(selection));
		}
	};

	private final IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(final IResourceChangeEvent event) {
			try {
				event.getDelta().accept(new IResourceDeltaVisitor() {
					@Override
					public boolean visit(final IResourceDelta delta) throws CoreException {
						if (!delta.getFullPath().equals(CSS_WORKSPACE_FILE)) {
							return true;
						}
						reloadStyleSheet();
						return false;
					}
				});
			} catch (final CoreException e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void init(final IViewSite site) throws PartInitException {
		super.init(site);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void createPartControl(final Composite parent) {
		final Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new GridLayout());

		boxWidgetParent = new Composite(root, SWT.NONE);
		boxWidgetParent.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		boxWidgetParent.setLayout(new FillLayout());

		final Composite infoPanel = new Composite(root, SWT.NONE);
		infoPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		infoPanel.setLayout(new RowLayout());

		new Label(infoPanel, SWT.NONE).setText("Caret Position:");
		offsetLabel = new Label(infoPanel, SWT.RIGHT);
		offsetLabel.setLayoutData(new RowData(40, SWT.DEFAULT));

		recreateBoxWidget();
	}

	@Override
	public void dispose() {
		super.dispose();
		boxWidget = null;
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
	}

	@Override
	public void setFocus() {
		boxWidget.setFocus();
	}

	public void recreateBoxWidget() {
		if (boxWidget != null) {
			boxWidget.removeSelectionChangedListener(selectionChangedListener);
			boxWidget.dispose();
			boxWidget = null;
			cleanStaleReferenceInShell();
		}
		boxWidget = new BoxWidget(boxWidgetParent, SWT.V_SCROLL);

		boxWidget.setContent(UniversalTestDocument.createTestDocumentWithInlineElements(SAMPLE_COUNT));
		boxWidget.setBoxModelBuilder(new CSSBasedBoxModelBuilder(readStyleSheet()));
		boxWidgetParent.layout();
		boxWidget.addSelectionChangedListener(selectionChangedListener);

		offsetLabel.setText(caretPositionAsText(boxWidget.getSelection()));
	}

	private void cleanStaleReferenceInShell() {
		/*
		 * Shell keeps a reference to the boxWidget in Shell.savedFocus. parent.setFocus() forces Shell to store a
		 * reference to parent instead.
		 */
		boxWidgetParent.setFocus();
	}

	private void reloadStyleSheet() {
		boxWidget.setBoxModelBuilder(new CSSBasedBoxModelBuilder(readStyleSheet()));
		rebuildBoxModel();
	}

	private StyleSheet readStyleSheet() {
		try {
			final InputStream styleSheetInputStream;
			final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(CSS_WORKSPACE_FILE);
			if (file.exists()) {
				styleSheetInputStream = file.getContents();
			} else {
				styleSheetInputStream = getClass().getResourceAsStream("box-demo.css");
			}

			final InputSource inputSource = new InputSource(new InputStreamReader(styleSheetInputStream, "utf-8"));
			return new StyleSheetReader().read(inputSource, null);
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (final CSSException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String caretPositionAsText(final ISelection selection) {
		return Integer.toString(((IVexSelection) selection).getCaretOffset());
	}

	public void rebuildBoxModel() {
		final DOMController controller = boxWidget.getDOMController();
		controller.rebuildBoxModel();
	}

	public void insertBold() {
		final DOMController controller = boxWidget.getDOMController();
		controller.insertElement(new QualifiedName(null, "b"));
	}

	public void insertItalic() {
		final DOMController controller = boxWidget.getDOMController();
		controller.insertElement(new QualifiedName(null, "i"));
	}
}
