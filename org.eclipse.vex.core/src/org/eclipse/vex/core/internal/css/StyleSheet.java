/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Dave Holroyd - Implement font-weight:bolder
 *     Dave Holroyd - Implement text decoration
 *     John Austin - More complete CSS constants. Add the colour "orange".
 *     Travis Haagen - bug 260806 - enhanced support for 'content' CSS property
 *     Florian Thienel - bug 306639 - remove serializability from StyleSheet
 *                       and dependend classes
 *     Mohamadou Nassourou - Bug 298912 - rudimentary support for images
 *     Carsten Hiesserich - Styles cache now uses hard references instead of
 *                          WeekReference. PseudoElements are cached.
 *     Carsten Hiesserich - Added OutlineContent property
 *     Carsten Hiesserich - New handling for pseudo elements
 *     Carsten Hiesserich - Added core styles
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Selector;

/**
 * Represents a CSS style sheet.
 */
public class StyleSheet {

	public static final StyleSheet NULL = new StyleSheet(Collections.<Rule> emptyList(), null);

	private static final Comparator<PropertyDecl> PROPERTY_CASCADE_ORDERING = new Comparator<PropertyDecl>() {
		@Override
		public int compare(final PropertyDecl propertyDecl1, final PropertyDecl propertyDecl2) {
			if (propertyDecl1.isImportant() != propertyDecl2.isImportant()) {
				return (propertyDecl1.isImportant() ? 1 : 0) - (propertyDecl2.isImportant() ? 1 : 0);
			}

			return propertyDecl1.getRule().getSpecificity() - propertyDecl2.getRule().getSpecificity();
		}
	};

	/**
	 * Standard CSS properties.
	 */
	private static final IProperty[] CSS_PROPERTIES = new IProperty[] { new DisplayProperty(), new LineHeightProperty(), new ListStyleTypeProperty(), new TextAlignProperty(),
			new WhiteSpaceProperty(),

			new FontFamilyProperty(), new FontSizeProperty(), new FontStyleProperty(), new FontWeightProperty(), new TextDecorationProperty(),

			new ColorProperty(CSS.COLOR), new ColorProperty(CSS.BACKGROUND_COLOR),

			new LengthProperty(CSS.MARGIN_BOTTOM, IProperty.Axis.VERTICAL), new LengthProperty(CSS.MARGIN_LEFT, IProperty.Axis.HORIZONTAL),
			new LengthProperty(CSS.MARGIN_RIGHT, IProperty.Axis.HORIZONTAL), new LengthProperty(CSS.MARGIN_TOP, IProperty.Axis.VERTICAL),

			new LengthProperty(CSS.PADDING_BOTTOM, IProperty.Axis.VERTICAL), new LengthProperty(CSS.PADDING_LEFT, IProperty.Axis.HORIZONTAL),
			new LengthProperty(CSS.PADDING_RIGHT, IProperty.Axis.HORIZONTAL), new LengthProperty(CSS.PADDING_TOP, IProperty.Axis.VERTICAL),

			new ColorProperty(CSS.BORDER_BOTTOM_COLOR), new ColorProperty(CSS.BORDER_LEFT_COLOR), new ColorProperty(CSS.BORDER_RIGHT_COLOR), new ColorProperty(CSS.BORDER_TOP_COLOR),
			new BorderStyleProperty(CSS.BORDER_BOTTOM_STYLE), new BorderStyleProperty(CSS.BORDER_LEFT_STYLE), new BorderStyleProperty(CSS.BORDER_RIGHT_STYLE),
			new BorderStyleProperty(CSS.BORDER_TOP_STYLE), new BorderWidthProperty(CSS.BORDER_BOTTOM_WIDTH, CSS.BORDER_BOTTOM_STYLE, IProperty.Axis.VERTICAL),
			new BorderWidthProperty(CSS.BORDER_LEFT_WIDTH, CSS.BORDER_LEFT_STYLE, IProperty.Axis.HORIZONTAL),
			new BorderWidthProperty(CSS.BORDER_RIGHT_WIDTH, CSS.BORDER_RIGHT_STYLE, IProperty.Axis.HORIZONTAL),
			new BorderWidthProperty(CSS.BORDER_TOP_WIDTH, CSS.BORDER_TOP_STYLE, IProperty.Axis.VERTICAL), new BorderSpacingProperty(), new LengthProperty(CSS.HEIGHT, IProperty.Axis.VERTICAL),
			new LengthProperty(CSS.WIDTH, IProperty.Axis.HORIZONTAL), new BackgroundImageProperty(), new OutlineContentProperty(), new InlineMarkerProperty(),
			new ContentProperty()
	};

