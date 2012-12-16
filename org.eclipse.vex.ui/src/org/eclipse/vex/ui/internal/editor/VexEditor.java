/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.ui.internal.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.vex.core.internal.core.ListenerList;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentReader;
import org.eclipse.vex.core.internal.dom.DocumentWriter;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.Validator;
import org.eclipse.vex.core.internal.validator.WTPVEXValidator;
import org.eclipse.vex.core.internal.widget.CssWhitespacePolicy;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.eclipse.vex.ui.internal.VexPreferences;
import org.eclipse.vex.ui.internal.config.ConfigEvent;
import org.eclipse.vex.ui.internal.config.ConfigurationRegistry;
import org.eclipse.vex.ui.internal.config.DocumentType;
import org.eclipse.vex.ui.internal.config.IConfigListener;
import org.eclipse.vex.ui.internal.config.Style;
import org.eclipse.vex.ui.internal.handlers.ConvertElementHandler;
import org.eclipse.vex.ui.internal.handlers.RemoveTagHandler;
import org.eclipse.vex.ui.internal.outline.DocumentOutlinePage;
import org.eclipse.vex.ui.internal.property.ElementPropertySource;
import org.eclipse.vex.ui.internal.swt.VexWidget;
import org.xml.sax.SAXParseException;

/**
 * Editor for editing XML file using the VexWidget.
 */
public class VexEditor extends EditorPart {

	/**
	 * ID of this editor extension.
	 */
	public static final String ID = "org.eclipse.vex.ui.internal.editor.VexEditor"; //$NON-NLS-1$

	private final boolean debugging;
	private final ConfigurationRegistry configurationRegistry;
	private final VexPreferences preferences;

	private Composite parentControl;
	private Label loadingLabel;

	private boolean loaded;
	private DocumentType doctype;
	private Document document;
	private Style style;

	private VexWidget vexWidget;

	private int savedUndoDepth;
	private boolean wasDirty;

	private final ListenerList<IVexEditorListener, VexEditorEvent> vexEditorListeners = new ListenerList<IVexEditorListener, VexEditorEvent>(IVexEditorListener.class);

	private final SelectionProvider selectionProvider = new SelectionProvider();

	/**
	 * Class constructor.
	 */
	public VexEditor() {
		debugging = VexPlugin.getDefault().isDebugging() && "true".equalsIgnoreCase(Platform.getDebugOption(VexPlugin.ID + "/debug/layout")); //$NON-NLS-1$ //$NON-NLS-2$
		configurationRegistry = VexPlugin.getDefault().getConfigurationRegistry();
		preferences = VexPlugin.getDefault().getPreferences();
	}

	/**
	 * Add a VexEditorListener to the notification list.
	 * 
	 * @param listener
	 *            VexEditorListener to be added.
	 */
	public void addVexEditorListener(final IVexEditorListener listener) {
		vexEditorListeners.add(listener);
	}

