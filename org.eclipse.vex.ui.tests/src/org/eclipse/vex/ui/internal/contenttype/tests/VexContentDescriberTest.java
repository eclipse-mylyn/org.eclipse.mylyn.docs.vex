/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.contenttype.tests;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.vex.ui.internal.contenttype.VexContentDescriber;
import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class VexContentDescriberTest {

	private static final String REGISTERED_VEX_DOCTYPE = "<?xml version=\"1.0\"?><!DOCTYPE section PUBLIC \"-//Vex//DTD Test//EN\" \"test.dtd\"><section/>";
	private static final String ARBITRARY_XML = "<?xml version=\"1.0\"?><someRootElement/>";
	private static final String SIMPLE_TEXT = "Hello World";

	@Test
	public void givenRegisteredVexDoctype_shouldDescribeAsValid() throws Exception {
		assertDescribesAs(IContentDescriber.VALID, REGISTERED_VEX_DOCTYPE);
	}

	@Test
	public void givenArbitraryXml_shouldDescribeAsIndeterminate() throws Exception {
		assertDescribesAs(IContentDescriber.INDETERMINATE, ARBITRARY_XML);
	}

	@Test
	public void givenSimpleText_shouldDescribeAsInvalid() throws Exception {
		assertDescribesAs(IContentDescriber.INVALID, SIMPLE_TEXT);
	}

	private static void assertDescribesAs(final int expectedDescription, final String contents) throws Exception {
		final VexContentDescriber describer = new VexContentDescriber();
		final IContentDescription description = new DummyContentDescription();
		assertEquals(expectedDescription, describer.describe(new ByteArrayInputStream(contents.getBytes()), description));
	}

	private static class DummyContentDescription implements IContentDescription {

		private final Map<QualifiedName, Object> properties = new HashMap<QualifiedName, Object>();

		@Override
		public boolean isRequested(final QualifiedName key) {
			return true;
		}

		@Override
		public String getCharset() {
			return System.getProperty("file.encoding");
		}

		@Override
		public IContentType getContentType() {
			return null;
		}

		@Override
		public Object getProperty(final QualifiedName key) {
			return properties.get(key);
		}

		@Override
		public void setProperty(final QualifiedName key, final Object value) {
			properties.put(key, value);
		}

	}
}
