/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - bug 306639 - remove serializability from StyleSheet
 *                       and dependend classes
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Carsten Hiesserich - added isPseudoElement override
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.css.parser.Scanner;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.ParsedURL;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolver;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverPlugin;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;

/**
 * Driver for the creation of StyleSheet objects.
 */
public class StyleSheetReader {

	private static final URIResolver URI_RESOLVER = URIResolverPlugin.createResolver();

	public static Parser createParser() {
		return new org.apache.batik.css.parser.Parser() {

			/**
			 * The batik implementation uses hardcoded values. This Override allows custom PseudoElements.
			 */
			@Override
			protected boolean isPseudoElement(final String s) {
				if (super.isPseudoElement(s)) {
					return true;
				} else {
					return s.equalsIgnoreCase(CSS.PSEUDO_TARGET);
				}
			}

			/**
			 * This override allows the use of a custom Scanner overrride.
			 */
			@Override
			protected Scanner createScanner(final InputSource source) {
				documentURI = source.getURI();
				if (documentURI == null) {
					documentURI = "";
				}

				final Reader r = source.getCharacterStream();
				if (r != null) {
					return new CssScanner(r);
				}

				InputStream is = source.getByteStream();
				if (is != null) {
					return new CssScanner(is, source.getEncoding());
				}

				final String uri = source.getURI();
				if (uri == null) {
					throw new CSSException(formatMessage("empty.source", null));
				}

				try {
					final ParsedURL purl = new ParsedURL(uri);
					is = purl.openStreamRaw(CSSConstants.CSS_MIME_TYPE);
					return new CssScanner(is, source.getEncoding());
				} catch (final IOException e) {
					throw new CSSException(e);
				}
			}

			/**
			 * This override joins two identifiers which are seperated by a dash '|'. The created rule uses the joined
			 * identifier (e.g. <code>vex|identifier</code>).<br />
			 * This is a hack to support 'pseudo namespaces'. There is no <b>real</b> namespace support!
			 */
			@Override
			protected void parseRuleSet() {
				if (((CssScanner) scanner).getCurrent() == '|') {
					((CssScanner) scanner).joinIdentifier();
				}
				super.parseRuleSet();
			}

		};
	}

	/**
	 * Creates a new StyleSheet object from a URL.
	 * 
	 * @param url
	 *            URL from which to read the style sheet.
	 */
	public StyleSheet read(final URL url) throws IOException {
		return this.read(new InputSource(url.toString()), url);
	}

	/**
	 * Creates a style sheet from a string. This is mainly used for small style sheets within unit tests.
	 * 
	 * @param s
	 *            String containing the style sheet.
	 */
	public StyleSheet read(final String s) throws CSSException, IOException {
		final Reader reader = new CharArrayReader(s.toCharArray());
		return this.read(new InputSource(reader), null);
	}

	/**
	 * Creates a new Stylesheet from an input source.
	 * 
	 * @param inputSource
	 *            InputSource from which to read the stylesheet.
	 * @param url
	 *            URL representing the input source, used to resolve @import rules with relative URIs. May be null, in
	 *            which case @import rules are ignored.
	 */
	public StyleSheet read(final InputSource inputSource, final URL url) throws CSSException, IOException {
		final List<Rule> rules = readRules(inputSource, url);
		return new StyleSheet(rules);
	}

	/**
	 * Parse a stylesheet file from a URL and return the list of rules.
	 * 
	 * @param url
	 *            URL from which to read the style sheet.
	 * @return The List of rules.
	 */
	public List<Rule> readRules(final URL url) throws IOException {
		return this.readRules(new InputSource(url.toString()), url);
	}

	/**
	 * Parse a stylesheet file from an input source and return the list of rules.
	 * 
	 * @param inputSource
	 *            InputSource from which to read the stylesheet.
	 * @param url
	 *            URL representing the input source, used to resolve @import rules with relative URIs. May be null, in
	 *            which case @import rules are ignored.
	 * @return The List of rules.
	 */
	public List<Rule> readRules(final InputSource inputSource, final URL url) throws CSSException, IOException {
		final Parser parser = createParser();
		final List<Rule> rules = new ArrayList<Rule>();
		final StyleSheetBuilder styleSheetBuilder = new StyleSheetBuilder(rules, url);
		parser.setDocumentHandler(styleSheetBuilder);
		parser.parseStyleSheet(inputSource);
		return rules;
	}

	// ======================================================== PRIVATE

