/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.config;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * Project nature that defines Vex Plugin projects.
 */
public class PluginProjectNature implements IProjectNature {

	public static final String ID = "org.eclipse.vex.ui.pluginNature"; //$NON-NLS-1$

	@Override
	public void configure() throws CoreException {
		registerBuilder();
	}

	@Override
	public void deconfigure() throws CoreException {
		// System.out.println("deconfiguring " + project.getName());
		project.deleteMarkers(IMarker.PROBLEM, true, 1);
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(final IProject project) {
		this.project = project;
	}

	// ====================================================== PRIVATE

	private IProject project;

	private void registerBuilder() throws CoreException {
		final IProjectDescription desc = project.getDescription();
		final ICommand[] commands = desc.getBuildSpec();
		boolean found = false;

		for (final ICommand command : commands) {
			if (command.getBuilderName().equals(PluginProjectBuilder.ID)) {
				found = true;
				break;
			}
		}
		if (!found) {
			// add builder to project
			final ICommand command = desc.newCommand();
			command.setBuilderName(PluginProjectBuilder.ID);
			final ICommand[] newCommands = new ICommand[commands.length + 1];

			// Add it before other builders.
			System.arraycopy(commands, 0, newCommands, 1, commands.length);
			newCommands[0] = command;
			desc.setBuildSpec(newCommands);
			project.setDescription(desc, null);
		}

	}
}
