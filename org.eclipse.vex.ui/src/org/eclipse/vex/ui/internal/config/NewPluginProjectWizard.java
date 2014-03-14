/*******************************************************************************
 * Copyright (c) 2004, 2014 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - fix illegal inheritance from BasicNewProjectResourceWizard
 *******************************************************************************/
package org.eclipse.vex.ui.internal.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.eclipse.vex.ui.internal.VexPlugin;

public class NewPluginProjectWizard extends Wizard implements INewWizard, IExecutableExtension {

	private IWorkbench workbench;
	private IConfigurationElement configurationElement;

	private WizardNewProjectCreationPage mainPage;
	private WizardNewProjectReferencePage referencePage;

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection selection) {
		this.workbench = workbench;

		setWindowTitle(Messages.getString("NewPluginProjectWizard.title")); //$NON-NLS-1$
	}

	@Override
	public void setInitializationData(final IConfigurationElement configurationElement, final String propertyName, final Object data) throws CoreException {
		this.configurationElement = configurationElement;
	}

	@Override
	public void addPages() {
		super.addPages();

		mainPage = new WizardNewProjectCreationPage("NewPluginProjectPage"); //$NON-NLS-1$
		mainPage.setTitle("Project");
		mainPage.setDescription("Create a plug-in project.");
		addPage(mainPage);

		if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
			referencePage = new WizardNewProjectReferencePage("ReferenceProjectPage");//$NON-NLS-1$
			referencePage.setTitle("Project References");
			referencePage.setDescription("Select referenced projects.");
			addPage(referencePage);
		}
	}

	@Override
	public boolean performFinish() {
		final IProject newProject = createNewProject();

		if (newProject == null) {
			return false;
		}

		final IWorkingSet[] workingSets = mainPage.getSelectedWorkingSets();
		workbench.getWorkingSetManager().addToWorkingSets(newProject, workingSets);

		BasicNewProjectResourceWizard.updatePerspective(configurationElement);
		BasicNewResourceWizard.selectAndReveal(newProject, workbench.getActiveWorkbenchWindow());

		try {
			createVexPluginXml(newProject);
			registerVexPluginNature(newProject);
		} catch (final CoreException e) {
			VexPlugin.getDefault().log(IStatus.ERROR, Messages.getString("NewPluginProjectWizard.createError"), e); //$NON-NLS-1$
			return false;
		}

		return true;
	}

	private IProject createNewProject() {
		final IProject newProjectHandle = mainPage.getProjectHandle();

		final URI location;
		if (!mainPage.useDefaults()) {
			location = mainPage.getLocationURI();
		} else {
			location = null;
		}

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
		description.setLocationURI(location);

		// update the referenced project if provided
		if (referencePage != null) {
			final IProject[] refProjects = referencePage.getReferencedProjects();
			if (refProjects.length > 0) {
				description.setReferencedProjects(refProjects);
			}
		}

		// create the new project operation
		final IRunnableWithProgress createOperation = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InvocationTargetException {
				final CreateProjectOperation createProject = new CreateProjectOperation(description, Messages.getString("NewPluginProjectWizard.title"));
				try {
					createProject.execute(monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
				} catch (final ExecutionException e) {
					throw new InvocationTargetException(e);
				}
			}
		};

		// run the new project creation operation
		try {
			getContainer().run(true, true, createOperation);
		} catch (final InterruptedException e) {
			return null;
		} catch (final InvocationTargetException e) {
			final Throwable t = e.getTargetException();
			if (t instanceof ExecutionException && t.getCause() instanceof CoreException) {
				final CoreException cause = (CoreException) t.getCause();
				final StatusAdapter status;
				if (cause.getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
					status = new StatusAdapter(new Status(IStatus.WARNING, VexPlugin.ID, MessageFormat.format(
							"The underlying file system is case insensitive. There is an existing project or directory that conflicts with ''{0}''.", newProjectHandle.getName()), cause));
				} else {
					status = new StatusAdapter(new Status(cause.getStatus().getSeverity(), VexPlugin.ID, "Creation Problems", cause));
				}
				status.setProperty(IStatusAdapterConstants.TITLE_PROPERTY, "Creation Problems");
				StatusManager.getManager().handle(status, StatusManager.BLOCK);
			} else {
				final StatusAdapter status = new StatusAdapter(new Status(IStatus.WARNING, VexPlugin.ID, 0, MessageFormat.format("Internal error: {0}", t.getMessage()), t));
				status.setProperty(IStatusAdapterConstants.TITLE_PROPERTY, "Creation Problems");
				StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.BLOCK);
			}
			return null;
		}

		return newProjectHandle;
	}

	private void createVexPluginXml(final IProject project) throws CoreException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final PrintStream out = new PrintStream(buffer);
		out.println("<?xml version='1.0'?>"); //$NON-NLS-1$
		out.println("<plugin>"); //$NON-NLS-1$
		out.println("</plugin>"); //$NON-NLS-1$
		out.close();

		final IFile pluginXml = project.getFile(PluginProject.PLUGIN_XML);
		pluginXml.create(new ByteArrayInputStream(buffer.toByteArray()), true, null);

		/*
		 * By default open the Default Text Editor for vex-plugin.xml. This isn't perfect, because the Vex icon is still
		 * be shown, but it'll do until we create a custom editor.
		 */
		IDE.setDefaultEditor(pluginXml, "org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
	}

	private void registerVexPluginNature(final IProject project) throws CoreException {
		final IProjectDescription description = project.getDescription();
		final String[] natures = description.getNatureIds();
		final String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = PluginProjectNature.ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}

}
