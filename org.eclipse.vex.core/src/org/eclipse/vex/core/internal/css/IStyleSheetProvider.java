/*******************************************************************************
 * Copyright (c) 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import org.eclipse.vex.core.provisional.dom.DocumentContentModel;

/**
 * @author Florian Thienel
 */
public interface IStyleSheetProvider {

	static final IStyleSheetProvider NULL = new IStyleSheetProvider() {
		public StyleSheet getStyleSheet(final DocumentContentModel documentContentModel) {
			return StyleSheet.NULL;
		}
	};

	StyleSheet getStyleSheet(DocumentContentModel documentContentModel);

}
