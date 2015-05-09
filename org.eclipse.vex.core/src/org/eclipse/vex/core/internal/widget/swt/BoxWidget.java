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
import org.eclipse.vex.core.internal.boxes.Frame;
import org.eclipse.vex.core.internal.boxes.HorizontalBar;
import org.eclipse.vex.core.internal.boxes.IBox;
import org.eclipse.vex.core.internal.boxes.IBoxVisitorWithResult;
import org.eclipse.vex.core.internal.boxes.IChildBox;
import org.eclipse.vex.core.internal.boxes.IContentBox;
import org.eclipse.vex.core.internal.boxes.NodeReference;
import org.eclipse.vex.core.internal.boxes.Paragraph;
import org.eclipse.vex.core.internal.boxes.RootBox;
import org.eclipse.vex.core.internal.boxes.Square;
import org.eclipse.vex.core.internal.boxes.StaticText;
import org.eclipse.vex.core.internal.boxes.TextContent;
import org.eclipse.vex.core.internal.boxes.VerticalBlock;
import org.eclipse.vex.core.internal.core.Graphics;
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
		System.out.println("Width: " + getClientArea().width);
		rootBox.setWidth(getClientArea().width);
		invalidateLayout();
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
		cursor.move(right());
	}

	private void invalidateStructure(final INode node) {
		final IRenderStep rebuildStructureForNode = new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				final IContentBox modifiedBox = contentMap.findBoxForRange(node.getRange());
				final IChildBox newBox = visualizationChain.visualizeStructure(node);
				final IChildBox newChildBox = newBox.accept(new BaseBoxVisitorWithResult<IChildBox>(newBox) {
					@Override
					public IChildBox visit(final NodeReference box) {
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
		renderer.render(rebuildStructureForNode, renderCursorMovement(), paintContent());
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
		renderer.render(reconcileLayoutForRange, renderCursorMovement(), paintContent());
	}

	private void reconcileParentsLayout(final IContentBox box, final Graphics graphics) {
		IBox parentBox = getParentBox(box);
		while (parentBox != null && parentBox.reconcileLayout(graphics)) {
			parentBox = getParentBox(parentBox);
		}
	}

	private IBox getParentBox(final IBox childBox) {
		return childBox.accept(new IBoxVisitorWithResult<IBox>() {
			@Override
			public IBox visit(final RootBox box) {
				return null;
			}

			@Override
			public IBox visit(final VerticalBlock box) {
				return box.getParent();
			}

			@Override
			public IBox visit(final Frame box) {
				return box.getParent();
			}

			@Override
			public IBox visit(final NodeReference box) {
				return box.getParent();
			}

			@Override
			public IBox visit(final HorizontalBar box) {
				return box.getParent();
			}

			@Override
			public IBox visit(final Paragraph box) {
				return box.getParent();
			}

			@Override
			public IBox visit(final StaticText box) {
				return box.getParent();
			}

			@Override
			public IBox visit(final TextContent box) {
				return box.getParent();
			}

			@Override
			public IBox visit(final Square box) {
				return box.getParent();
			}
		});
	}

	private void invalidateViewport() {
		renderer.render(paintContent());
	}

	private void invalidateCursor() {
		renderer.render(renderCursorMovement(), paintContent());
	}

	private void invalidateLayout() {
		renderer.render(layoutContent(), paintContent());
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
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				final int maximum = rootBox.getHeight() + Cursor.CARET_BUFFER;
				final int pageSize = getClientArea().height;
				final int selection = getVerticalBar().getSelection();
				getVerticalBar().setValues(selection, 0, maximum, pageSize, pageSize / 4, pageSize);
			}
		});
	}

	private IRenderStep renderCursorMovement() {
		return new IRenderStep() {
			@Override
			public void render(final Graphics graphics) {
				cursor.applyMoves(graphics);
				moveViewPortToCursor(graphics);
			}
		};
	}

	private void moveViewPortToCursor(final Graphics graphics) {
		final int delta = getDeltaIntoVisibleArea();
		graphics.moveOrigin(0, -delta);
		moveVerticalBar(delta);
	}

	private int getDeltaIntoVisibleArea() {
		final int[] top = new int[1];
		final int[] height = new int[1];
		getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				top[0] = getVerticalBar().getSelection();
				height[0] = getSize().y;
			}
		});
		return cursor.getDeltaIntoVisibleArea(top[0], height[0]);
	}

	private void moveVerticalBar(final int delta) {
		if (delta == 0) {
			return;
		}

		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				final int selection = getVerticalBar().getSelection() + delta;
				getVerticalBar().setSelection(selection);
			}
		});
	}

}