	private final List<Rule> rules;
	private final URL baseUrl;

	/**
	 * The VEX core styles
	 */
	private final static List<Rule> coreRules;

	static {
		List<Rule> rules;
		try {
			rules = new StyleSheetReader().readRules(StyleSheet.class.getResource("vex-core-styles.css"));
		} catch (final IOException e) {
			rules = Collections.<Rule> emptyList();
			e.printStackTrace();
		}
		coreRules = rules;
	}

	/**
	 * Computing styles can be expensive, e.g. we have to calculate the styles of all parents of an element. We
	 * therefore cache styles in a map of element => styles. We use a WeakHashMap here that does not prevent the INode's
	 * from being GC'ed. Note that the entries of the Map are only collected, when one of the Maps method is called the
	 * next time, so entries will stay on the heap until another VexEditor is launched. To prevent this, the Styles are
	 * flushed in BaseVexWidget#dispose.
	 */
	private final Map<INode, Styles> styleMap = new WeakHashMap<INode, Styles>(50);

	/**
	 * Class constructor.
	 *
	 * @param rules
	 *            Rules that constitute the style sheet.
	 */
	public StyleSheet(final Collection<Rule> rules, final URL baseUrl) {
		this.rules = new ArrayList<Rule>(rules);
		this.baseUrl = baseUrl;
	}

	public URL getBaseUrl() {
		return baseUrl;
	}

	public URL resolveUrl(final String urlSpecification) {
		try {
			if (baseUrl == null) {
				return new URL(urlSpecification);
			} else {
				return new URL(baseUrl, urlSpecification);
			}
		} catch (final MalformedURLException e) {
			e.printStackTrace(); // TODO log
			return null;
		}
	}

	/**
	 * Flush any cached styles for the given element.
	 *
	 * @param element
	 *            INode for which styles are to be flushed.
	 */
	public void flushStyles(final INode node) {
		styleMap.remove(node);
	}

	/**
	 * Flush all styles used by the given document. A StyleSheet may be shared by multiple documents, so we only remove
	 * elements for the specific document.
	 *
	 * @param document
	 *            The document for which to flush cached styles.
	 */
	public void flushAllStyles(final IDocument document) {
		for (final Iterator<Map.Entry<INode, Styles>> iter = styleMap.entrySet().iterator(); iter.hasNext();) {
			final Map.Entry<INode, Styles> entry = iter.next();
			IDocument nodeDoc = null;
			if (entry.getKey() != null) {
				nodeDoc = entry.getKey().getDocument();
			}
			if (nodeDoc == null || document.equals(nodeDoc)) {
				// The style is also flushed if the node is not attached to an document any more.
				iter.remove();
			}
		}
	}

	/**
	 * @param parent
	 *            parent element of the pseudo-element
	 * @return the 'before' pseudo-element for the given parent element, or null if there is no such element defined in
	 *         the stylesheet
	 */
	@Deprecated
	public IElement getPseudoElementBefore(final INode parent) {
		return getPseudoElement(parent, CSS.PSEUDO_BEFORE);
	}

	/**
	 * @param parent
	 *            parent element of the pseudo-element
	 * @return the 'after' pseudo-element for the given parent element, or null if there is no such element defined in
	 *         the stylesheet
	 */
	@Deprecated
	public IElement getPseudoElementAfter(final INode parent) {
		return getPseudoElement(parent, CSS.PSEUDO_AFTER);
	}

