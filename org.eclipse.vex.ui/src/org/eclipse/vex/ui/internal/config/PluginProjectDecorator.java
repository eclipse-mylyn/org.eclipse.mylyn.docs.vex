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

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.vex.ui.internal.VexPlugin;

/**
 * Decorates Vex projects with the Vex logo.
 */
public class PluginProjectDecorator implements ILightweightLabelDecorator {

	@Override
	public void decorate(final Object element, final IDecoration decoration) {

		if (vexIcon == null) {
			loadImageDescriptors();
		}

		if (element instanceof IProject) {
			try {
				final IProject project = (IProject) element;
				if (project.hasNature(PluginProjectNature.ID)) {
					decoration.addOverlay(vexIcon, IDecoration.TOP_RIGHT);
				}
			} catch (final CoreException e) {
			}
		}
	}

	@Override
	public void addListener(final ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(final Object element, final String property) {
		return false;
	}

	@Override
	public void removeListener(final ILabelProviderListener listener) {
	}

	// ======================================================== PRIVATE

	private ImageDescriptor vexIcon;

	private void loadImageDescriptors() {
		final URL url = FileLocator.find(VexPlugin.getDefault().getBundle(), new Path("icons/vex8.gif"), //$NON-NLS-1$
				null);
		vexIcon = ImageDescriptor.createFromURL(url);
	}

}