	@Override
	public void dispose() {
		super.dispose();

		if (parentControl != null) {
			// createPartControl was called, so we must de-register from config
			// events
			configurationRegistry.removeConfigListener(configListener);
		}

		if (getEditorInput() instanceof IFileEditorInput) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		}

	}

	@Override
	public void doSave(final IProgressMonitor monitor) {

		final IEditorInput input = getEditorInput();
		OutputStream os = null;
		try {
			resourceChangeListener.setSaving(true);
			final DocumentWriter writer = createDocumentWriter();

			if (input instanceof IFileEditorInput) {
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				writer.write(document, baos);
				baos.close();
				final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
				((IFileEditorInput) input).getFile().setContents(bais, false, false, monitor);
			} else {
				os = new FileOutputStream(((ILocationProvider) input).getPath(input).toFile());
				writer.write(document, os);
			}

			savedUndoDepth = vexWidget.getUndoDepth();
			firePropertyChange(IEditorPart.PROP_DIRTY);

		} catch (final Exception ex) {
			monitor.setCanceled(true);
			final String title = Messages.getString("VexEditor.errorSaving.title"); //$NON-NLS-1$
			final String message = MessageFormat.format(Messages.getString("VexEditor.errorSaving.message"), //$NON-NLS-1$
					new Object[] { input.getName(), ex.getMessage() });
			MessageDialog.openError(getEditorSite().getShell(), title, message);
			VexPlugin.getDefault().log(IStatus.ERROR, message, ex);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (final IOException e) {
				}
			}
			resourceChangeListener.setSaving(false);
		}
	}

	private DocumentWriter createDocumentWriter() {
		final DocumentWriter result = new DocumentWriter();
		result.setWhitespacePolicy(new CssWhitespacePolicy(style.getStyleSheet()));
		result.setIndent(preferences.getIndentationPattern());
		result.setWrapColumn(preferences.getLineWidth());
		return result;
	}

	@Override
	public void doSaveAs() {
		final SaveAsDialog dlg = new SaveAsDialog(getSite().getShell());
		final int result = dlg.open();
		if (result == Window.OK) {
			final IPath path = dlg.getResult();
			try {
				resourceChangeListener.setSaving(true);
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				final DocumentWriter writer = createDocumentWriter();
				writer.write(document, baos);
				baos.close();

				final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
				final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
				file.create(bais, false, null);

				final IFileEditorInput input = new FileEditorInput(file);
				setInput(input);
				savedUndoDepth = vexWidget.getUndoDepth();

				firePropertyChange(IEditorPart.PROP_DIRTY);
				firePropertyChange(IEditorPart.PROP_INPUT);
				firePropertyChange(IWorkbenchPart.PROP_TITLE);

			} catch (final Exception ex) {
				final String title = Messages.getString("VexEditor.errorSaving.title"); //$NON-NLS-1$
				final String message = MessageFormat.format(Messages.getString("VexEditor.errorSaving.message"), //$NON-NLS-1$
						new Object[] { path, ex.getMessage() });
				MessageDialog.openError(getEditorSite().getShell(), title, message);
				VexPlugin.getDefault().log(IStatus.ERROR, message, ex);
			} finally {
				resourceChangeListener.setSaving(false);
			}
		}

	}

	/**
	 * Return a reasonable style for the given doctype.
	 * 
	 * @param publicId
	 *            Public ID for which to return the style.
	 */
	public Style getPreferredStyle(final String publicId) {
		return configurationRegistry.getStyle(publicId, preferences.getPreferredStyleId(publicId));
	}

	/**
	 * Returns the DocumentType associated with this editor.
	 */
	public DocumentType getDocumentType() {
		return doctype;
	}

	/**
	 * Returns the Style currently associated with the editor. May be null.
	 */
	public Style getStyle() {
		return style;
	}

	/**
	 * Returns the VexWidget that implements this editor.
	 */
	public VexWidget getVexWidget() {
		return vexWidget;
	}

	public void gotoMarker(final IMarker marker) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {

		setSite(site);
		setInput(input);

		getEditorSite().setSelectionProvider(selectionProvider);
		getEditorSite().getSelectionProvider().addSelectionChangedListener(selectionChangedListener);

		if (input instanceof IFileEditorInput) {
			ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
		}
	}

	protected void loadInput() {

		if (vexWidget != null) {
			vexEditorListeners.fireEvent("documentUnloaded", new VexEditorEvent(this)); //$NON-NLS-1$
		}

		loaded = false;

		final IEditorInput input = getEditorInput();

		try {
			final long start = System.currentTimeMillis();

			final IFile file;

			if (input instanceof IFileEditorInput) {
				file = ((IFileEditorInput) input).getFile();
			} else {
				final String msg = MessageFormat.format(Messages.getString("VexEditor.unknownInputClass"), //$NON-NLS-1$
						new Object[] { input.getClass() });
				showLabel(msg);
				return;
			}

			final VexDocumentContentModel documentContentModel = new VexDocumentContentModel(getSite().getShell());
			final DocumentReader reader = new DocumentReader();
			reader.setDebugging(debugging);
			reader.setDocumentContentModel(documentContentModel);
			document = reader.read(file.getLocationURI().toURL());

			if (debugging) {
				final long end = System.currentTimeMillis();
				final String message = "Parsed document in " //$NON-NLS-1$
						+ (end - start) + "ms"; //$NON-NLS-1$
				System.out.println(message);
			}

			if (document == null) {
				showLabel(MessageFormat.format(Messages.getString("VexEditor.noContent"), file));
				return;
			}

			doctype = documentContentModel.getDocumentType();
			style = documentContentModel.getStyle();

			final Validator validator = new WTPVEXValidator(documentContentModel);
			if (validator != null) {
				document.setValidator(validator);
				if (debugging) {
					final long end = System.currentTimeMillis();
					System.out.println("Got validator in " + (end - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			showVexWidget();

			vexWidget.setDebugging(debugging);
			vexWidget.setDocument(document, style.getStyleSheet());

			if (documentContentModel.shouldAssignInferredDocumentType()) {
				document.setPublicID(doctype.getPublicId());
				document.setSystemID(doctype.getSystemId());
				doSave(null);
			}

			loaded = true;
			savedUndoDepth = vexWidget.getUndoDepth();
			firePropertyChange(IEditorPart.PROP_DIRTY);
			wasDirty = isDirty();

			vexEditorListeners.fireEvent("documentLoaded", new VexEditorEvent(this)); //$NON-NLS-1$

		} catch (final SAXParseException ex) {

			if (ex.getException() instanceof NoRegisteredDoctypeException) {
				// TODO doc did not have document type and the user
				// declined to select another one. Should fail silently.
				String msg;
				final NoRegisteredDoctypeException ex2 = (NoRegisteredDoctypeException) ex.getException();
				if (ex2.getPublicId() == null) {
					msg = Messages.getString("VexEditor.noDoctype"); //$NON-NLS-1$
				} else {
					msg = MessageFormat.format(Messages.getString("VexEditor.unknownDoctype"), //$NON-NLS-1$
							new Object[] { ex2.getPublicId() });
				}
				showLabel(msg);
			} else if (ex.getException() instanceof NoStyleForDoctypeException) {
				final String msg = MessageFormat.format(Messages.getString("VexEditor.noStyles"), //$NON-NLS-1$
						new Object[] { doctype.getPublicId() });
				showLabel(msg);
			} else {
				String file = ex.getSystemId();
				if (file == null) {
					file = input.getName();
				}

				final String msg = MessageFormat.format(Messages.getString("VexEditor.parseError"), //$NON-NLS-1$
						new Object[] { Integer.valueOf(ex.getLineNumber()), file, ex.getLocalizedMessage() });

				showLabel(msg);

				VexPlugin.getDefault().log(IStatus.ERROR, msg, ex);
			}

		} catch (final Exception ex) {

			final String msg = MessageFormat.format(Messages.getString("VexEditor.unexpectedError"), //$NON-NLS-1$
					new Object[] { input.getName() });

			VexPlugin.getDefault().log(IStatus.ERROR, msg, ex);

			showLabel(msg);
		}
	}

	@Override
	public boolean isDirty() {
		if (vexWidget != null) {
			return savedUndoDepth != vexWidget.getUndoDepth();
		} else {
			return false;
		}
	}

	/**
	 * Returns true if this editor has finished loading its document.
	 */
	public boolean isLoaded() {
		return loaded;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void createPartControl(final Composite parent) {

		parentControl = parent;

		configurationRegistry.addConfigListener(configListener);
		if (configurationRegistry.isLoaded()) {
			loadInput();
		} else {
			showLabel(Messages.getString("VexEditor.loading")); //$NON-NLS-1$
		}
	}

	/**
	 * Remove a VexEditorListener from the notification list.
	 * 
	 * @param listener
	 *            VexEditorListener to be removed.
	 */
	public void removeVexEditorListener(final IVexEditorListener listener) {
		vexEditorListeners.remove(listener);
	}

	@Override
	public void setFocus() {
		if (vexWidget != null) {
			vexWidget.setFocus();
			setStatus(getLocation());
		}
	}

	@Override
	protected void setInput(final IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
		setContentDescription(input.getName());
		//this.setTitleToolTip(input.getToolTipText());
	}

	public void setStatus(final String text) {
		// this.statusLabel.setText(text);
		getEditorSite().getActionBars().getStatusLineManager().setMessage(text);
	}

	/**
	 * Sets the style for this editor.
	 * 
	 * @param style
	 *            Style to use.
	 */
	public void setStyle(final Style style) {
		this.style = style;
		if (vexWidget != null) {
			vexWidget.setStyleSheet(style.getStyleSheet());
			preferences.setPreferredStyleId(document.getPublicID(), style.getUniqueId());
		}
	}

	private void showLabel(final String message) {
		if (loadingLabel == null) {
			if (vexWidget != null) {
				vexWidget.dispose();
				vexWidget = null;
			}
			loadingLabel = new Label(parentControl, SWT.WRAP);
		}
		loadingLabel.setText(message);
		parentControl.layout(true);
	}

	private void showVexWidget() {

		if (vexWidget != null) {
			return;
		}

		if (loadingLabel != null) {
			loadingLabel.dispose();
			loadingLabel = null;
		}

		final GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parentControl.setLayout(layout);
		GridData gd;

		// StatusPanel statusPanel = new StatusPanel(this.parentControl);

		// Composite statusPanel = new Composite(this.parentControl, SWT.NONE);
		// statusPanel.setLayout(new GridLayout());
		// gd = new GridData();
		// gd.grabExcessHorizontalSpace = true;
		// gd.horizontalAlignment = GridData.FILL;
		// statusPanel.setLayoutData(gd);

		// this.statusLabel = new Label(statusPanel, SWT.NONE);
		// gd = new GridData();
		// gd.grabExcessHorizontalSpace = true;
		// gd.horizontalAlignment = GridData.FILL;
		// this.statusLabel.setLayoutData(gd);

		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;

		vexWidget = new VexWidget(parentControl, SWT.V_SCROLL);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;
		vexWidget.setLayoutData(gd);

		final MenuManager menuManager = new MenuManager();
		getSite().registerContextMenu("org.eclipse.vex.ui.popup", menuManager, vexWidget);
		vexWidget.setMenu(menuManager.createContextMenu(vexWidget));

		savedUndoDepth = vexWidget.getUndoDepth();

		// new for scopes
		final IContextService cs = (IContextService) getSite().getService(IContextService.class);
		cs.activateContext("org.eclipse.vex.ui.VexEditorContext");

		vexWidget.addSelectionChangedListener(selectionProvider);

		parentControl.layout(true);

	}

	private void handleResourceChanged(final IResourceDelta delta) {

		if (delta.getKind() == IResourceDelta.CHANGED) {
			if ((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
				handleResourceContentChanged();
			}
		} else if (delta.getKind() == IResourceDelta.REMOVED) {
			if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
				final IPath toPath = delta.getMovedToPath();
				final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(toPath);
				setInput(new FileEditorInput(file));
			} else if (!isDirty()) {
				getEditorSite().getPage().closeEditor(this, false);
			} else {
				handleResourceDeleted();
			}
		}

	}

	private void handleResourceContentChanged() {

		if (!isDirty()) {
			loadInput();
		} else {

			final String message = MessageFormat.format(Messages.getString("VexEditor.docChanged.message"), //$NON-NLS-1$
					new Object[] { getEditorInput().getName() });

			final MessageDialog dlg = new MessageDialog(getSite().getShell(), Messages.getString("VexEditor.docChanged.title"), //$NON-NLS-1$
					null, message, MessageDialog.QUESTION, new String[] { Messages.getString("VexEditor.docChanged.discard"), //$NON-NLS-1$
							Messages.getString("VexEditor.docChanged.overwrite") }, //$NON-NLS-1$
					1);

			final int result = dlg.open();

			if (result == 0) {
				loadInput();
			} else {
				doSave(null);
			}
		}
	}

	private void handleResourceDeleted() {

		final String message = MessageFormat.format(Messages.getString("VexEditor.docDeleted.message"), //$NON-NLS-1$
				new Object[] { getEditorInput().getName() });

		final MessageDialog dlg = new MessageDialog(getSite().getShell(), Messages.getString("VexEditor.docDeleted.title"), //$NON-NLS-1$
				null, message, MessageDialog.QUESTION, new String[] { Messages.getString("VexEditor.docDeleted.discard"), //$NON-NLS-1$ 
						Messages.getString("VexEditor.docDeleted.save") }, //$NON-NLS-1$
				1);

		final int result = dlg.open();

		if (result == 0) {
			getEditorSite().getPage().closeEditor(this, false);
		} else { // Save

			doSaveAs();

			// Check if they saved or not. If not, close the editor
			if (!getEditorInput().exists()) {
				getEditorSite().getPage().closeEditor(this, false);
			}
		}
	}

	// Listen for stylesheet changes and respond appropriately
	private final IConfigListener configListener = new IConfigListener() {
		public void configChanged(final ConfigEvent e) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (style == null) {
						return;
					}

					final String styleId = style.getUniqueId();
					final Style newStyle = configurationRegistry.getStyle(styleId);
					if (newStyle == null) {
						// Oops, style went bye-bye
						// Let's just hold on to it in case it comes back later
					} else {
						vexWidget.setStyleSheet(newStyle.getStyleSheet());
						style = newStyle;
					}
				}
			});
		}

		public void configLoaded(final ConfigEvent e) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					loadInput();
				}
			});
		}
	};

	private final ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
		public void selectionChanged(final SelectionChangedEvent event) {
			if (isDirty() != wasDirty) {
				firePropertyChange(IEditorPart.PROP_DIRTY);
				wasDirty = isDirty();
			}
			setStatus(getLocation());

			// update dynamic UI element labels
			final IEditorSite editorSite = VexEditor.this.getEditorSite();
			final IWorkbenchWindow window = editorSite.getWorkbenchWindow();
			if (window instanceof IServiceLocator) {
				final IServiceLocator serviceLocator = window;
				final ICommandService commandService = (ICommandService) serviceLocator.getService(ICommandService.class);
				commandService.refreshElements(ConvertElementHandler.COMMAND_ID, null);
				commandService.refreshElements(RemoveTagHandler.COMMAND_ID, null);
			}

			// update context service
			final ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
			final DocumentContextSourceProvider contextProvider = (DocumentContextSourceProvider) service.getSourceProvider(DocumentContextSourceProvider.IS_COLUMN);
			contextProvider.fireUpdate(vexWidget);
		}
	};

	private class ResourceChangeListener implements IResourceChangeListener {

		public void resourceChanged(final IResourceChangeEvent event) {

			if (saving) {
				return;
			}

			final IPath path = ((IFileEditorInput) getEditorInput()).getFile().getFullPath();
			final IResourceDelta delta = event.getDelta().findMember(path);
			if (delta != null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						handleResourceChanged(delta);
					}
				});
			}
		}

		public void setSaving(final boolean saving) {
			this.saving = saving;
		}

		// Set to true so we can ignore change events while we're saving.
		private boolean saving;
	};

	private final ResourceChangeListener resourceChangeListener = new ResourceChangeListener();

	//
	// wsFactory communicates failures back to init() through the XML parser
	// by throwing one of these exceptions
	//

	private String getLocation() {
		final List<String> path = new ArrayList<String>();
		Element element = vexWidget.getCurrentElement();
		while (element != null) {
			path.add(element.getPrefixedName());
			element = element.getParentElement();
		}
		Collections.reverse(path);
		final StringBuilder sb = new StringBuilder(path.size() * 15);
		for (final String part : path) {
			sb.append("/"); //$NON-NLS-1$
			sb.append(part);
		}
		return sb.toString();
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {

		if (adapter == IContentOutlinePage.class) {
			return new DocumentOutlinePage();
		} else if (adapter == IPropertySheetPage.class) {

			final PropertySheetPage page = new PropertySheetPage();
			page.setPropertySourceProvider(new IPropertySourceProvider() {
				public IPropertySource getPropertySource(final Object object) {
					if (object instanceof Element) {
						final IStructuredSelection sel = (IStructuredSelection) vexWidget.getSelection();
						final boolean multi = sel != null && sel.size() > 1;
						final Validator validator = vexWidget.getDocument().getValidator();
						return new ElementPropertySource((Element) object, validator, multi);
					} else {
						return null;
					}
				}
			});
			return page;

		} else if (adapter == IFindReplaceTarget.class) {
			return new AbstractRegExFindReplaceTarget() {

				@Override
				protected int getSelectionStart() {
					return getVexWidget().getSelectedRange().getStartOffset();
				}

				@Override
				protected int getSelectionEnd() {
					return getVexWidget().getSelectedRange().getEndOffset();
				}

				@Override
				protected void setSelection(final int start, final int end) {
					getVexWidget().moveTo(start);
					getVexWidget().moveTo(end + 1, true);
				}

				@Override
				protected CharSequence getDocument() {
					return document.getContent();
				}

				@Override
				protected void inDocumentReplaceSelection(final CharSequence text) {
					final VexWidget vexWidget = getVexWidget();

					// because of Undo this action must be atomic
					vexWidget.beginWork();
					try {
						vexWidget.deleteSelection();
						vexWidget.insertText(text.toString());
					} finally {
						vexWidget.endWork(true);
					}
				}

			};
		} else {
			return super.getAdapter(adapter);
		}
	}

}