	private IElement getPseudoElement(final INode parent, final String pseudoElementName) {
		Assert.isNotNull(parent, "The parent node must not be null!");

		final org.eclipse.vex.core.internal.css.Styles.PseudoElement pseudoElement = org.eclipse.vex.core.internal.css.Styles.PseudoElement.parse(pseudoElementName);
		final Styles parentStyles = getStyles(parent);
		if (parentStyles == null || !parentStyles.hasPseudoElement(pseudoElement)) {
			return null;
		}

		final Styles pseudoElementStyles = parentStyles.getPseudoElementStyles(pseudoElement);
		if (pseudoElementStyles == null || !pseudoElementStyles.isContentDefined()) {
			return null;
		}

		return new PseudoElement(parent, pseudoElementName.toLowerCase());
	}

	/**
	 * Returns the styles for the given element. The styles are cached to ensure reasonable performance.
	 *
	 * @param node
	 *            Node for which to calculate the styles.
	 */
	public Styles getStyles(final INode node) {

		if (node instanceof PseudoElement) {
			return getStyles(((PseudoElement) node).getParentNode()).getPseudoElementStyles(org.eclipse.vex.core.internal.css.Styles.PseudoElement.parse(((PseudoElement) node).getName()));
		} else {
			if (styleMap.containsKey(node)) {
				return styleMap.get(node);
			}
		}

		// Style is not cached - calculate styles
		final Styles styles = calculateStyles(node);
		styleMap.put(node, styles);

		return styles;
	}

	private Styles calculateStyles(final INode node) {

		// getApplicableDeclarations returns the elements styles and also pseudo element styles
		final Map<String, Map<String, LexicalUnit>> decls = getApplicableDeclarations(node);

		// The null key contains the element's direct styles
		Styles parentStyles = null;
		if (node != null && node.getParent() != null) {
			parentStyles = getStyles(node.getParent());
		}
		final Styles styles = calculateNodeStyles(node, decls.get(null), parentStyles);
		if (styles == null) {
			return null;
		}

		// Now calculate the pseudo element styles and store the in the parent's Styles
		decls.remove(null);
		for (final Entry<String, Map<String, LexicalUnit>> entry : decls.entrySet()) {
			final String pseudoElement = entry.getKey();
			final Styles pseudoElementStyles = calculateNodeStyles(node, entry.getValue(), styles);
			styles.putPseudoElementStyles(org.eclipse.vex.core.internal.css.Styles.PseudoElement.parse(pseudoElement), pseudoElementStyles);
		}

		return styles;
	}

	private Styles calculateNodeStyles(final INode node, final Map<String, LexicalUnit> decls, final Styles parentStyles) {
		final Styles styles = new Styles();

		styles.setBaseUrl(baseUrl);

		for (final IProperty property : CSS_PROPERTIES) {
			final LexicalUnit lexicalUnit = decls.get(property.getName());
			final Object value = property.calculate(lexicalUnit, parentStyles, styles, node);
			styles.put(property.getName(), value);
		}

		// Now, map font-family, font-style, font-weight, and font-size onto
		// an AWT font.

		int styleFlags = FontSpec.PLAIN;
		final String fontStyle = styles.getFontStyle();
		if (fontStyle.equals(CSS.ITALIC) || fontStyle.equals(CSS.OBLIQUE)) {
			styleFlags |= FontSpec.ITALIC;
		}
		if (styles.getFontWeight() > 550) {
			// 550 is halfway btn normal (400) and bold (700)
			styleFlags |= FontSpec.BOLD;
		}
		final String textDecoration = styles.getTextDecoration();
		if (textDecoration.equals(CSS.UNDERLINE)) {
			styleFlags |= FontSpec.UNDERLINE;
		} else if (textDecoration.equals(CSS.OVERLINE)) {
			styleFlags |= FontSpec.OVERLINE;
		} else if (textDecoration.equals(CSS.LINE_THROUGH)) {
			styleFlags |= FontSpec.LINE_THROUGH;
		}

		styles.setFont(new FontSpec(styles.getFontFamilies(), styleFlags, Math.round(styles.getFontSize())));

		return styles;
	}

	/**
	 * Returns the rules comprising this stylesheet.
	 */
	public List<Rule> getRules() {
		return Collections.unmodifiableList(rules);
	}

