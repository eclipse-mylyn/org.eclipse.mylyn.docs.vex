/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.vex.core.internal.boxes.Border;
import org.eclipse.vex.core.internal.boxes.HorizontalBar;
import org.eclipse.vex.core.internal.boxes.Margin;
import org.eclipse.vex.core.internal.boxes.Padding;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.VerticalBlock;
import org.eclipse.vex.core.internal.core.Color;

/**
 * A widget to show the new box model.
 *
 * @author Florian Thienel
 */
public class BoxWidget extends Canvas {

	private final RootBox rootBox;

	public BoxWidget(final Composite parent, final int style) {
		super(parent, style);
		connectPaintControl();
		connectResize();
		if ((style & SWT.V_SCROLL) == SWT.V_SCROLL) {
			connectScrollVertically();
		}

		rootBox = new RootBox();
		for (int i = 0; i < 5000; i += 1) {
			final HorizontalBar bar = new HorizontalBar();
			bar.setHeight(10);
			bar.setColor(Color.BLACK);
			final VerticalBlock block = new VerticalBlock();
			block.appendChild(bar);
			block.setMargin(new Margin(10, 20, 30, 40));
			block.setBorder(new Border(10));
			block.setPadding(new Padding(15, 25, 35, 45));
			rootBox.appendChild(block);
		}
	}

	private void connectPaintControl() {
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(final PaintEvent e) {
				BoxWidget.this.paintControl(e);
			}
		});
	}

	private void connectResize() {
		addControlListener(new ControlListener() {
			@Override
			public void controlResized(final ControlEvent e) {
				resize(e);
			}

			@Override
			public void controlMoved(final ControlEvent e) {
				// ignore
			}
		});
	}

	private void connectScrollVertically() {
		getVerticalBar().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				scrollVertically(e);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// ignore
			}
		});
	}

	private void paintControl(final PaintEvent event) {
		final SwtGraphics graphics = new SwtGraphics(event.gc);
		rootBox.paint(graphics);
	}

	private void resize(final ControlEvent event) {
		rootBox.setWidth(getClientArea().width);
		rootBox.layout();
	}

	private void scrollVertically(final SelectionEvent event) {

	}
}
