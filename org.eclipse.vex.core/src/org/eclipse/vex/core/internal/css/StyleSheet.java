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
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitor;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.w3c.css.sac.LexicalUnit;

/**
 * Represents a CSS style sheet.
 */
public class StyleSheet {

	public static final StyleSheet NULL = new StyleSheet(Collections.<Rule> emptyList());

	private static final Comparator<PropertyDecl> PROPERTY_CASCADE_ORDERING = new Comparator<PropertyDecl>() {
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
			new LengthProperty(CSS.WIDTH, IProperty.Axis.HORIZONTAL), new BackgroundImageProperty() };

	/**
	 * The rules that comprise the stylesheet.
	 */
	private final List<Rule> rules;

	/**
	 * Computing styles can be expensive, e.g. we have to calculate the styles of all parents of an element. We
	 * therefore cache styles in a map of element => styles. We use a WeakHashMap here that does not prevent the INode's
	 * from being GC'ed. The created pseudo-elements are also cached. Without caching, they would have to be recreated
	 * for every layout update. Note that the entries of the Map are only collected, when one of the Maps method is
	 * called the next time, so entries will stay on the heap until another VexEditor is launched. To prevent this, the
	 * Styles are flushed in BaseVexWidget#dispose.
	 */
	private final Map<INode, Styles> styleMap = new WeakHashMap<INode, Styles>(50);
	private final Map<INode, PseudoElementEntry> pseudoElementMap = new WeakHashMap<INode, PseudoElementEntry>(50);

	/**
	 * Class constructor.
	 * 
	 * @param rules
	 *            Rules that constitute the style sheet.
	 */
	public StyleSheet(final Collection<Rule> rules) {
		this.rules = new ArrayList<Rule>(rules);
	}