	private static class StyleSheetBuilder implements DocumentHandler {

		// The rules that will be added to the stylesheet
		private final List<Rule> rules;

		// The rules to which decls are currently being added
		private List<Rule> currentRules;

		// URL from which @import rules relative URIs are resolved.
		// May be null!
		private final URL url;

		public StyleSheetBuilder(final List<Rule> rules, final URL url) {
			this.rules = rules;
			this.url = url;
		}

		// -------------------------------------------- DocumentHandler methods

		public void comment(final java.lang.String text) {
		}

		public void endDocument(final InputSource source) {
		}

		public void endFontFace() {
		}

		public void endMedia(final SACMediaList media) {
		}

		public void endPage(final String name, final String pseudo_page) {
		}

		public void endSelector(final SelectorList selectors) {
			rules.addAll(currentRules);
			currentRules = null;
		}

		public void ignorableAtRule(final String atRule) {
		}

		public void importStyle(final String uri, final SACMediaList media, final String defaultNamespaceURI) {
			if (url == null) {
				return;
			}

			try {
				final Parser parser = createParser();
				final URL importUrl = new URL(URI_RESOLVER.resolve(url.toString(), null, uri));
				final StyleSheetBuilder styleSheetBuilder = new StyleSheetBuilder(rules, importUrl);
				parser.setDocumentHandler(styleSheetBuilder);
				parser.parseStyleSheet(new InputSource(importUrl.toString()));
			} catch (final CSSException e) {
				System.out.println("Cannot parse stylesheet " + uri + ": " + e.getMessage());
			} catch (final IOException e) {
				System.out.println("Cannot read stylesheet " + uri + ": " + e.getMessage());
			}

		}

		public void namespaceDeclaration(final String prefix, final String uri) {
		}

		public void property(final String name, final LexicalUnit value, final boolean important) {
			if (name.equals(CSS.BORDER)) {
				this.expandBorder(value, important);
			} else if (name.equals(CSS.BORDER_BOTTOM)) {
				this.expandBorder(value, CSS.BORDER_BOTTOM, important);
			} else if (name.equals(CSS.BORDER_LEFT)) {
				this.expandBorder(value, CSS.BORDER_LEFT, important);
			} else if (name.equals(CSS.BORDER_RIGHT)) {
				this.expandBorder(value, CSS.BORDER_RIGHT, important);
			} else if (name.equals(CSS.BORDER_TOP)) {
				this.expandBorder(value, CSS.BORDER_TOP, important);
			} else if (name.equals(CSS.BORDER_COLOR)) {
				expandBorderColor(value, important);
			} else if (name.equals(CSS.BORDER_STYLE)) {
				expandBorderStyle(value, important);
			} else if (name.equals(CSS.BORDER_WIDTH)) {
				expandBorderWidth(value, important);
			} else if (name.equals(CSS.FONT)) {
				expandFont(value, important);
			} else if (name.equals(CSS.MARGIN)) {
				expandMargin(value, important);
			} else if (name.equals(CSS.PADDING)) {
				expandPadding(value, important);
			} else {
				addDecl(name, value, important);
			}
		}

		public void startDocument(final InputSource source) {
		}

		public void startFontFace() {
		}

		public void startMedia(final SACMediaList media) {
		}

		public void startPage(final String name, final String pseudo_page) {
		}

		public void startSelector(final SelectorList selectors) {
			currentRules = new ArrayList<Rule>();
			for (int i = 0; i < selectors.getLength(); i++) {
				final Selector selector = selectors.item(i);
				currentRules.add(new Rule(selector));
			}
		}

		// ----------------------------------------- DocumentHandler methods end

		// ======================================================= PRIVATE

		/**
		 * Adds a PropertyDecl to the current set of rules.
		 */
		private void addDecl(final String name, final LexicalUnit value, final boolean important) {
			for (final Rule rule : currentRules) {
				rule.add(new PropertyDecl(rule, name, value, important));
			}
		}

		/**
		 * Expand the "border" shorthand property.
		 */
		private void expandBorder(final LexicalUnit value, final boolean important) {
			this.expandBorder(value, CSS.BORDER_BOTTOM, important);
			this.expandBorder(value, CSS.BORDER_LEFT, important);
			this.expandBorder(value, CSS.BORDER_RIGHT, important);
			this.expandBorder(value, CSS.BORDER_TOP, important);
		}

