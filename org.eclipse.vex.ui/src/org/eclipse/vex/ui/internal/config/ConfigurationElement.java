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
package org.eclipse.vex.ui.internal.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lightweight implementation of the IConfigurationElement interface. This class is used by config item factories when
 * re-creating the configuration elements corresponding to a given config item.
 */
public class ConfigurationElement implements IConfigElement {

	/**
	 * Class constructor.
	 */
	public ConfigurationElement() {
	}

	/**
	 * Class constructor.
	 * 
	 * @param name
	 *            Name of the element.
	 */
	public ConfigurationElement(final String name) {
		this.name = name;
	}

	/**
	 * Adds a new child to this element.
	 * 
	 * @param child
	 *            child to be added.
	 */
	public void addChild(final IConfigElement child) {
		children.add(child);
	}

	public String getAttribute(final String name) {
		return attributes.get(name);
	}

	public String[] getAttributeNames() {
		final Set<String> keys = attributes.keySet();
		return keys.toArray(new String[keys.size()]);
	}

	public IConfigElement[] getChildren() {
		return children.toArray(new IConfigElement[children.size()]);
	}

	public IConfigElement[] getChildren(final String name) {
		final List<IConfigElement> kids = new ArrayList<IConfigElement>();
		for (final IConfigElement child : children) {
			if (child.getName().equals(name)) {
				kids.add(child);
			}
		}
		return kids.toArray(new IConfigElement[kids.size()]);
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	/**
	 * Sets the given attribute. If value is null, the attribute is removed from the element.
	 * 
	 * @param name
	 *            Name of the attribute.
	 * @param value
	 *            Value of the attribute.
	 */
	public void setAttribute(final String name, final String value) {
		if (value == null) {
			attributes.remove(name);
		} else {
			attributes.put(name, value);
		}
	}

	/**
	 * Sets the children of this element given an array of IConfigElement objects.
	 * 
	 * @param children
	 *            Children of this element.
	 */
	public void setChildren(final IConfigElement[] children) {
		this.children.clear();
		this.children.addAll(Arrays.asList(children));
	}

	/**
	 * Sets the name of the element.
	 * 
	 * @param name
	 *            Name of the element.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Sets the value of the element.
	 * 
	 * @param value
	 *            Value of the element.
	 */
	public void setValue(final String value) {
		this.value = value;
	}

	// ==================================================== PRIVATE

	private String name;
	private String value;
	private final Map<String, String> attributes = new HashMap<String, String>();
	private final List<IConfigElement> children = new ArrayList<IConfigElement>();
}
