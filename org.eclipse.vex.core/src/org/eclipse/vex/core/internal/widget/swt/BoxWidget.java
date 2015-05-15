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
package org.eclipse.vex.core.internal.widget.swt;

import static org.eclipse.vex.core.internal.cursor.CursorMoves.down;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.left;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.right;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toAbsoluteCoordinates;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.toOffset;
import static org.eclipse.vex.core.internal.cursor.CursorMoves.up;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.vex.core.internal.boxes.BaseBoxVisitor;
import org.eclipse.vex.core.internal.boxes.BaseBoxVisitorWithResult;
import org.eclipse.vex.core.internal.boxes.DepthFirstTraversal;
import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IChildBox;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.IStructuralBox;
import org.eclipse.vex.core.internal.boxes.NodeReference;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.cursor.ContentMap;
import org.eclipse.vex.core.internal.cursor.Cursor;
import org.eclipse.vex.core.internal.cursor.ICursorMove;
import org.eclipse.vex.core.internal.visualization.VisualizationChain;
import org.eclipse.vex.core.internal.widget.swt.DoubleBufferedRenderer.IRenderStep;
import org.eclipse.vex.core.provisional.dom.AttributeChangeEvent;
import org.eclipse.vex.core.provisional.dom.ContentChangeEvent;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentListener;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.NamespaceDeclarationChangeEvent;

/**
 * A widget to display the new box model.
 *
 * @author Florian Thienel
 */
public class BoxWidget extends Canvas {

	private IDocument document;
	private VisualizationChain visualizationChain;
	private RootBox rootBox;

	private final ContentMap contentMap;
	private final Cursor cursor;
	private final DoubleBufferedRenderer renderer;

	private final IDocumentListener documentListener = new IDocumentListener() {
		@Override
		public void attributeChanged(final AttributeChangeEvent event) {
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
		}

		@Override
		public void contentInserted(final ContentChangeEvent event) {
			if (event.isStructuralChange()) {
				invalidateStructure(event.getParent());
			} else {
				invalidateContentRange(event.getRange());
			}
		}
	};

	public BoxWidget(final Composite parent, final int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		renderer = new DoubleBufferedRenderer(this);
		connectDispose();
		connectResize();
		if ((style & SWT.V_SCROLL) == SWT.V_SCROLL) {
			connectScrollVertically();
		}
		connectKeyboard();
		connectMouse();

		visualizationChain = new VisualizationChain();
		rootBox = new RootBox();
		contentMap = new ContentMap();
		contentMap.setRootBox(rootBox);
		cursor = new Cursor(contentMap);
	}

	public void setContent(final IDocument document) {
		disconnectDocumentListener();
		this.document = document;
		connectDocumentListener();

		rebuildRootBox();
	}

	private void connectDocumentListener() {
		if (document != null) {
			document.addDocumentListener(documentListener);
		}
	}

	private void disconnectDocumentListener() {
		if (document != null) {
			document.removeDocumentListener(documentListener);
		}
	}

	public void setVisualization(final VisualizationChain visualizationChain) {
		Assert.isNotNull(visualizationChain);
		this.visualizationChain = visualizationChain;
		rebuildRootBox();
	}

	private void rebuildRootBox() {
		if (document != null) {
			rootBox = visualizationChain.visualizeRoot(document);
		} else {
			rootBox = new RootBox();
		}

		if (rootBox == null) {
			rootBox = new RootBox();
		}

		contentMap.setRootBox(rootBox);
	}