		/**
		 * Expand one of the the "border-xxx" shorthand properties. whichBorder must be one of CSS.BORDER_BOTTOM,
		 * CSS.BORDER_LEFT, CSS.BORDER_RIGHT, CSS.BORDER_TOP.
		 */
		private void expandBorder(final LexicalUnit value, final String whichBorder, final boolean important) {

			if (AbstractProperty.isInherit(value)) {
				addDecl(whichBorder + CSS.COLOR_SUFFIX, value, important);
				addDecl(whichBorder + CSS.STYLE_SUFFIX, value, important);
				addDecl(whichBorder + CSS.WIDTH_SUFFIX, value, important);
				return;
			}

			final LexicalUnit[] lus = getLexicalUnitList(value);
			int i = 0;
			if (BorderWidthProperty.isBorderWidth(lus[i])) {
				addDecl(whichBorder + CSS.WIDTH_SUFFIX, lus[i], important);
				i++;
			}

			if (i < lus.length && BorderStyleProperty.isBorderStyle(lus[i])) {
				addDecl(whichBorder + CSS.STYLE_SUFFIX, lus[i], important);
				i++;
			}

			if (i < lus.length && ColorProperty.isColor(lus[i])) {
				addDecl(whichBorder + CSS.COLOR_SUFFIX, lus[i], important);
				i++;
			}

		}

		/**
		 * Expand the "border-color" shorthand property.
		 */
		private void expandBorderColor(final LexicalUnit value, final boolean important) {

			if (AbstractProperty.isInherit(value)) {
				addDecl(CSS.BORDER_TOP_COLOR, value, important);
				addDecl(CSS.BORDER_LEFT_COLOR, value, important);
				addDecl(CSS.BORDER_RIGHT_COLOR, value, important);
				addDecl(CSS.BORDER_BOTTOM_COLOR, value, important);
				return;
			}

			final LexicalUnit[] lus = getLexicalUnitList(value);
			if (lus.length >= 4) {
				addDecl(CSS.BORDER_TOP_COLOR, lus[0], important);
				addDecl(CSS.BORDER_RIGHT_COLOR, lus[1], important);
				addDecl(CSS.BORDER_BOTTOM_COLOR, lus[2], important);
				addDecl(CSS.BORDER_LEFT_COLOR, lus[3], important);
			} else if (lus.length == 3) {
				addDecl(CSS.BORDER_TOP_COLOR, lus[0], important);
				addDecl(CSS.BORDER_LEFT_COLOR, lus[1], important);
				addDecl(CSS.BORDER_RIGHT_COLOR, lus[1], important);
				addDecl(CSS.BORDER_BOTTOM_COLOR, lus[2], important);
			} else if (lus.length == 2) {
				addDecl(CSS.BORDER_TOP_COLOR, lus[0], important);
				addDecl(CSS.BORDER_LEFT_COLOR, lus[1], important);
				addDecl(CSS.BORDER_RIGHT_COLOR, lus[1], important);
				addDecl(CSS.BORDER_BOTTOM_COLOR, lus[0], important);
			} else if (lus.length == 1) {
				addDecl(CSS.BORDER_TOP_COLOR, lus[0], important);
				addDecl(CSS.BORDER_LEFT_COLOR, lus[0], important);
				addDecl(CSS.BORDER_RIGHT_COLOR, lus[0], important);
				addDecl(CSS.BORDER_BOTTOM_COLOR, lus[0], important);
			}
		}

		/**
		 * Expand the "border-style" shorthand property.
		 */
		private void expandBorderStyle(final LexicalUnit value, final boolean important) {

			if (AbstractProperty.isInherit(value)) {
				addDecl(CSS.BORDER_TOP_STYLE, value, important);
				addDecl(CSS.BORDER_LEFT_STYLE, value, important);
				addDecl(CSS.BORDER_RIGHT_STYLE, value, important);
				addDecl(CSS.BORDER_BOTTOM_STYLE, value, important);
				return;
			}

			final LexicalUnit[] lus = getLexicalUnitList(value);
			if (lus.length >= 4) {
				addDecl(CSS.BORDER_TOP_STYLE, lus[0], important);
				addDecl(CSS.BORDER_RIGHT_STYLE, lus[1], important);
				addDecl(CSS.BORDER_BOTTOM_STYLE, lus[2], important);
				addDecl(CSS.BORDER_LEFT_STYLE, lus[3], important);
			} else if (lus.length == 3) {
				addDecl(CSS.BORDER_TOP_STYLE, lus[0], important);
				addDecl(CSS.BORDER_LEFT_STYLE, lus[1], important);
				addDecl(CSS.BORDER_RIGHT_STYLE, lus[1], important);
				addDecl(CSS.BORDER_BOTTOM_STYLE, lus[2], important);
			} else if (lus.length == 2) {
				addDecl(CSS.BORDER_TOP_STYLE, lus[0], important);
				addDecl(CSS.BORDER_LEFT_STYLE, lus[1], important);
				addDecl(CSS.BORDER_RIGHT_STYLE, lus[1], important);
				addDecl(CSS.BORDER_BOTTOM_STYLE, lus[0], important);
			} else if (lus.length == 1) {
				addDecl(CSS.BORDER_TOP_STYLE, lus[0], important);
				addDecl(CSS.BORDER_LEFT_STYLE, lus[0], important);
				addDecl(CSS.BORDER_RIGHT_STYLE, lus[0], important);
				addDecl(CSS.BORDER_BOTTOM_STYLE, lus[0], important);
			}
		}