	/**
	 * Returns all the declarations that apply to the given element and defined pseudo elements.
	 *
	 * @return The key 'null' in the returned Map contains the node's declarations. Names keys contain the declarations
	 *         for pseudo elements.
	 */
	private Map<String, Map<String, LexicalUnit>> getApplicableDeclarations(final INode node) {
		final List<PropertyDecl> coreDeclarationsForElement = findCoreDeclarationsFor(node);
		Collections.sort(coreDeclarationsForElement, PROPERTY_CASCADE_ORDERING);

		final List<PropertyDecl> stylesheetDeclarationsForElement = findAllDeclarationsFor(node);
		Collections.sort(stylesheetDeclarationsForElement, PROPERTY_CASCADE_ORDERING);

		// Both lists are sorted in cascade order. We can then just stuff them into a map and get the right values
		// since higher-priority values come later and overwrite lower-priority ones.
		// Core styles are at the list's begin, so they are ruled out by stylesheet definitions.
		final List<PropertyDecl> rawDeclarationsForElement = coreDeclarationsForElement;
		rawDeclarationsForElement.addAll(stylesheetDeclarationsForElement);

		final Map<String, Map<String, PropertyDecl>> distilledDeclarations = new HashMap<String, Map<String, PropertyDecl>>();
		final Map<String, Map<String, LexicalUnit>> values = new HashMap<String, Map<String, LexicalUnit>>();
		// Key null for nodes direct styles
		distilledDeclarations.put(null, new HashMap<String, PropertyDecl>());
		values.put(null, new HashMap<String, LexicalUnit>());
		for (final PropertyDecl declaration : rawDeclarationsForElement) {
			String pseudoElement = null;
			final Selector sel = declaration.getRule().getSelector();
			if (sel instanceof DescendantSelector && ((DescendantSelector) sel).getSimpleSelector().getSelectorType() == Selector.SAC_PSEUDO_ELEMENT_SELECTOR) {
				// Get the pseudo elements name, if this declaration comes from an SAC_PSEUDO_ELEMENT_SELECTOR
				final ElementSelector elementSel = (ElementSelector) ((DescendantSelector) sel).getSimpleSelector();
				pseudoElement = elementSel.getLocalName().toLowerCase();
			}

			PropertyDecl previousDeclaration = null;
			if (distilledDeclarations.containsKey(pseudoElement)) {
				previousDeclaration = distilledDeclarations.get(pseudoElement).get(declaration.getProperty());
			} else {
				distilledDeclarations.put(pseudoElement, new HashMap<String, PropertyDecl>());
			}
			if (previousDeclaration == null || !previousDeclaration.isImportant() || declaration.isImportant()) {
				distilledDeclarations.get(pseudoElement).put(declaration.getProperty(), declaration);
				if (values.containsKey(pseudoElement)) {
					values.get(pseudoElement).put(declaration.getProperty(), declaration.getValue());
				} else {
					values.put(pseudoElement, new HashMap<String, LexicalUnit>());
					values.get(pseudoElement).put(declaration.getProperty(), declaration.getValue());
				}
			}
		}

		return values;
	}

	private List<PropertyDecl> findAllDeclarationsFor(final INode node) {
		final List<PropertyDecl> rawDeclarations = new ArrayList<PropertyDecl>();

		for (final Rule rule : rules) {
			if (rule.matches(node)) {
				final PropertyDecl[] ruleDecls = rule.getPropertyDecls();
				for (final PropertyDecl ruleDecl : ruleDecls) {
					rawDeclarations.add(ruleDecl);
				}
			}
		}
		return rawDeclarations;
	}

	private List<PropertyDecl> findCoreDeclarationsFor(final INode node) {
		final List<PropertyDecl> rawDeclarations = new ArrayList<PropertyDecl>();

		for (final Rule rule : coreRules) {
			if (rule.matches(node)) {
				final PropertyDecl[] ruleDecls = rule.getPropertyDecls();
				for (final PropertyDecl ruleDecl : ruleDecls) {
					rawDeclarations.add(ruleDecl);
				}
			}
		}
		return rawDeclarations;
	}

	/**
	 * This method is only public to be available for unit testing. It is not meant to be used in an implementation.
	 *
	 * @param node
	 * @return
	 */
	public Map<INode, Styles> testGetStylesCache() {
		return styleMap;
	}
}