	private void connectDispose() {
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				BoxWidget.this.widgetDisposed();
			}
		});
	}

	private void connectResize() {
		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				resize(e);
			}
		});
	}

	private void connectScrollVertically() {
		getVerticalBar().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				scrollVertically(e);
			}
		});
	}

	private void connectKeyboard() {
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				BoxWidget.this.keyPressed(e);
			}
		});
	}

	private void connectMouse() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent e) {
				BoxWidget.this.mouseDown(e);
			}
		});
	}

	private void widgetDisposed() {
		rootBox = null;
	}

	private void resize(final ControlEvent event) {
		invalidateWidth(getClientArea().width);
	}

	private void scrollVertically(final SelectionEvent event) {
		invalidateViewport();
	}

	private void keyPressed(final KeyEvent event) {
		switch (event.keyCode) {
		case SWT.ARROW_LEFT:
			moveCursor(left());
			break;
		case SWT.ARROW_RIGHT:
			moveCursor(right());
			break;
		case SWT.ARROW_UP:
			moveCursor(up());
			break;
		case SWT.ARROW_DOWN:
			moveCursor(down());
			break;
		case SWT.HOME:
			moveCursor(toOffset(0));
			break;
		default:
			enterChar(event.character);
			break;
		}
	}

	private void mouseDown(final MouseEvent event) {
		final int absoluteY = event.y + getVerticalBar().getSelection();
		moveCursor(toAbsoluteCoordinates(event.x, absoluteY));
	}

	private void moveCursor(final ICursorMove move) {
		cursor.move(move);
		invalidateCursor();
	}

	private void enterChar(final char c) {
		document.insertText(cursor.getOffset(), Character.toString(c));
		moveCursor(toOffset(cursor.getOffset() + 1));
	}

	private void invalidateStructure(final INode node) {
		final IRenderStep rebuildStructureForNode = new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				final IContentBox modifiedBox = contentMap.findBoxForRange(node.getRange());
				final IStructuralBox newBox = visualizationChain.visualizeStructure(node);
				final IStructuralBox newChildBox = newBox.accept(new BaseBoxVisitorWithResult<IStructuralBox>(newBox) {
					@Override
					public IStructuralBox visit(final NodeReference box) {
						return box.getComponent();
					}
				});
				modifiedBox.accept(new BaseBoxVisitor() {
					@Override
					public void visit(final NodeReference box) {
						box.setComponent(newChildBox);
					}
				});
				modifiedBox.layout(graphics);
				reconcileParentsLayout(modifiedBox, graphics);
				updateVerticalBar();
			}
		};

		render(rebuildStructureForNode, paintContent());
	}

	private void invalidateContentRange(final ContentRange range) {
		final IRenderStep reconcileLayoutForRange = new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				final IContentBox modifiedBox = contentMap.findBoxForRange(range);
				if (modifiedBox == null) {
					return;
				}

				includeGapsInTextContent(modifiedBox);
				modifiedBox.layout(graphics);
				reconcileParentsLayout(modifiedBox, graphics);
				updateVerticalBar();
			}

			private void includeGapsInTextContent(final IContentBox box) {
				box.accept(new DepthFirstTraversal<Object>() {
					private int lastEndOffset = box.getStartOffset();
					private TextContent lastTextContentBox;

					@Override
					public Object visit(final NodeReference box) {
						lastEndOffset = box.getStartOffset();
						box.getComponent().accept(this);

						if (lastTextContentBox != null && lastTextContentBox.getEndOffset() < box.getEndOffset() - 1) {
							lastTextContentBox.setEndOffset(box.getEndOffset() - 1);
						}

						lastEndOffset = box.getEndOffset();
						lastTextContentBox = null;
						return super.visit(box);
					}

					@Override
					public Object visit(final TextContent box) {
						if (box.getStartOffset() > lastEndOffset + 1) {
							box.setStartOffset(lastEndOffset + 1);
						}

						lastEndOffset = box.getEndOffset();
						lastTextContentBox = box;
						return super.visit(box);
					}
				});
			}

		};

		render(reconcileLayoutForRange, paintContent());
	}

	private void reconcileParentsLayout(final IContentBox box, final Graphics graphics) {
		IBox parentBox = getParentBox(box);
		while (parentBox != null && parentBox.reconcileLayout(graphics)) {
			parentBox = getParentBox(parentBox);
		}
	}

	private IBox getParentBox(final IBox box) {
		if (box instanceof IChildBox) {
			return ((IChildBox) box).getParent();
		}
		return null;
	}

	private void invalidateViewport() {
		render(paintContent());
	}

	private void invalidateCursor() {
		render(renderCursorMovement(), paintContent());
	}

	private void invalidateWidth(final int width) {
		final IRenderStep invalidateWidth = new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				rootBox.setWidth(width);
			}
		};

		render(invalidateWidth, layoutContent(), paintContent());
	}

	private void render(final IRenderStep... steps) {
		renderer.render(getViewPort(), steps);
	}

	private Rectangle getViewPort() {
		return new Rectangle(0, getVerticalBar().getSelection(), getSize().x, getSize().y);
	}

	private IRenderStep paintContent() {
		return new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				rootBox.paint(graphics);
				cursor.paint(graphics);
			}
		};
	}

	private IRenderStep layoutContent() {
		return new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				cursor.reconcile(graphics);
				rootBox.layout(graphics);
				updateVerticalBar();
			}
		};
	}

	private void updateVerticalBar() {
		final int maximum = rootBox.getHeight() + Cursor.CARET_BUFFER;
		final int pageSize = getClientArea().height;
		final int selection = getVerticalBar().getSelection();
		getVerticalBar().setValues(selection, 0, maximum, pageSize, pageSize / 4, pageSize);
	}

	private IRenderStep renderCursorMovement() {
		return new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				cursor.applyMoves(graphics);
				moveViewPortToCursor(getViewPort(), graphics);
			}
		};
	}

	private void moveViewPortToCursor(final Rectangle viewPort, final Graphics graphics) {
		final int delta = getDeltaIntoVisibleArea(viewPort);
		graphics.moveOrigin(0, -delta);
		moveVerticalBar(delta);
	}

	private int getDeltaIntoVisibleArea(final Rectangle viewPort) {
		final int top = viewPort.getY();
		final int height = viewPort.getHeight();
		return cursor.getDeltaIntoVisibleArea(top, height);
	}

	private void moveVerticalBar(final int delta) {
		if (delta == 0) {
			return;
		}

		final int selection = getVerticalBar().getSelection() + delta;
		getVerticalBar().setSelection(selection);
	}

}
