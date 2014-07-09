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
package org.eclipse.vex.ui.internal.editor;

/**
 * Indicates that the document was matched to a registered doctype, but that the given doctype does not have a matching
 * style.
 */
public class NoStyleForDoctypeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
}