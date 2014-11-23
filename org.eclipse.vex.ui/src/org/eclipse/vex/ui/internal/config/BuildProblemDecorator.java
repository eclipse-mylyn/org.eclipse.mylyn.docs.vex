/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Torsten Stolpmann - bug 250837 - fixed id used for builder.
 *******************************************************************************/
package org.eclipse.vex.ui.internal.config;

import java.net.URL;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.vex.ui.internal.VexPlugin;

/**
 * Decorates Vex resources that build problems.
 */
public class BuildProblemDecorator extends LabelProvider implements ILightweightLabelDecorator {

	public static final String ID = "org.eclipse.vex.ui.config.buildProblemDecorator"; //$NON-NLS-1$

	@Override
	public void decorate(final Object element, final IDecoration decoration) {

		if (errorIcon == null) {
			loadImageDescriptors();
		}

		if (element instanceof IResource) {
			try {
				final IResource resource = (IResource) element;
				final IMarker[] markers = resource.findMarkers(IMarker.PROBLEM, true, 0);
				if (markers.length > 0) {
					decoration.addOverlay(errorIcon, IDecoration.BOTTOM_LEFT);
				}
			} catch (final CoreException e) {
			}
		}
	}

	/**
	 * Fire a change notification that the markers on the given resource has changed.
	 *
	 * @param resources
	 *            Array of resources whose markers have changed.
	 */
	public void update(final IResource resource) {
		fireLabelProviderChanged(new LabelProviderChangedEvent(this, resource));
	}

	/**
	 * Fire a change notification that the markers on the given resources has changed.
	 *
	 * @param resources
	 *            Array of resources whose markers have changed.
	 */
	public void update(final IResource[] resources) {
		fireLabelProviderChanged(new LabelProviderChangedEvent(this, resources));
	}

	// ======================================================== PRIVATE

	private ImageDescriptor errorIcon;

	private void loadImageDescriptors() {
		final URL url = FileLocator.find(VexPlugin.getDefault().getBundle(), new Path("icons/error_co.gif"), //$NON-NLS-1$
				null);
		errorIcon = ImageDescriptor.createFromURL(url);
	}

}
