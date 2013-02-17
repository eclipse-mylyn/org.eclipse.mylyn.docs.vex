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
 *     Florian Thienel - promotion to public API
 *******************************************************************************/
package org.eclipse.vex.core.provisional.dom;

/**
 * An immuatable representation of an attribute definition in a grammar. Attribute definitions are comparable by the
 * name of the attribute they define.
 */
public class AttributeDefinition implements Comparable<AttributeDefinition> {

	private final String name;
	private final Type type;
	private final String defaultValue;
	private final String[] values;
	private final boolean required;
	private final boolean fixed;

	/**
	 * The attribute's type.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/TR/REC-xml/#sec-attribute-types">http://www.w3.org/TR/REC-xml/#sec-attribute-types</a>
	 */
	public static enum Type {
		CDATA, ID, IDREF, IDREFS, NMTOKEN, NMTOKENS, ENTITY, ENTITIES, NOTATION, ENUMERATION;
	}

	/**
	 * @param name
	 *            the local name of the attribute. An attribute name is qualified by its parent element's namespace
	 *            qualifier.
	 * @param type
	 *            the attribute's type
	 * @param defaultValue
	 *            the default value, or null
	 * @param values
	 *            the list of allowed values (for enumerations) or null if any value is allowed
	 * @param required
	 *            if true this attribute is required to have a value
	 * @param fixed
	 *            if true the value of this attribute is fixed and may not be changed
	 */
	public AttributeDefinition(final String name, final Type type, final String defaultValue, final String[] values, final boolean required, final boolean fixed) {
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.values = values;
		this.required = required;
		this.fixed = fixed;
	}

	/**
	 * Implements <code>Comparable.compareTo</code> to sort alphabetically by name.
	 * 
	 * @param other
	 *            The attribute to which this one is to be compared.
	 * @see Comparable
	 */
	public int compareTo(final AttributeDefinition other) {
		return name.compareTo(other.name);
	}

	/**
	 * @return the attribute's type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return the default value of the attribute, or null if the attribute has no default value
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return true if the attribute value is fixed
	 */
	public boolean isFixed() {
		return fixed;
	}

	/**
	 * @return the (local) name of the attribute
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return true if the attribute is required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @return an array of acceptable values for the attribute. If null is returned, any value is acceptable for the
	 *         attribute
	 */
	public String[] getValues() {
		return values;
	}

}
