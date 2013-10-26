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
package org.eclipse.vex.core.internal.css;

import org.apache.batik.css.parser.DefaultElementSelector;
import org.apache.batik.css.parser.DefaultSelectorFactory;
import org.eclipse.vex.core.internal.dom.Namespace;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.ElementSelector;

/**
 * Together with {@link org.eclipse.vex.core.internal.css.CssScanner CssScanner} this class provides a pseudo support
 * for CSS namespaces.<br>
 * There is no support for <code>@namespace</code> rules, the allowed namespaces and prefixes are hardcoded.
 */
public class VexSelectorFactory extends DefaultSelectorFactory {
	/**
	 * <b>SAC</b>: Implements {@link org.w3c.css.sac.SelectorFactory#createElementSelector(String,String)}.
	 */
	@Override
	public ElementSelector createElementSelector(String namespaceURI, String tagName) throws CSSException {
		final int seperatorIndex = tagName != null ? tagName.indexOf("|") : -1;
		if (seperatorIndex > -1) {
			final String namespacePrefix = tagName.substring(0, seperatorIndex);
			if (namespacePrefix.equals(CSS.VEX_NAMESPACE_PREFIX)) {
				namespaceURI = Namespace.VEX_NAMESPACE_URI;
				tagName = tagName.substring(seperatorIndex + 1);
			}
		}
		return new DefaultElementSelector(namespaceURI, tagName);
	}
}
