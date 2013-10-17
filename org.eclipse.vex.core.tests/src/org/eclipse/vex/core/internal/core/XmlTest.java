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
package org.eclipse.vex.core.internal.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.vex.core.XML;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class XmlTest {

	@Test
	public void givenPITarget_whenTargetStartsWithXml_shouldAcceptPITarget() throws Exception {
		assertTrue(XML.validateProcessingInstructionTarget("xml-stylesheet").isOK());
	}

	@Test
	public void givenPITarget_whenTargetStartsIsXml_shouldNotAcceptPITarget() throws Exception {
		assertFalse(XML.validateProcessingInstructionTarget("xml").isOK());
	}

}