		/**
		 * Expand the "border-width" shorthand property.
		 */
		private void expandBorderWidth(final LexicalUnit value, final boolean important) {

			if (AbstractProperty.isInherit(value)) {
				addDecl(CSS.BORDER_TOP_WIDTH, value, important);
				addDecl(CSS.BORDER_LEFT_WIDTH, value, important);
				addDecl(CSS.BORDER_RIGHT_WIDTH, value, important);
				addDecl(CSS.BORDER_BOTTOM_WIDTH, value, important);
				return;
			}

			final LexicalUnit[] lus = getLexicalUnitList(value);
			if (lus.length >= 4) {
				addDecl(CSS.BORDER_TOP_WIDTH, lus[0], important);
				addDecl(CSS.BORDER_RIGHT_WIDTH, lus[1], important);
				addDecl(CSS.BORDER_BOTTOM_WIDTH, lus[2], important);
				addDecl(CSS.BORDER_LEFT_WIDTH, lus[3], important);
			} else if (lus.length == 3) {
				addDecl(CSS.BORDER_TOP_WIDTH, lus[0], important);
				addDecl(CSS.BORDER_LEFT_WIDTH, lus[1], important);
				addDecl(CSS.BORDER_RIGHT_WIDTH, lus[1], important);
				addDecl(CSS.BORDER_BOTTOM_WIDTH, lus[2], important);
			} else if (lus.length == 2) {
				addDecl(CSS.BORDER_TOP_WIDTH, lus[0], important);
				addDecl(CSS.BORDER_LEFT_WIDTH, lus[1], important);
				addDecl(CSS.BORDER_RIGHT_WIDTH, lus[1], important);
				addDecl(CSS.BORDER_BOTTOM_WIDTH, lus[0], important);
			} else if (lus.length == 1) {
				addDecl(CSS.BORDER_TOP_WIDTH, lus[0], important);
				addDecl(CSS.BORDER_LEFT_WIDTH, lus[0], important);
				addDecl(CSS.BORDER_RIGHT_WIDTH, lus[0], important);
				addDecl(CSS.BORDER_BOTTOM_WIDTH, lus[0], important);
			}
		}

		/**
		 * Expand the "font" shorthand property.
		 */
		private void expandFont(final LexicalUnit value, final boolean important) {

			if (AbstractProperty.isInherit(value)) {
				addDecl(CSS.FONT_STYLE, value, important);
				addDecl(CSS.FONT_VARIANT, value, important);
				addDecl(CSS.FONT_WEIGHT, value, important);
				addDecl(CSS.FONT_SIZE, value, important);
				addDecl(CSS.FONT_FAMILY, value, important);
				return;
			}

			final LexicalUnit[] lus = getLexicalUnitList(value);
			final int n = lus.length;
			int i = 0;
			if (i < n && FontStyleProperty.isFontStyle(lus[i])) {
				addDecl(CSS.FONT_STYLE, lus[i], important);
				i++;
			}

			if (i < n && FontVariantProperty.isFontVariant(lus[i])) {
				addDecl(CSS.FONT_VARIANT, lus[i], important);
				i++;
			}

			if (i < n && FontWeightProperty.isFontWeight(lus[i])) {
				addDecl(CSS.FONT_WEIGHT, lus[i], important);
				i++;
			}

			if (i < n && FontSizeProperty.isFontSize(lus[i])) {
				addDecl(CSS.FONT_SIZE, lus[i], important);
				i++;
			}

			if (i < n && lus[i].getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_SLASH) {
				i++; // gobble slash
				if (i < n) {
					addDecl(CSS.LINE_HEIGHT, lus[i], important);
				}
				i++;
			}

			if (i < n) {
				addDecl(CSS.FONT_FAMILY, lus[i], important);
			}
		}

