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
package org.eclipse.vex.ui.internal.contenttype;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.QualifiedName;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Florian Thienel
 */
public class InferXmlContentTypeHandler extends DefaultHandler implements LexicalHandler {

	private String dtdPublicId;
	private String dtdSystemId;
	private QualifiedName rootElementName;

	public String getMainDocumentTypeIdentifier() {
		if (dtdPublicId != null) {
			return dtdPublicId;
		}
		if (dtdSystemId != null) {
			return dtdSystemId;
		}
		return rootElementName.getQualifier();
	}

	public QualifiedName getRootElementName() {
		return rootElementName;
	}

	public boolean parseContents(final InputSource contents) {
		try {
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			if (factory == null) {
				return false;
			}
			factory.setNamespaceAware(true);
			final SAXParser parser = createParser(factory);
			contents.setSystemId("/"); //$NON-NLS-1$
			parser.parse(contents, this);
		} catch (final AbortParsingException e) {
			// Abort the parsing normally. Fall through...
		} catch (final ParserConfigurationException e) {
			return false;
		} catch (final SAXException e) {
			return false;
		} catch (final IOException e) {
			return false;
		}
		return true;
	}

	private SAXParser createParser(final SAXParserFactory parserFactory) throws ParserConfigurationException, SAXException, SAXNotRecognizedException, SAXNotSupportedException {
		final SAXParser parser = parserFactory.newSAXParser();
		final XMLReader reader = parser.getXMLReader();
		reader.setProperty("http://xml.org/sax/properties/lexical-handler", this); //$NON-NLS-1$
		try {
			reader.setFeature("http://xml.org/sax/features/validation", false); //$NON-NLS-1$
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$
		} catch (final SAXNotRecognizedException e) {
			// not a big deal if the parser does not recognize the features
		} catch (final SAXNotSupportedException e) {
			// not a big deal if the parser does not support the features
		}
		return parser;
	}

	@Override
	public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {
		dtdPublicId = publicId;
		dtdSystemId = systemId;
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		rootElementName = new QualifiedName(uri, localName);
		throw new AbortParsingException();
	}

	@Override
	public void endDTD() throws SAXException {
		// ignore
	}

	@Override
	public void startEntity(final String name) throws SAXException {
		// ignore
	}

	@Override
	public void endEntity(final String name) throws SAXException {
		// ignore
	}

	@Override
	public void startCDATA() throws SAXException {
		// ignore
	}

	@Override
	public void endCDATA() throws SAXException {
		// ignore
	}

	@Override
	public void comment(final char[] ch, final int start, final int length) throws SAXException {
		// ignore
	}

	@Override
	public InputSource resolveEntity(final String publicId, final String systemId) throws IOException, SAXException {
		return new InputSource(new StringReader(""));
	}

	private static class AbortParsingException extends SAXException {
		private static final long serialVersionUID = 1L;
	}

}
