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
package org.eclipse.vex.core.internal.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.vex.core.XML;
import org.eclipse.vex.core.internal.css.IWhitespacePolicy;
import org.eclipse.vex.core.internal.css.IWhitespacePolicyFactory;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.dom.Namespace;
import org.eclipse.vex.core.provisional.dom.DocumentContentModel;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IValidator;
import org.xml.sax.SAXException;

/**
 * A helper class to transfer XML to an {@link IDocumentFragment} and vice versa.
 */
public class XMLFragment {

	private static final String XML_PRE = "<?xml version='1.0' encoding='UTF-8'?>\n" + "<vex:vex-fragment xmlns:vex=\"" + Namespace.VEX_NAMESPACE_URI + "\">";
	private static final String XML_POST = "</vex:vex-fragment>";

	private final String fragmentXML;
	private IDocumentFragment fragment = null;

	/**
	 * @param xml
	 *            The source XML of the wrapped fragment
	 */
	public XMLFragment(final String xml) {
		fragmentXML = xml;
		fragment = null;
	}

	/**
	 * @param fragment
	 *            The fragment to be wrapped
	 */
	public XMLFragment(final IDocumentFragment fragment) {
		fragmentXML = fragmentToString(fragment);
		this.fragment = fragment;
	}

	private static String fragmentToString(final IDocumentFragment fragment) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			new DocumentWriter().writeNoWrap(fragment, out);
			return out.toString("UTF-8");
		} catch (final IOException e) {
			// should not happen with a ByteArrayOutputStream
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * @return true if this XML fragment contains only text
	 */
	public boolean isTextOnly() {
		return getDocumentFragment().getText().equals(fragmentXML);
	}

	/**
	 * @return a new instance of XMLFragment where all whitespace is compressed
	 */
	public XMLFragment compressWhitespace() {
		return new XMLFragment(XML.compressWhitespace(fragmentXML, true, true, false));
	}

	/**
	 * @return The raw XML of the wrapped fragment.
	 */
	public String getXML() {
		return fragmentXML;
	}

	/**
	 * @return An IDocumentFragment created from the parsed XML string, whitespace is preserved.
	 * @throws DocumentValidationException
	 *             when the given String is no valid XML fragment
	 */
	public IDocumentFragment getDocumentFragment() throws DocumentValidationException {
		return getDocumentFragment(IWhitespacePolicy.PRESERVE_WHITESPACE);
	}

	/**
	 * @param whitespacePolicy
	 *            the IWhitespacePolicy to apply when creating the IDocumentFragment
	 * @return An IDocumentFragment created from the parsed XML String.
	 * @throws DocumentValidationException
	 *             when the given String is no valid XML fragment
	 */
	public IDocumentFragment getDocumentFragment(final IWhitespacePolicy whitespacePolicy) throws DocumentValidationException {
		if (fragment != null) {
			return fragment;
		}

		try {
			final DocumentReader reader = new DocumentReader();

			reader.setWhitespacePolicyFactory(new IWhitespacePolicyFactory() {
				public IWhitespacePolicy createPolicy(final IValidator validator, final DocumentContentModel documentContentModel, final StyleSheet styleSheet) {
					return whitespacePolicy;
				}
			});

			final String xml = wrap();
			final IDocument document = reader.read(xml);
			fragment = document.getFragment(document.getRootElement().getRange().resizeBy(1, -1));
			return fragment;
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		} catch (final SAXException e) {
			// Text is no valid XML;
			fragment = null;
			throw new DocumentValidationException(e.getLocalizedMessage());
		}
		fragment = null;
		return null;
	}

	private String wrap() {
		final StringBuilder sb = new StringBuilder();
		sb.append(XML_PRE);
		sb.append(fragmentXML);
		sb.append(XML_POST);
		return sb.toString();
	}

}