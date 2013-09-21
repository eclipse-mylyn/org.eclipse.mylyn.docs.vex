/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Carsten Hiesserich - Added support for view states and content display
 *******************************************************************************/
package org.eclipse.vex.ui.internal.outline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IActionBars;
import org.eclipse.vex.core.XML;
import org.eclipse.vex.core.internal.css.IWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IAttribute;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.ui.internal.Messages;
import org.eclipse.vex.ui.internal.PluginImages;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.editor.EditorEventAdapter;
import org.eclipse.vex.ui.internal.editor.IVexEditorListener;
import org.eclipse.vex.ui.internal.editor.VexEditor;
import org.eclipse.vex.ui.internal.editor.VexEditorEvent;
import org.osgi.service.prefs.Preferences;

/**
 * Default implementation of IOutlineProvider.
 */
public class DefaultOutlineProvider implements IOutlineProvider, IToolBarContributor {

	/* Display the element content */
	public static final String SHOW_ELEMENT_CONTENT = "showElementContent";

	private ITreeContentProvider contentProvider;
	private IBaseLabelProvider labelProvider;

	/* The 'Show Element Content' command state */
	private boolean showElementContent = false;

	/* The maximum length of the shown element content */
	private static final int MAX_CONTENT_LENGTH = 30;

	public void init(final VexEditor editor) {
		contentProvider = new OutlineContentProvider();
		labelProvider = new OutlineLabelProvider(editor);
		initStates();
	}

	public void init(final StyleSheet styleSheet, final IWhitespacePolicy whitespacePolicy) {
		contentProvider = new OutlineContentProvider();
		labelProvider = new OutlineLabelProvider(styleSheet, whitespacePolicy);
		initStates();
	}

	public ITreeContentProvider getContentProvider() {
		return contentProvider;
	}

	public IBaseLabelProvider getLabelProvider() {
		return labelProvider;
	}

	/* This method is only here for compatibility with the interface */
	public IElement getOutlineElement(final IElement child) {
		return child;
	}

	public void setState(final String commandId, final boolean state) {
		if (commandId.equals(SHOW_ELEMENT_CONTENT)) {
			showElementContent = state;
		}
	}

	public void registerToolBarActions(final DocumentOutlinePage page, final IActionBars actionBars) {
		actionBars.getToolBarManager().add(new ToolBarToggleAction(page, SHOW_ELEMENT_CONTENT, PluginImages.DESC_SHOW_ELEMENT_CONTENT));
	}

	public boolean isStateSupported(final String commandId) {
		if (commandId.equals(SHOW_ELEMENT_CONTENT)) {
			return true;
		}

		return false;
	}

	public StyledString getOutlineLabel(final INode element) {
		return ((OutlineLabelProvider) labelProvider).getElementLabel(element);
	}

	public Image getOutlineImage(final IElement element) {
		return ((OutlineLabelProvider) labelProvider).getElementImage(element);
	}

	private void initStates() {
		final Preferences preferences = InstanceScope.INSTANCE.getNode(VexPlugin.ID);
		if (preferences != null) {
			showElementContent = preferences.getBoolean(SHOW_ELEMENT_CONTENT, true);
		}
	}

	private class OutlineContentProvider implements ITreeContentProvider {

		public void dispose() {
		}

		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		}

		public Object[] getChildren(final Object parentElement) {
			return getOutlineChildren((INode) parentElement);
		}

		public Object getParent(final Object node) {
			final INode parent = ((INode) node).getParent();
			if (parent == null) {
				return null;
			} else {
				return parent;
			}
		}

		public boolean hasChildren(final Object node) {
			return getOutlineChildren((INode) node).length > 0;
		}

		public Object[] getElements(final Object inputElement) {
			final IDocument document = (IDocument) inputElement;
			final IElement root = document.getRootElement();
			if (root.hasChildren()) {
				return document.getRootElement().children().asList().toArray();
			} else {
				// Only show the root element if there are no childs
				return new Object[] { root };
			}
		}

