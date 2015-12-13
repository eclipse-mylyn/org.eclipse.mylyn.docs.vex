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
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

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

	private StyleSheet readStyleSheet() {
		try {
			final InputSource inputSource = new InputSource(new InputStreamReader(getClass().getResourceAsStream("box-demo.css"), "utf-8"));
			return new StyleSheetReader().read(inputSource, null);
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (final CSSException e) {
			e.printStackTrace();
		} catch (final IOException e) {
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
