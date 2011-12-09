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
package org.eclipse.vex.core.internal.css;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.vex.core.internal.dom.Element;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class BatikBehaviorTest {
	
	@Test
	public void pseudoElements() throws Exception {
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet styleSheet = reader.read("plan:before { display: block; font-size: 123; }");
		final List<Rule> rules = styleSheet.getRules();
		assertEquals(1, rules.size());
		final Rule rule = rules.get(0);
		Element element = new Element("plan");
		PseudoElement beforeElement = new PseudoElement(element, "before");
		assertTrue(rule.matches(beforeElement));
	}

}
