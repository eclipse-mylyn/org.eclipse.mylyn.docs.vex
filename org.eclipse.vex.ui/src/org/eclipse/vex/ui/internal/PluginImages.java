/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class PluginImages {

	private static ImageRegistry IMAGE_REGISTRY;

	// Outline toolbar actions
	public static final ImageDescriptor DESC_HIDE_INLINE_ELEMENTS = createImageDescriptor("icons/hide_inline.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_SHOW_ELEMENT_CONTENT = createImageDescriptor("icons/show_content.gif"); //$NON-NLS-1$

	// Element Icons
	/** XML element */
	public static final ImageDescriptor DESC_XML_ELEMENT = createImageDescriptor("icons/element_obj.gif"); //$NON-NLS-1$
	/** XML comment. */
	public static final ImageDescriptor DESC_XML_COMMENT = createImageDescriptor("icons/comment_obj.gif"); //$NON-NLS-1$
	/** XML attribute. */
	public static final ImageDescriptor DESC_XML_ATTRIBUTE = createImageDescriptor("icons/attribute_obj.gif"); //$NON-NLS-1$
	/** XML unknown object. */
	public static final ImageDescriptor DESC_XML_UNKNOWN = createImageDescriptor("icons/unknown_obj.gif"); //$NON-NLS-1$

	private static ImageDescriptor createImageDescriptor(final String filePath) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(VexPlugin.ID, filePath);
	}

	// ImageRegistry for frequently used images
	private static final String NAME_PREFIX = VexPlugin.ID + "."; //$NON-NLS-1$
	public static final String IMG_XML_ELEMENT = NAME_PREFIX + "img.xml_element"; //$NON-NLS-1$
	public static final String IMG_XML_COMMENT = NAME_PREFIX + "img.xml_comment"; //$NON-NLS-1$
	public static final String IMG_XML_ATTRIBUTE = NAME_PREFIX + "img.xml_attribute"; //$NON-NLS-1$
	public static final String IMG_XML_UNKNOWN = NAME_PREFIX + "img.xml_unknown"; //$NON-NLS-1$

	public static Image get(final String key) {
		if (IMAGE_REGISTRY == null) {
			initializeImageRegistry();
		}
		return IMAGE_REGISTRY.get(key);
	}

	private static final void initializeImageRegistry() {
		IMAGE_REGISTRY = new ImageRegistry();
		register(IMG_XML_ELEMENT, DESC_XML_ELEMENT);
		register(IMG_XML_COMMENT, DESC_XML_COMMENT);
		register(IMG_XML_ATTRIBUTE, DESC_XML_ATTRIBUTE);
		register(IMG_XML_UNKNOWN, DESC_XML_UNKNOWN);
	}

	public static void register(final String key, final ImageDescriptor desc) {
		final Image image = desc.createImage();
		IMAGE_REGISTRY.put(key, image);
	}
}
