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
package org.eclipse.vex.core.dom;

import java.io.ObjectStreamException;

import org.eclipse.wst.xml.core.internal.contentmodel.CMDataType;

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
	 * Enumeration of attribute types.
	 */
	public static final class Type {

		private final String token;

		public static final Type CDATA = new Type("CDATA");
		public static final Type ID = new Type("ID");
		public static final Type IDREF = new Type("IDREF");
		public static final Type IDREFS = new Type("IDREFS");
		public static final Type NMTOKEN = new Type("NMTOKEN");
		public static final Type NMTOKENS = new Type("NMTOKENS");
		public static final Type ENTITY = new Type("ENTITY");
		public static final Type ENTITIES = new Type("ENTITIES");
		public static final Type NOTATION = new Type("NOTATION");
		public static final Type ENUMERATION = new Type("ENUMERATION");

		private Type(final String token) {
			this.token = token;
		}

		public static Type get(final String token) {
			if (token.equals(CDATA.toString())) {
				return CDATA;
			} else if (token.equals(ID.toString())) {
				return ID;
			} else if (token.equals(IDREF.toString())) {
				return IDREF;
			} else if (token.equals(IDREFS.toString())) {
				return IDREFS;
			} else if (token.equals(NMTOKEN.toString())) {
				return NMTOKEN;
			} else if (token.equals(NMTOKENS.toString())) {
				return NMTOKENS;
			} else if (token.equals(ENTITY.toString())) {
				return ENTITY;
			} else if (token.equals(ENTITIES.toString())) {
				return ENTITIES;
			} else if (token.equals(NOTATION.toString())) {
				return NOTATION;
			} else if (token.equals(ENUMERATION.toString()) || token.equals(CMDataType.ENUM)) {
				return ENUMERATION;
			} else {
				System.out.println("Found unknown attribute type '" + token + "'.");
				return CDATA;
			}
		}

		@Override
		public String toString() {
			return token;
		}

		/**
		 * Serialization method, to ensure that we do not introduce new instances.
		 */
		private Object readResolve() throws ObjectStreamException {
			return get(toString());
		}
	}

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
