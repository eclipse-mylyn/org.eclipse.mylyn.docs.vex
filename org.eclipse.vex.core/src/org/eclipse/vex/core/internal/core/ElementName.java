/*******************************************************************************
 * Copyright (c) 2011 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.core;

import org.eclipse.core.runtime.QualifiedName;

/**
 * @author Florian Thienel
 */
public class ElementName {

	private final QualifiedName qualifiedName;

	private final String prefix;

	public ElementName(final QualifiedName qualifiedName, final String prefix) {
		this.qualifiedName = qualifiedName;
		this.prefix = prefix;
	}

	public QualifiedName getQualifiedName() {
		return qualifiedName;
	}

	public String getQualifier() {
		return qualifiedName.getQualifier();
	}

	public String getLocalName() {
		return qualifiedName.getLocalName();
	}

	public String getPrefix() {
		return prefix;
	}

	@Override
	public String toString() {
		if (prefix == null) {
			return qualifiedName.getLocalName();
		}
		return prefix + ":" + qualifiedName.getLocalName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (prefix == null ? 0 : prefix.hashCode());
		result = prime * result + (qualifiedName == null ? 0 : qualifiedName.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ElementName other = (ElementName) obj;
		if (prefix == null) {
			if (other.prefix != null) {
				return false;
			}
		} else if (!prefix.equals(other.prefix)) {
			return false;
		}
		if (qualifiedName == null) {
			if (other.qualifiedName != null) {
				return false;
			}
		} else if (!qualifiedName.equals(other.qualifiedName)) {
			return false;
		}
		return true;
	}

}
