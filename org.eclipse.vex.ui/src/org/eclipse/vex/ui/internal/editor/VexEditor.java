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
 *     Carsten Hiesserich - added styleChanged event
 *     Carsten Hiesserich - remove listeners on dispose (bug 413878)
 *     Carsten Hiesserich - use JFaceDocument as intermediate between VexDocument
 *                          and filesystem
 *******************************************************************************/
package org.eclipse.vex.ui.internal.editor;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.editors.text.IStorageDocumentProvider;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension;
import org.eclipse.ui.texteditor.IDocumentProviderExtension3;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.vex.core.internal.core.ListenerList;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.dom.DocumentTextPosition;
import org.eclipse.vex.core.internal.io.DocumentReader;
import org.eclipse.vex.core.internal.io.DocumentWriter;
import org.eclipse.vex.core.internal.validator.WTPVEXValidator;
import org.eclipse.vex.core.internal.widget.swt.VexWidget;
import org.eclipse.vex.core.provisional.dom.AttributeChangeEvent;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.ContentChangeEvent;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentListener;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IValidator;
import org.eclipse.vex.core.provisional.dom.NamespaceDeclarationChangeEvent;
import org.eclipse.vex.ui.internal.Messages;
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
import org.eclipse.vex.ui.internal.property.DocumentPropertySource;
import org.eclipse.vex.ui.internal.property.ElementPropertySource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/*
 * Vex uses a IDocumentProvider as an intermediate between the Vex document and the filesystem.
 * This way, we allow multiple editor instances for the same document (e.g. an XML editor together with Vex).
 *
 * When a new Vex Editor is opened:
 * - If the document is already open in another editor, Vex does not read the input file but synchronizes with
 *   the open document (see setInputFromProvider).
 * - If Vex is the first instance to open the document, we connect the EditorInput to a DocumentProvider, so
 *   other editor instances will use our unsaved document.
 *
 * Document synchronization:
 * - When Vex looses focus and the document is modified by Vex, the internal document is rewritten to the
 *   DocumentProvider (see synDocumentProvider).
 * - When Vex gets activated and the document has been changed externally, we reread the document from the
 *   DocumentProvider (see handleEditorInputChanged).
 */

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
	private Text loadingLabel;

	private boolean loaded;
	private DocumentType doctype;
	private IDocument document;
	private Style style;

	private VexWidget vexWidget;

	private boolean dirty;

	private final ListenerList<IVexEditorListener, VexEditorEvent> vexEditorListeners = new ListenerList<IVexEditorListener, VexEditorEvent>(IVexEditorListener.class);

	private final SelectionProvider selectionProvider = new SelectionProvider();

	private IDocumentProvider documentProvider;

	private ActivationListener activationListener;
	private IElementStateListener elementStateListener = new ElementStateListener();
	private final org.eclipse.jface.text.IDocumentListener jFaceDocumentListener = new JFaceDocumentListener();
	/** Indicates activation should be handled. */
	private boolean handleActivation = true;
	/** Indicates JFace Document change should be handled */
	private boolean handleDocumentChange = true;
	/** Tells whether this editor has been activated at least once. */
	private boolean hasBeenActivated;
	/** Indicates the document has been modified outside of VEX */
	private boolean externalModified = false;
	/** Indicates the document has been modified by VEX */
	private boolean internalModified = false;
	/** The modification stamp of the current document */
	private long docModificationStamp;

	private DocumentTextPosition positionOfCurrentNode = null;

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

		if (activationListener != null) {
			activationListener.dispose();
			activationListener = null;
		}

		if (document != null) {
			document.removeDocumentListener(documentListener);
		}

		getEditorSite().getSelectionProvider().removeSelectionChangedListener(selectionChangedListener);

		if (parentControl != null) {
			// createPartControl was called, so we must de-register from config
			// events
			configurationRegistry.removeConfigListener(configListener);
		}

		disposeDocumentProvider();
	}

	@Override
	public void doSave(final IProgressMonitor monitor) {

		final IDocumentProvider p = getDocumentProvider();
		if (p == null) {
			return;
		}

		if (p.isDeleted(getEditorInput())) {
			if (isSaveAsAllowed()) {
				performSaveAs(monitor);
			} else {
				final Shell shell = getSite().getShell();
				final String title = Messages.getString("VexEditor.docDeleted.canNotSave.title");
				final String msg = Messages.getString("VexEditor.docDeleted.canNotSave.title");
				MessageDialog.openError(shell, title, msg);
			}
		} else {
			performSave(false, monitor);
		}
	}

	/**
	 * Performs the save and handles errors appropriately.
	 * 
	 * @param overwrite
	 *            indicates whether or not overwriting is allowed
	 * @param progressMonitor
	 *            the monitor in which to run the operation
	 */
	protected void performSave(final boolean overwrite, final IProgressMonitor progressMonitor) {
		final IDocumentProvider provider = getDocumentProvider();
		if (provider == null) {
			return;
		}

		handleActivation = false;
		try {
			provider.aboutToChange(getEditorInput());
			syncDocumentProvider();
			final IEditorInput input = getEditorInput();
			provider.saveDocument(progressMonitor, input, getDocumentProvider().getDocument(input), overwrite);
		} catch (final CoreException ex) {
			final String title = Messages.getString("VexEditor.errorSaving.title"); //$NON-NLS-1$
			final String message = MessageFormat.format(Messages.getString("VexEditor.errorSaving.message"), //$NON-NLS-1$
					getEditorInput().getName(), ex.getMessage());
			MessageDialog.openError(getEditorSite().getShell(), title, message);
			VexPlugin.getDefault().log(IStatus.ERROR, message, ex);
		} finally {
			provider.changed(getEditorInput());
			setClean();
			handleActivation = true;
		}
	}

	/**
	 * Asks the user for the workspace path of a file resource and saves the document there.
	 * 
	 * @param progressMonitor
	 *            the monitor in which to run the operation
	 */
	protected void performSaveAs(final IProgressMonitor progressMonitor) {
		final Shell shell = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
		final IDocumentProvider provider = getDocumentProvider();
		final IEditorInput input = getEditorInput();
		final IEditorInput newInput;

		if (input instanceof IURIEditorInput && !(input instanceof IFileEditorInput)) {
			final FileDialog dialog = new FileDialog(shell, SWT.SAVE);
			final IPath oldPath = URIUtil.toPath(((IURIEditorInput) input).getURI());
			if (oldPath != null) {
				dialog.setFileName(oldPath.lastSegment());
				dialog.setFilterPath(oldPath.toOSString());
			}
			final String path = dialog.open();
			if (path == null) {
				if (progressMonitor != null) {
					progressMonitor.setCanceled(true);
				}
				return;
			}

			// Check whether file exists and if so, confirm overwrite
			final File localFile = new File(path);
			if (localFile.exists()) {
				final String title = Messages.getString("VexEditor.saveAs.overwrite.title");
				final String message = MessageFormat.format(Messages.getString("VexEditor.saveAs.overwrite.message"), path);
				final MessageDialog overwriteDialog = new MessageDialog(shell, title, null, message, MessageDialog.WARNING, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 1); // 'No' is the default
				if (overwriteDialog.open() != Window.OK) {
					if (progressMonitor != null) {
						progressMonitor.setCanceled(true);
						return;
					}
				}
			}

			IFileStore fileStore;
			try {
				fileStore = EFS.getStore(localFile.toURI());
			} catch (final CoreException ex) {
				VexPlugin.getDefault().log(IStatus.ERROR, ex.getLocalizedMessage(), ex);
				final String title = Messages.getString("VexEditor.saveAs.error.title");
				final String msg = MessageFormat.format(Messages.getString("VexEditor.saveAs.error.message"), ex.getLocalizedMessage());
				MessageDialog.openError(shell, title, msg);
				return;
			}

			final IFile file = getWorkspaceFile(fileStore);
			if (file != null) {
				newInput = new FileEditorInput(file);
			} else {
				newInput = new FileStoreEditorInput(fileStore);
			}
		} else {
			final SaveAsDialog dialog = new SaveAsDialog(shell);

			final IFile original = input instanceof IFileEditorInput ? ((IFileEditorInput) input).getFile() : null;
			if (original != null) {
				dialog.setOriginalFile(original);
			} else {
				dialog.setOriginalName(input.getName());
			}

			dialog.create();

			if (provider.isDeleted(input) && original != null) {
				final String msg = MessageFormat.format(Messages.getString("VexEditor.saveAs.deleted"), original.getName());
				dialog.setErrorMessage(null);
				dialog.setMessage(msg, IMessageProvider.WARNING);
			}

			if (dialog.open() == Window.CANCEL) {
				if (progressMonitor != null) {
					progressMonitor.setCanceled(true);
				}
				return;
			}

			final IPath filePath = dialog.getResult();
			if (filePath == null) {
				if (progressMonitor != null) {
					progressMonitor.setCanceled(true);
				}
				return;
			}

			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			final IFile file = workspace.getRoot().getFile(filePath);
			newInput = new FileEditorInput(file);
		}

		if (provider == null) {
			// editor has programmatically been  closed while the dialog was open
			return;
		}

		boolean success = false;
		try {
			provider.aboutToChange(newInput);
			syncDocumentProvider();
			provider.saveDocument(progressMonitor, newInput, provider.getDocument(input), true);
			success = true;
		} catch (final CoreException ex) {
			final IStatus status = ex.getStatus();
			if (status == null || status.getSeverity() != IStatus.CANCEL) {
				VexPlugin.getDefault().log(IStatus.ERROR, ex.getLocalizedMessage(), ex);
				final String title = Messages.getString("VexEditor.saveAs.error.title");
				final String msg = MessageFormat.format(Messages.getString("VexEditor.saveAs.error.message"), ex.getLocalizedMessage());
				MessageDialog.openError(shell, title, msg);
			}
		} finally {
			provider.changed(newInput);
			if (success) {
				setInput(newInput);
				setClean();
			}
		}

		if (progressMonitor != null) {
			progressMonitor.setCanceled(!success);
		}
	}

	private IFile getWorkspaceFile(final IFileStore fileStore) {
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		final IFile[] files = workspaceRoot.findFilesForLocationURI(fileStore.toURI());
		if (files != null && files.length == 1) {
			return files[0];
		}
		return null;
	}

	/**
	 * Write the current state of the document to the DocumentProvider if the document has been modfied by Vex.
	 */
	private void syncDocumentProvider() {
		if (!loaded) {
			return;
		}

		final IDocumentProvider provider = getDocumentProvider();
		if (provider == null) {
			return;
		}

		handleDocumentChange = false; // Disable document change handling

		final org.eclipse.jface.text.IDocument jFaceDoc = getDocumentProvider().getDocument(getEditorInput());
		final org.eclipse.jface.text.IDocument doc;
		if (internalModified) {
			doc = jFaceDoc;
		} else {
			// Document is not modfied, so we don't touch it
			// The dummy document is used to store the caret position
			doc = new org.eclipse.jface.text.Document();
		}

		try {
			provider.aboutToChange(getEditorInput());

			if (positionOfCurrentNode != null) {
				jFaceDoc.removePosition(positionOfCurrentNode);
			}

			positionOfCurrentNode = createDocumentWriter().write(document, doc, vexWidget.getCurrentNode());
			positionOfCurrentNode.setOffsetInNode(vexWidget.getCaretPosition().getOffset() - vexWidget.getCurrentNode().getStartPosition().getOffset());

			try {
				jFaceDoc.addPosition(positionOfCurrentNode);
			} catch (final BadLocationException e) {
				// That should not happen
				e.printStackTrace();
			}
		} finally {
			provider.changed(getEditorInput());
			internalModified = false;
			handleDocumentChange = true;
		}
	}

	private DocumentWriter createDocumentWriter() {
		final DocumentWriter result = new DocumentWriter();
		result.setWhitespacePolicy(vexWidget.getWhitespacePolicy());
		result.setIndent(preferences.getIndentationPattern());
		result.setWrapColumn(preferences.getLineWidth());
		return result;
	}

	@Override
	public void doSaveAs() {
		performSaveAs(getProgressMonitor());
	}

	/**
	 * Returns the progress monitor related to this editor.
	 * 
	 * @return the progress monitor related to this editor
	 */
	protected IProgressMonitor getProgressMonitor() {
		IProgressMonitor pm = null;
		final IStatusLineManager manager = getEditorSite().getActionBars().getStatusLineManager();
		;
		if (manager != null) {
			pm = manager.getProgressMonitor();
		}
		return pm != null ? pm : new NullProgressMonitor();
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

		activationListener = new ActivationListener(site.getWorkbenchWindow().getPartService());
	}

	/**
	 * @see ITextEditorExtension#isEditorInputReadOnly()
	 */
	public boolean isEditorInputReadOnly() {
		final IDocumentProvider provider = getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension) {
			final IDocumentProviderExtension extension = (IDocumentProviderExtension) provider;
			return extension.isReadOnly(getEditorInput());
		}
		return true;
	}

	private void setDirty() {
		internalModified = true;

		if (dirty) {
			return;
		}
		dirty = true;
		firePropertyChange(PROP_DIRTY);

		makeJFaceDocumentDirty();
	}

	/**
	 * Mark the document in the DocumentProvider dirty.
	 */
	private void makeJFaceDocumentDirty() {
		// Replace a part of the JFace document to mark it dirty. This is a simple hack.
		// The best behavior would be to directly modify the JFace document with the changes
		// made in VEX.
		final org.eclipse.jface.text.IDocument doc = getDocumentProvider().getDocument(getEditorInput());
		try {
			handleDocumentChange = false; // Disable change handling
			doc.replace(0, 1, doc.get(0, 1));
		} catch (final BadLocationException e) {
			e.printStackTrace();
		} finally {
			handleDocumentChange = true;
		}
	}

	private void setClean() {
		if (!dirty) {
			return;
		}

		dirty = false;
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public boolean isDirty() {
		return dirty;
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
	public boolean isSaveOnCloseNeeded() {
		// do not save if there is no document loaded
		// this method is not called in some situations. see bug Bug 411465
		// as a workaround we call setClean when no document is loaded.
		return isLoaded() && isDirty();
	}

	@Override
	public void createPartControl(final Composite parent) {
		parentControl = parent;

		configurationRegistry.addConfigListener(configListener);
		if (configurationRegistry.isLoaded()) {
			setInputFromProvider();
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
			setStatus(getLocationPath());
		}
	}

	/**
	 * Link the given IEditorInput with the IDocumentProvider. To make the new input available to Vex, the document has
	 * to be loaded from the provider. See {@link #setInputFromProvider()}
	 */
	@Override
	protected void setInput(final IEditorInput input) {
		final IEditorInput oldInput = getEditorInput();
		if (oldInput != null) {
			getDocumentProvider().getDocument(oldInput).removeDocumentListener(jFaceDocumentListener);
			getDocumentProvider().disconnect(oldInput);
		}

		super.setInput(input);

		setPartName(input.getName());
		setContentDescription(input.getName()); // Content description is displayed in the top line of the view

		updateDocumentProvider(input);
		final IDocumentProvider provider = getDocumentProvider();
		if (provider == null) {
			final String msg = MessageFormat.format(Messages.getString("VexEditor.noProvider"), //$NON-NLS-1$
					new Object[] { input.getClass() });
			showLabel(msg);
			return;
		}

		try {
			provider.connect(input);
		} catch (final CoreException e) {
			e.printStackTrace();
		}

		provider.getDocument(input).addDocumentListener(jFaceDocumentListener);
	}

	/**
	 * Read the input from the document provider and create the Vex-Document.
	 */
	protected void setInputFromProvider() {

		loaded = false;
		dirty = false; // Mark the document clean, to prevent the 'Save...' dialog when loading fails

		final IDocumentProvider provider = getDocumentProvider();

		IValidator validator = null;
		VexDocumentContentModel documentContentModel;

		try {
			if (vexWidget != null) {
				vexEditorListeners.fireEvent("documentUnloaded", new VexEditorEvent(this)); //$NON-NLS-1$
			}
			if (document != null) {
				document.removeDocumentListener(documentListener);
				validator = document.getValidator();
			}

			// Reuse the validator from current document
			if (validator != null) {
				documentContentModel = (VexDocumentContentModel) validator.getDocumentContentModel();
			} else {
				documentContentModel = new VexDocumentContentModel(getSite().getShell());
				validator = new WTPVEXValidator(documentContentModel);
			}

			final DocumentReader reader = new DocumentReader();
			reader.setDebugging(debugging);
			reader.setValidator(validator);
			reader.setStyleSheetProvider(VexPlugin.getDefault().getPreferences());
			reader.setWhitespacePolicyFactory(CssWhitespacePolicy.FACTORY);

			final org.eclipse.jface.text.IDocument jFaceDoc = provider.getDocument(getEditorInput());

			if (provider instanceof IDocumentProviderExtension) {
				final IDocumentProviderExtension extension = (IDocumentProviderExtension) getDocumentProvider();
				final IStatus status = extension.getStatus(getEditorInput());
				if (status != null && status.getSeverity() == IStatus.ERROR) {
					// There's something wrong with the input. Show the message.
					// Typically this happes when the input is not in sync with the file system
					showLabel(status.getMessage());
					return;
				}
			}

			String encoding;
			if (provider instanceof IStorageDocumentProvider) {
				// Try to get the encoding from the DocumentProvider
				encoding = ((IStorageDocumentProvider) provider).getEncoding(getEditorInput());
			} else {
				encoding = "UTF-8";
			}

			// A Reader is used here to avoid an in memory copy of the documents content
			final InputSource is = new InputSource(new DocumentInputReader(jFaceDoc));
			is.setEncoding(encoding);

			// Set the systemId of the InputSource to resolve relative URIs
			final IEditorInput input = getEditorInput();
			if (input instanceof IFileEditorInput) {
				final IFile file = ((IFileEditorInput) input).getFile();
				is.setSystemId(file.getLocationURI().toString());
			} else if (input instanceof IStorageEditorInput) {
				final IStorage storage = ((IStorageEditorInput) input).getStorage();
				is.setSystemId(storage.getFullPath().toString());
			} else if (input instanceof IURIEditorInput) {
				final URI uri = ((IURIEditorInput) input).getURI();
				is.setSystemId(uri.toString());
			} else {
				final String msg = MessageFormat.format(Messages.getString("VexEditor.unknownInputClass"), //$NON-NLS-1$
						new Object[] { input.getClass() });
				showLabel(msg);
				return;
			}

			if (positionOfCurrentNode != null) {
				positionOfCurrentNode.computePosition(jFaceDoc);
				reader.setCaretPosition(positionOfCurrentNode);
			}
			document = reader.read(is);
			if (document == null) {
				showLabel(MessageFormat.format(Messages.getString("VexEditor.noContent"), getEditorInput().getName()));
				return;
			}

			doctype = documentContentModel.getDocumentType();
			style = documentContentModel.getStyle();
			// The document reader uses the style sheet before the document is completely loaded
			// This results in imcomplete styles in the cache
			style.getStyleSheet().flushAllStyles(document);

			document.setValidator(validator);

			if (documentContentModel.shouldAssignInferredDocumentType()) {
				// The user has selected to apply the selected DocType to the document
				// FIXME This does currently work for DTD's only and fails for namespaces
				document.setPublicID(doctype.getPublicId());
				document.setSystemID(doctype.getSystemId());
				internalModified = true;
				syncDocumentProvider();
				setDirty();
			}

			showVexWidget();

			document.addDocumentListener(documentListener);

			vexWidget.setDebugging(debugging);
			vexWidget.setWhitespacePolicy(reader.getWhitespacePolicy());
			vexWidget.setDocument(document, style.getStyleSheet());
			vexWidget.setReadOnly(isEditorInputReadOnly());

			final INode nodeAtCaret = reader.getNodeAtCaret();
			if (nodeAtCaret != null) {
				final int offsetInNode = Math.min(nodeAtCaret.getStartOffset() + positionOfCurrentNode.getOffsetInNode(), nodeAtCaret.getEndOffset());
				vexWidget.moveTo(new ContentPosition(null, offsetInNode));
			}

			loaded = true;
			// Check if the loaded document is in sync with the filesystem
			if (provider.canSaveDocument(getEditorInput())) {
				dirty = true;
			}
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
					msg = MessageFormat.format(Messages.getString("VexEditor.unknownDoctype"), ex2.getPublicId()); //$NON-NLS-1$
				}
				showLabel(msg);
			} else if (ex.getException() instanceof NoStyleForDoctypeException) {
				final String msg = MessageFormat.format(Messages.getString("VexEditor.noStyles"), doctype.getPublicId());//$NON-NLS-1$
				showLabel(msg);
			} else {
				String file = ex.getSystemId();
				if (file == null) {
					file = getEditorInput().getName();
				}
				final String msg = MessageFormat.format(Messages.getString("VexEditor.parseError"), Integer.valueOf(ex.getLineNumber()), file, ex.getLocalizedMessage()); //$NON-NLS-1$
				showLabel(msg);
				VexPlugin.getDefault().log(IStatus.ERROR, msg, ex);
			}
		} catch (final NoRegisteredDoctypeException ex) {
			if (ex.isUserCanceled()) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(this, true);
			} else {
				final String msg = MessageFormat.format(Messages.getString("VexEditor.unexpectedError"), getEditorInput().getName()); //$NON-NLS-1$
				VexPlugin.getDefault().log(IStatus.ERROR, msg, ex);
				showLabel(msg, ex);
			}
		} catch (final Exception ex) {
			final String msg = MessageFormat.format(Messages.getString("VexEditor.unexpectedError"), getEditorInput().getName()); //$NON-NLS-1$
			VexPlugin.getDefault().log(IStatus.ERROR, msg, ex);
			showLabel(msg, ex);
		}

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
			preferences.setPreferredStyleId(doctype, style.getUniqueId());
		}
		vexEditorListeners.fireEvent("styleChanged", new VexEditorEvent(this)); //$NON-NLS-1$
	}

	/**
	 * Dispose the VexWidget and display a message instead.
	 * 
	 * @param message
	 *            The message to display.
	 * @param ex
	 *            The Exception to display
	 */
	private void showLabel(final String message, final Exception ex) {
		if (loadingLabel == null) {
			if (vexWidget != null) {
				vexWidget.dispose();
				vexWidget = null;
			}
			final GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			layout.verticalSpacing = 0;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			parentControl.setLayout(layout);

			loadingLabel = new Text(parentControl, SWT.WRAP | SWT.V_SCROLL);
			loadingLabel.setEditable(false);
			final GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.grabExcessVerticalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			gd.verticalAlignment = GridData.FILL;
			loadingLabel.setLayoutData(gd);
		}
		final StringWriter sw = new StringWriter();
		sw.append(message);
		if (ex != null) {
			sw.append("\n\n");
			ex.printStackTrace(new PrintWriter(sw));
		}
		loadingLabel.setText(sw.toString());
		parentControl.layout(true);
	}

	/**
	 * Dispose the VexWidget and display a message instead.
	 * 
	 * @param message
	 *            The message to display.
	 */
	private void showLabel(final String message) {
		showLabel(message, null);
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

		setClean();

		// new for scopes
		final IContextService cs = (IContextService) getSite().getService(IContextService.class);
		cs.activateContext("org.eclipse.vex.ui.VexEditorContext");

		vexWidget.addSelectionChangedListener(selectionProvider);

		parentControl.layout(true);

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
					setInputFromProvider();
				}
			});
		}
	};

	private final ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
		public void selectionChanged(final SelectionChangedEvent event) {
			setStatus(getLocationPath());

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

	private final IDocumentListener documentListener = new IDocumentListener() {

		public void attributeChanged(final AttributeChangeEvent e) {
			setDirty();
		}

		public void namespaceChanged(final NamespaceDeclarationChangeEvent e) {
			setDirty();
		}

		public void beforeContentDeleted(final ContentChangeEvent e) {
			// TODO Auto-generated method stub

		}

		public void beforeContentInserted(final ContentChangeEvent e) {
			// TODO Auto-generated method stub

		}

		public void contentDeleted(final ContentChangeEvent e) {
			setDirty();
		}

		public void contentInserted(final ContentChangeEvent e) {
			setDirty();
		}

	};

	/**
	 * Visitor to return the path elements displayed in the status line
	 */
	private final INodeVisitorWithResult<String> nodePathVisitor = new BaseNodeVisitorWithResult<String>("") {
		@Override
		public String visit(final IElement element) {
			return element.getPrefixedName();
		};

		@Override
		public String visit(final IProcessingInstruction pi) {
			return Messages.getString("VexEditor.Path.ProcessingInstruction");
		};

		@Override
		public String visit(final IComment comment) {
			return Messages.getString("VexEditor.Path.Comment");
		};
	};

	private String getLocationPath() {
		final List<String> path = new ArrayList<String>();
		INode node = vexWidget.getCurrentNode();
		while (node != null) {
			path.add(node.accept(nodePathVisitor));
			node = node.getParent();
		}
		Collections.reverse(path);

		if (path.isEmpty()) {
			return "/";
		}

		final StringBuilder pathString = new StringBuilder(path.size() * 15);
		for (final String part : path) {
			if (!part.isEmpty()) {
				pathString.append('/');
				pathString.append(part);
			}
		}
		return pathString.toString();
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
		if (adapter == IContentOutlinePage.class) {
			return new DocumentOutlinePage(this);
		} else if (adapter == IPropertySheetPage.class) {
			final PropertySheetPage page = new PropertySheetPage();
			page.setPropertySourceProvider(new IPropertySourceProvider() {
				public IPropertySource getPropertySource(final Object object) {
					if (object instanceof IElement) {
						final IStructuredSelection selection = (IStructuredSelection) vexWidget.getSelection();
						final boolean multipleElementsSelected = selection != null && selection.size() > 1;
						final IValidator validator = vexWidget.getDocument().getValidator();
						return new ElementPropertySource((IElement) object, validator, multipleElementsSelected);
					}
					if (object instanceof IDocument) {
						return new DocumentPropertySource((IDocument) object);
					}
					return null;
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
					getVexWidget().moveTo(new ContentPosition(null, start));
					getVexWidget().moveTo(new ContentPosition(null, end + 1), true);
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

	private class ElementStateListener implements IElementStateListener {

		private Display display;

		@Override
		public void elementDirtyStateChanged(final Object element, final boolean isDirty) {
			if (element != null && element.equals(getEditorInput()) && dirty != isDirty) {
				final Runnable r = new Runnable() {
					public void run() {
						handleActivation = true;
						if (isDirty) {
							// Do not use setDirty() here, as this method will change the jFaceDocument to mark
							// is also as dirty. With multiple Vex editors open, that would trigger an infinite loop.
							dirty = true;
							firePropertyChange(PROP_DIRTY);
						} else {
							setClean();
						}
					}
				};
				execute(r);
			}
		}

		@Override
		public void elementContentAboutToBeReplaced(final Object element) {

		}

		@Override
		public void elementContentReplaced(final Object element) {

		}

		@Override
		public void elementDeleted(final Object element) {
			// The default behavior of Eclipse editors is to simply close when the document is deleted
			getSite().getPage().closeEditor(VexEditor.this, false);
		}

		@Override
		public void elementMoved(final Object originalElement, final Object movedElement) {
			if (originalElement != null && originalElement.equals(getEditorInput())) {

				// TODO: The undo/redo history gets lost during the reload
				final Runnable r = new Runnable() {
					public void run() {
						if (movedElement == null || movedElement instanceof IEditorInput) {

							// Store the current content if it is modified by VEX
							final IDocumentProvider provider = getDocumentProvider();
							final String previousContent;
							if (internalModified) {
								syncDocumentProvider();
								previousContent = provider.getDocument(getEditorInput()).get();
							} else {
								previousContent = null;
							}

							setInput((IEditorInput) movedElement);

							// Apply the stored changes to the new element
							if (previousContent != null) {
								getDocumentProvider().getDocument(getEditorInput()).set(previousContent);
							}

						}
					}
				};
				execute(r);

			}
		}

		/**
		 * Executes the given runnable in the UI thread.
		 * 
		 * @param runnable
		 *            runnable to be executed
		 */
		private void execute(final Runnable runnable) {
			if (Display.getCurrent() == null) {
				if (display == null) {
					display = getSite().getShell().getDisplay();
				}
				display.asyncExec(runnable);
			} else {
				runnable.run();
			}
		}

	}

	private class JFaceDocumentListener implements org.eclipse.jface.text.IDocumentListener {

		@Override
		public void documentAboutToBeChanged(final DocumentEvent event) {
		}

		@Override
		public void documentChanged(final DocumentEvent event) {

			if (handleDocumentChange) {
				externalModified = true;
				// Do not use setDirty() here, as that would mark as internalModfied
				if (!dirty) {
					dirty = true;
					firePropertyChange(PROP_DIRTY);
				}
			}
		}
	}

	/**
	 * Handles the activation of this editor.
	 */
	private void handleEditorActivated() {
		final IDocumentProvider p = getDocumentProvider();
		if (p == null) {
			return;
		}

		final boolean fileChange = checkDocumentState();
		if (fileChange && hasBeenActivated) {
			handleEditorInputChanged();
		} else if (externalModified) {
			setInputFromProvider();
		}
		externalModified = false;
	}

	/**
	 * Handle an external change of the editor's input
	 */
	private void handleEditorInputChanged() {
		final IDocumentProvider provider = getDocumentProvider();
		final IEditorInput input = getEditorInput();

		if (provider.isDeleted(input)) {
			final String title = Messages.getString("VexEditor.docDeleted.title");
			final String message = MessageFormat.format(Messages.getString("VexEditor.docDeleted.message"), input.getName()); //$NON-NLS-1$
			final String[] buttons = { Messages.getString("VexEditor.docDeleted.save"), Messages.getString("VexEditor.docDeleted.discard") };
			final MessageDialog dialog = new MessageDialog(getSite().getShell(), title, null, message, MessageDialog.QUESTION, buttons, 0);

			if (dialog.open() == 0) {
				final IProgressMonitor pm = getProgressMonitor();
				performSaveAs(pm);
				if (pm.isCanceled()) {
					handleEditorInputChanged();
				}
			} else {
				getEditorSite().getPage().closeEditor(this, false);
			}

		} else {
			final String title = Messages.getString("VexEditor.docChanged.title");
			final String message = MessageFormat.format(Messages.getString("VexEditor.docChanged.message"), getEditorInput().getName()); //$NON-NLS-1$

			// Ask the user if the document should be reloaded from the modfied input
			if (MessageDialog.openQuestion(getSite().getShell(), title, message)) {
				if (provider instanceof IDocumentProviderExtension) {
					final IDocumentProviderExtension extension = (IDocumentProviderExtension) provider;
					try {
						extension.synchronize(input);
					} catch (final CoreException e) {
						e.printStackTrace();
					}
				}
				setInputFromProvider();
			} else if (!isDirty()) {
				// Trigger dummy change to dirty the editor.
				try {
					final org.eclipse.jface.text.IDocument document = provider.getDocument(input);
					if (document != null) {
						document.replace(0, 0, ""); //$NON-NLS-1$
					}
				} catch (final BadLocationException e) {
					// Ignore as this can't happen
				}
			}
		}
	}

	/**
	 * Checks the state of the current document against the filesystem.
	 * 
	 * @return <code>true</code> if the document has been modified on the filesystem
	 */
	private boolean checkDocumentState() {

		final IDocumentProvider p = getDocumentProvider();
		if (p == null) {
			return false;
		}

		final IEditorInput input = getEditorInput();

		if (p instanceof IDocumentProviderExtension3) {

			final IDocumentProviderExtension3 p3 = (IDocumentProviderExtension3) p;

			final long stamp = p.getModificationStamp(input);
			if (stamp != docModificationStamp) {
				docModificationStamp = stamp;
				if (!p3.isSynchronized(input)) {
					return true;
				}
			}
		} else {
			if (docModificationStamp == -1) {
				docModificationStamp = p.getSynchronizationStamp(input);
			}

			final long stamp = p.getModificationStamp(input);
			if (stamp != docModificationStamp) {
				docModificationStamp = stamp;
				if (stamp != p.getSynchronizationStamp(input)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Internal part and shell activation listener for triggering state validation.<br />
	 * This class is copied from {@link org.eclipse.ui.texteditor.AbstractTextEditor}
	 */
	class ActivationListener implements IPartListener, IWindowListener {

		/** Cache of the active workbench part. */
		private IWorkbenchPart fActivePart;
		/** The part service. */
		private IPartService fPartService;

		/**
		 * Creates this activation listener.
		 * 
		 * @param partService
		 *            the part service on which to add the part listener
		 */
		public ActivationListener(final IPartService partService) {
			fPartService = partService;
			fPartService.addPartListener(this);
			PlatformUI.getWorkbench().addWindowListener(this);
		}

		/**
		 * Disposes this activation listener.
		 */
		public void dispose() {
			fPartService.removePartListener(this);
			PlatformUI.getWorkbench().removeWindowListener(this);
			fPartService = null;
		}

		/*
		 * @see IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partActivated(final IWorkbenchPart part) {
			fActivePart = part;
			handleActivation();
		}

		/*
		 * @see IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partBroughtToTop(final IWorkbenchPart part) {
		}

		/*
		 * @see IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partClosed(final IWorkbenchPart part) {
		}

		/*
		 * @see IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partDeactivated(final IWorkbenchPart part) {
			if (fActivePart == VexEditor.this || fActivePart != null && fActivePart.getAdapter(AbstractTextEditor.class) == VexEditor.this) {
				syncDocumentProvider();
			}
			fActivePart = null;
		}

		/*
		 * @see IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partOpened(final IWorkbenchPart part) {

		}

		/**
		 * Handles the activation triggering a element state check in the editor.
		 */
		private void handleActivation() {
			if (!handleActivation) {
				return;
			}

			if (fActivePart == VexEditor.this || fActivePart != null && fActivePart.getAdapter(AbstractTextEditor.class) == VexEditor.this) {
				handleActivation = false;
				try {
					handleEditorActivated();
				} finally {
					handleActivation = true;
					hasBeenActivated = true;
				}
			}
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowActivated(final IWorkbenchWindow window) {
			if (handleActivation && window == getEditorSite().getWorkbenchWindow()) {
				/*
				 * Workaround for problem described in http://dev.eclipse.org/bugs/show_bug.cgi?id=11731 Will be removed
				 * when SWT has solved the problem.
				 */
				window.getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						handleActivation();
					}
				});
			}
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowDeactivated(final IWorkbenchWindow window) {
			if (window == getEditorSite().getWorkbenchWindow()) {
				syncDocumentProvider();
			}
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowClosed(final IWorkbenchWindow window) {
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowOpened(final IWorkbenchWindow window) {
		}
	}

	/**
	 * Sets the DocumentProvider for the given EditorInput.
	 */
	protected void setDocumentProvider(final IEditorInput input) {
		documentProvider = DocumentProviderRegistry.getDefault().getDocumentProvider(input);
	}

	/**
	 * If there is no explicit document provider set, the implicit one is re-initialized based on the given editor
	 * input.
	 * 
	 * @param input
	 *            the editor input.
	 */
	private void updateDocumentProvider(final IEditorInput input) {
		IDocumentProvider provider = getDocumentProvider();
		if (provider != null) {
			provider.removeElementStateListener(elementStateListener);
		}

		setDocumentProvider(input);

		provider = getDocumentProvider();
		if (provider != null) {
			provider.addElementStateListener(elementStateListener);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.ITextEditor#getDocumentProvider()
	 */
	public IDocumentProvider getDocumentProvider() {
		return documentProvider;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#disposeDocumentProvider()
	 */
	protected void disposeDocumentProvider() {
		final IDocumentProvider provider = getDocumentProvider();
		if (provider != null) {
			final IEditorInput input = getEditorInput();
			if (input != null) {
				provider.getDocument(input).removeDocumentListener(jFaceDocumentListener);
				provider.disconnect(input);
			}

			if (elementStateListener != null) {
				provider.removeElementStateListener(elementStateListener);
				elementStateListener = null;
			}
		}

	}
}