	/**
	 * Flush any cached styles for the given element.
	 * 
	 * @param element
	 *            INode for which styles are to be flushed.
	 */
	public void flushStyles(final INode node) {
		styleMap.remove(node);
		pseudoElementMap.remove(node);
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
			if (entry.getKey().getDocument().equals(document)) {
				pseudoElementMap.remove(entry.getKey());
				iter.remove();
			}
		}
	}

	/**
	 * Returns a pseudo-element representing content to be displayed after the given element, or null if there is no
	 * such content.
	 * 
	 * @param element
	 *            Parent element of the pseudo-element.
	 * @return
	 */
	public PseudoElement getAfterElement(final IElement element) {
		if (pseudoElementMap.containsKey(element)) {
			return pseudoElementMap.get(element).getAfterElement();
		}

		return null;
	}

	/**
	 * Returns a pseudo-element representing content to be displayed before the given element, or null if there is no
	 * such content.
	 * 
	 * @param element
	 *            Parent element of the pseudo-element.
	 */
	public PseudoElement getBeforeElement(final IElement element) {
		if (pseudoElementMap.containsKey(element)) {
			return pseudoElementMap.get(element).getBeforeElement();
		}

		return null;
	}

	/**
	 * Returns the styles for the given element. The styles are cached to ensure reasonable performance.
	 * 
	 * @param node
	 *            Node for which to calculate the styles.
	 */
	public Styles getStyles(final INode node) {

		// Get style from cache if possible
		if (node instanceof PseudoElement) {
			// Styles for pseudo-elements are calculated once and stored in the element.
			return ((PseudoElement) node).getStyles();
		} else {
			if (styleMap.containsKey(node)) {
				return styleMap.get(node);
			}
		}

		// Style is not cached - calculate styles
		final Styles styles = calculateStyles(node);
		styleMap.put(node, styles);

		// Create the pseudo elements if this node is an IElement
		// The pseudo elements are also cahed, so they don't have to
		// be recreated for every layout update.
		if (!(node instanceof PseudoElement) && node instanceof IElement) {
			PseudoElement pseudoBefore = new PseudoElement((IElement) node, PseudoElement.BEFORE);
			final Styles beforeStyles = calculateStyles(pseudoBefore);
			if (beforeStyles != null) {
				pseudoBefore.setStyles(beforeStyles);
			} else {
				pseudoBefore = null;
			}

			PseudoElement pseudoAfter = new PseudoElement((IElement) node, PseudoElement.AFTER);
			final Styles afterStyles = calculateStyles(pseudoAfter);
			if (afterStyles != null) {
				pseudoAfter.setStyles(afterStyles);
			} else {
				pseudoAfter = null;
			}
			pseudoElementMap.put(node, new PseudoElementEntry(pseudoBefore, pseudoAfter));
		}

		return styles;
	}

	private Styles calculateStyles(final INode node) {

		final Styles styles = new Styles();
		Styles parentStyles = null;
		if (node != null && node.getParent() != null) {
			parentStyles = getStyles(node.getParent());
		}

		final Map<String, LexicalUnit> decls = getApplicableDeclarations(node);

		LexicalUnit lexicalUnit;

		// If we're finding a pseudo-element, look at the 'content' property
		// first, since most of the time it'll be empty and we'll return null.
		if (node instanceof PseudoElement) {
			lexicalUnit = decls.get(CSS.CONTENT);
			if (lexicalUnit == null) {
				return null;
			}

			final List<String> content = new ArrayList<String>();
			while (lexicalUnit != null) {
				switch (lexicalUnit.getLexicalUnitType()) {
				case LexicalUnit.SAC_STRING_VALUE:
					// content: "A String"
					content.add(lexicalUnit.getStringValue());
					break;
				case LexicalUnit.SAC_ATTR:
					// content: attr(attributeName)
					final LexicalUnit currentLexicalUnit = lexicalUnit;
					node.accept(new BaseNodeVisitor() {
						@Override
						public void visit(final IElement element) {
							final String attributeValue = element.getParentElement().getAttributeValue(currentLexicalUnit.getStringValue());
							if (attributeValue != null) {
								content.add(attributeValue);
							}
						}
					});
					break;
				}
				lexicalUnit = lexicalUnit.getNextLexicalUnit();
			}
			styles.setContent(content);
		}

		for (final IProperty property : CSS_PROPERTIES) {
			lexicalUnit = decls.get(property.getName());
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
	 * Returns all the declarations that apply to the given element.
	 */
	private Map<String, LexicalUnit> getApplicableDeclarations(final INode node) {
		final List<PropertyDecl> rawDeclarationsForElement = findAllDeclarationsFor(node);

		// Sort in cascade order. We can then just stuff them into a
		// map and get the right values since higher-priority values
		// come later and overwrite lower-priority ones.
		Collections.sort(rawDeclarationsForElement, PROPERTY_CASCADE_ORDERING);

		final Map<String, PropertyDecl> distilledDeclarations = new HashMap<String, PropertyDecl>();
		final Map<String, LexicalUnit> values = new HashMap<String, LexicalUnit>();
		for (final PropertyDecl declaration : rawDeclarationsForElement) {
			final PropertyDecl previousDeclaration = distilledDeclarations.get(declaration.getProperty());
			if (previousDeclaration == null || !previousDeclaration.isImportant() || declaration.isImportant()) {
				distilledDeclarations.put(declaration.getProperty(), declaration);
				values.put(declaration.getProperty(), declaration.getValue());
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

	/**
	 * This method is only public to be available for unit testing. It is not meant to be used in an implementation.
	 * 
	 * @param node
	 * @return
	 */
	public Map<INode, Styles> testGetStylesCache() {
		return styleMap;
	}

	/**
	 * An entry in the pseudoElementMap. PseudoElements keep a reference to their parent Elements, so this class uses a
	 * WeakReference for cached PseudoElements to avoid a circular dependency.
	 * 
	 */
	private class PseudoElementEntry {
		private final WeakReference<PseudoElement> before;
		private final WeakReference<PseudoElement> after;

		public PseudoElementEntry(final PseudoElement before, final PseudoElement after) {
			this.before = new WeakReference<PseudoElement>(before);
			this.after = new WeakReference<PseudoElement>(after);
		}

		public PseudoElement getBeforeElement() {
			return before.get();
		}

		public PseudoElement getAfterElement() {
			return after.get();
		}
	}
}