		/**
		 * Expand the "margin" shorthand property.
		 */
		private void expandMargin(final LexicalUnit value, final boolean important) {

			if (AbstractProperty.isInherit(value)) {
				addDecl(CSS.MARGIN_TOP, value, important);
				addDecl(CSS.MARGIN_RIGHT, value, important);
				addDecl(CSS.MARGIN_BOTTOM, value, important);
				addDecl(CSS.MARGIN_LEFT, value, important);
				return;
			}

			final LexicalUnit[] lus = getLexicalUnitList(value);
			if (lus.length >= 4) {
				addDecl(CSS.MARGIN_TOP, lus[0], important);
				addDecl(CSS.MARGIN_RIGHT, lus[1], important);
				addDecl(CSS.MARGIN_BOTTOM, lus[2], important);
				addDecl(CSS.MARGIN_LEFT, lus[3], important);
			} else if (lus.length == 3) {
				addDecl(CSS.MARGIN_TOP, lus[0], important);
				addDecl(CSS.MARGIN_LEFT, lus[1], important);
				addDecl(CSS.MARGIN_RIGHT, lus[1], important);
				addDecl(CSS.MARGIN_BOTTOM, lus[2], important);
			} else if (lus.length == 2) {
				addDecl(CSS.MARGIN_TOP, lus[0], important);
				addDecl(CSS.MARGIN_LEFT, lus[1], important);
				addDecl(CSS.MARGIN_RIGHT, lus[1], important);
				addDecl(CSS.MARGIN_BOTTOM, lus[0], important);
			} else if (lus.length == 1) {
				addDecl(CSS.MARGIN_TOP, lus[0], important);
				addDecl(CSS.MARGIN_LEFT, lus[0], important);
				addDecl(CSS.MARGIN_RIGHT, lus[0], important);
				addDecl(CSS.MARGIN_BOTTOM, lus[0], important);
			}
		}

		/**
		 * Expand the "padding" shorthand property.
		 */
		private void expandPadding(final LexicalUnit value, final boolean important) {

			if (AbstractProperty.isInherit(value)) {
				addDecl(CSS.PADDING_TOP, value, important);
				addDecl(CSS.PADDING_LEFT, value, important);
				addDecl(CSS.PADDING_RIGHT, value, important);
				addDecl(CSS.PADDING_BOTTOM, value, important);
				return;
			}

			final LexicalUnit[] lus = getLexicalUnitList(value);
			if (lus.length >= 4) {
				addDecl(CSS.PADDING_TOP, lus[0], important);
				addDecl(CSS.PADDING_RIGHT, lus[1], important);
				addDecl(CSS.PADDING_BOTTOM, lus[2], important);
				addDecl(CSS.PADDING_LEFT, lus[3], important);
			} else if (lus.length == 3) {
				addDecl(CSS.PADDING_TOP, lus[0], important);
				addDecl(CSS.PADDING_LEFT, lus[1], important);
				addDecl(CSS.PADDING_RIGHT, lus[1], important);
				addDecl(CSS.PADDING_BOTTOM, lus[2], important);
			} else if (lus.length == 2) {
				addDecl(CSS.PADDING_TOP, lus[0], important);
				addDecl(CSS.PADDING_LEFT, lus[1], important);
				addDecl(CSS.PADDING_RIGHT, lus[1], important);
				addDecl(CSS.PADDING_BOTTOM, lus[0], important);
			} else if (lus.length == 1) {
				addDecl(CSS.PADDING_TOP, lus[0], important);
				addDecl(CSS.PADDING_LEFT, lus[0], important);
				addDecl(CSS.PADDING_RIGHT, lus[0], important);
				addDecl(CSS.PADDING_BOTTOM, lus[0], important);
			}
		}

		/**
		 * Returns an array of <code>LexicalUnit</code> objects, the first of which is given.
		 */
		private static LexicalUnit[] getLexicalUnitList(LexicalUnit lu) {
			final List<LexicalUnit> lus = new ArrayList<LexicalUnit>();
			while (lu != null) {
				lus.add(lu);
				lu = lu.getNextLexicalUnit();
			}
			return lus.toArray(new LexicalUnit[lus.size()]);
		}

	}
}
