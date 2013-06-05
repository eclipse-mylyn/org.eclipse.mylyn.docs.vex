/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.io;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IValidator;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * Class for creating documents given a URL.
 */
public class DocumentReader {

	private boolean debugging;

	private IValidator validator = IValidator.NULL;

	private IStyleSheetProvider styleSheetProvider = IStyleSheetProvider.NULL;

	private IWhitespacePolicyFactory whitespacePolicyFactory = IWhitespacePolicyFactory.NULL;

	private IWhitespacePolicy whitespacePolicy = IWhitespacePolicy.NULL;

	private EntityResolver entityResolver;

	private final EntityResolver combinedEntityResolver = new EntityResolver() {
		public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
			final InputSource result;
			if (entityResolver != null) {
				result = entityResolver.resolveEntity(publicId, systemId);
			} else {
				result = null;
			}
			if (result == null) {
				return validator.getDocumentContentModel().resolveEntity(publicId, systemId);
			}
			return result;
		}
	};

	/**
	 * Returns the debugging flag.
	 */
	public boolean isDebugging() {
		return debugging;
	}

	/**
	 * Reads a document given a URL.
	 * 
	 * @param url
	 *            URL from which to load the document.
	 */
	public IDocument read(final URL url) throws IOException, ParserConfigurationException, SAXException {
		return read(new InputSource(url.toString()));
	}

	/**
	 * Reads a document from a string. This is mainly used for short documents in unit tests.
	 * 
	 * @param s
	 *            String containing the document to be read.
	 */
	public IDocument read(final String s) throws IOException, ParserConfigurationException, SAXException {
		final Reader reader = new CharArrayReader(s.toCharArray());
		return this.read(new InputSource(reader));
	}

	/**
	 * Reads a document given a SAX InputSource.
	 * 
	 * @param is
	 *            SAX InputSource from which to load the document.
	 */
	public IDocument read(final InputSource is) throws IOException, ParserConfigurationException, SAXException {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);

		final XMLReader xmlReader = factory.newSAXParser().getXMLReader();
		final DocumentBuilder builder = new DocumentBuilder(is.getSystemId(), validator, styleSheetProvider, whitespacePolicyFactory);

		ContentHandler contentHandler = builder;
		LexicalHandler lexicalHandler = builder;

		if (isDebugging()) {
			final Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { ContentHandler.class, LexicalHandler.class }, new InvocationHandler() {
				public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
					try {
						return method.invoke(builder, args);
					} catch (final InvocationTargetException ex) {
						ex.getCause().printStackTrace();
						throw ex.getCause();
					}
				}
			});

			contentHandler = (ContentHandler) proxy;
			lexicalHandler = (LexicalHandler) proxy;
		}

		xmlReader.setContentHandler(contentHandler);
		xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", lexicalHandler);
		xmlReader.setEntityResolver(combinedEntityResolver);
		xmlReader.parse(is);
		final IDocument result = builder.getDocument();
		if (result != null) {
			result.setDocumentURI(is.getSystemId());
		}

		whitespacePolicy = builder.getWhitespacePolicy();

		return result;
	}

	/**
	 * Sets the debugging flag.
	 * 
	 * @param debugging
	 *            true if the component should log debugging info to stdout.
	 */
	public void setDebugging(final boolean debugging) {
		this.debugging = debugging;
	}

	/**
	 * Sets the entity resolver for this reader.
	 * 
	 * @param entityResolver
	 *            The entityResolver to set.
	 */
	public void setEntityResolver(final EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	/**
	 * Returns the entity resolver for this reader.
	 */
	public EntityResolver getEntityResolver() {
		return entityResolver;
	}

	public void setValidator(final IValidator validator) {
		this.validator = validator;
	}

	public IValidator getValidator() {
		return validator;
	}

	public void setStyleSheetProvider(final IStyleSheetProvider styleSheetProvider) {
		this.styleSheetProvider = styleSheetProvider;
	}

	public void setWhitespacePolicyFactory(final IWhitespacePolicyFactory whitespacePolicyFactory) {
		this.whitespacePolicyFactory = whitespacePolicyFactory;
	}

	public IWhitespacePolicy getWhitespacePolicy() {
		return whitespacePolicy;
	}
}
