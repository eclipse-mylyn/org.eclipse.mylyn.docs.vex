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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.vex.core.internal.io.UniversalTestDocument;
import org.eclipse.vex.core.internal.visualization.DocumentRootVisualization;
import org.eclipse.vex.core.internal.visualization.ParagraphVisualization;
import org.eclipse.vex.core.internal.visualization.StructureElementVisualization;
import org.eclipse.vex.core.internal.visualization.TextVisualization;
import org.eclipse.vex.core.internal.visualization.VisualizationChain;
import org.eclipse.vex.core.internal.widget.swt.BoxWidget;

/**
 * This is a viewer for the new box model - just to do visual experiments.
 *
 * @author Florian Thienel
 */
public class BoxDemoView extends ViewPart {

	private static final int SAMPLE_COUNT = 25;

	private Composite parent;
	private BoxWidget boxWidget;

	@Override
	public void createPartControl(final Composite parent) {
		this.parent = parent;
		refresh();
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

	public void refresh() {
		if (boxWidget != null) {
			boxWidget.dispose();
			boxWidget = null;
			cleanStaleReferenceInShell();
		}
		boxWidget = new BoxWidget(parent, SWT.V_SCROLL);

		boxWidget.setContent(UniversalTestDocument.createTestDocument(SAMPLE_COUNT));
		boxWidget.setVisualizationChain(buildVisualizationChain());
		parent.layout();
	}

	private void cleanStaleReferenceInShell() {
		/*
		 * Shell keeps a reference to the boxWidget in Shell.savedFocus. parent.setFocus() forces Shell to store a
		 * reference to parent instead.
		 */
		parent.setFocus();
	}

	private static VisualizationChain buildVisualizationChain() {
		final VisualizationChain visualizationChain = new VisualizationChain();
		visualizationChain.addForRoot(new DocumentRootVisualization());
		visualizationChain.addForStructure(new ParagraphVisualization());
		visualizationChain.addForStructure(new StructureElementVisualization());
		visualizationChain.addForInline(new TextVisualization());
		return visualizationChain;
	}

}