		private INode[] getOutlineChildren(final INode node) {

			if (!(node instanceof IParent)) {
				return new INode[0];
			}

			final List<INode> children = new ArrayList<INode>();
			for (final INode child : ((IParent) node).children().withoutText()) {
				children.add(child);
			}
			return children.toArray(new INode[children.size()]);
		}
	}

	private class OutlineLabelProvider extends StyledCellLabelProvider {

		private static final String EMPTY_STRING = "";

		private VexEditor editor = null;
		private IVexEditorListener listener = null;
		private StyleSheet styleSheet;
		private IWhitespacePolicy whitespacePolicy;

		public OutlineLabelProvider(final VexEditor editor) {
			this(editor.getStyle().getStyleSheet(), editor.getVexWidget().getWhitespacePolicy());
			this.editor = editor;
			listener = new EditorEventAdapter() {
				@Override
				public void styleChanged(final VexEditorEvent event) {
					styleSheet = event.getVexEditor().getStyle().getStyleSheet();
					whitespacePolicy = event.getVexEditor().getVexWidget().getWhitespacePolicy();
				}
			};
			editor.addVexEditorListener(listener);
		}

		public OutlineLabelProvider(final StyleSheet styleSheet, final IWhitespacePolicy whitespacePolicy) {
			this.styleSheet = styleSheet;
			this.whitespacePolicy = whitespacePolicy;
		}

		@Override
		public void update(final ViewerCell cell) {
			final INode node = (INode) cell.getElement();

			final StyledString label = getElementLabel(node);
			cell.setText(label.getString());
			cell.setStyleRanges(label.getStyleRanges());

			cell.setImage(getElementImage(node));
			super.update(cell);
		}

		@Override
		public void dispose() {
			super.dispose();
			if (editor != null) {
				editor.removeVexEditorListener(listener);
			}
		}

		public Image getElementImage(final INode node) {
			return node.accept(elementImageVisitor);
		}

		public StyledString getElementLabel(final INode node) {

			final String rawLabel = node.accept(elementLabelVisitor);

			if (!showElementContent) {
				return new StyledString(rawLabel);
			}

			final StyledString label = new StyledString(rawLabel);
			String content = null;
			// getOutlineContent returns either an IAttribute or an INode
			final Object outlineElement = styleSheet.getStyles(node).getOutlineContent();
			if (outlineElement != null) {
				if (outlineElement instanceof IAttribute) {
					content = ((IAttribute) outlineElement).getValue();
				} else if (outlineElement instanceof IParent) {
					content = "";
					final Iterator<? extends INode> childIter = ((IParent) outlineElement).children().iterator();
					while (content.length() < MAX_CONTENT_LENGTH && childIter.hasNext()) {
						content += childIter.next().accept(new BaseNodeVisitorWithResult<String>("") {
							@Override
							public String visit(final IElement element) {
								return element.getText();
							}

							@Override
							public String visit(final IText text) {
								return text.getText();
							}
						});
					}
					content = XML.compressWhitespace(content, false, false, false);
				} else if (outlineElement instanceof IProcessingInstruction) {
					content = ((IProcessingInstruction) outlineElement).getText();
					content = XML.compressWhitespace(content, false, false, false);
				}
			}
			
			if (content != null && content.length() > 0) {
				if (content.length() > MAX_CONTENT_LENGTH - 3) {
					content = content.substring(0, Math.min(MAX_CONTENT_LENGTH, content.length())) + "...";
				}
				label.append(" " + content, StyledString.DECORATIONS_STYLER);
			}

			return label;
		}

		private final INodeVisitorWithResult<Image> elementImageVisitor = new BaseNodeVisitorWithResult<Image>(PluginImages.get(PluginImages.IMG_XML_UNKNOWN)) {
			@Override
			public Image visit(final IElement element) {
				if (whitespacePolicy.isBlock(element)) {
					return PluginImages.get(PluginImages.IMG_XML_BLOCK_ELEMENT);
				}
				return PluginImages.get(PluginImages.IMG_XML_INLINE_ELEMENT);
			}

			@Override
			public Image visit(final IComment comment) {
				return PluginImages.get(PluginImages.IMG_XML_COMMENT);
			}

			@Override
			public Image visit(final IProcessingInstruction pi) {
				return PluginImages.get(PluginImages.IMG_XML_PROC_INSTR);
			}
		};

		private final INodeVisitorWithResult<String> elementLabelVisitor = new BaseNodeVisitorWithResult<String>(EMPTY_STRING) {
			@Override
			public String visit(final IElement element) {
				return element.getLocalName();
			}

			@Override
			public String visit(final IComment comment) {
				return Messages.getString("VexEditor.Path.Comment");
			}

			@Override
			public String visit(final IProcessingInstruction pi) {
				return pi.getTarget();
			}
		};
	}

}
