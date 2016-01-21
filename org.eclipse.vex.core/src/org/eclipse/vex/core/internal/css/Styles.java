/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Dave Holroyd - Implement text decoration
 *     Mohamadou Nassourou - Bug 298912 - rudimentary support for images
 *     Carsten Hiesserich - added OutlineContent property
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.internal.core.Length;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitor;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.w3c.css.sac.LexicalUnit;

/**
 * Represents the computed style properties for a particular element.
 */
public class Styles {

	public static enum PseudoElement {
		BEFORE, AFTER;

		public static PseudoElement parse(final String string) {
			if (string == null) {
				throw new NullPointerException("Cannot parse 'null' as PseudoElement.");
			}
			final String lowerString = string.toLowerCase();
			for (final PseudoElement value : values()) {
				if (value.toString().toLowerCase().equals(lowerString)) {
					return value;
				}
			}
			throw new IllegalArgumentException("'" + string + "' is not a valid PseudoElement name.");
		}

		public String key() {
			return toString().toLowerCase();
		}
	}

	/** Maps property name (String) => value (Object) */
	private final Map<String, Object> values = new HashMap<String, Object>();

	/**
	 * This Map contains the Styles for all pseudo elements of the element that this Style belongs to. Key is the pseudo
	 * elements name.
	 */
	private final Map<String, Styles> pseudoElementStyles = new HashMap<String, Styles>();

	private List<LexicalUnit> contentLexicalUnits;
	private FontSpec font;
	private URL baseUrl;

	public URL getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(final URL baseUrl) {
		this.baseUrl = baseUrl;
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
	 * Returns the value of the given property, or null if the property does not have a value.
	 *
	 * @param propertyName
	 * @return
	 */
	public Object get(final String propertyName) {
		return values.get(propertyName);
	}

	/**
	 * Returns the value of the <code>backgroundColor</code> property.
	 */
	public Color getBackgroundColor() {
		return (Color) values.get(CSS.BACKGROUND_COLOR);
	}

	/**
	 * Returns the value of the <code>borderBottomColor</code> property.
	 */
	public Color getBorderBottomColor() {
		return (Color) values.get(CSS.BORDER_BOTTOM_COLOR);
	}

	/**
	 * Returns the value of the <code>borderBottomStyle</code> property.
	 */
	public String getBorderBottomStyle() {
		return (String) values.get(CSS.BORDER_BOTTOM_STYLE);
	}

	/**
	 * Returns the value of the <code>borderLeftColor</code> property.
	 */
	public Color getBorderLeftColor() {
		return (Color) values.get(CSS.BORDER_LEFT_COLOR);
	}

	/**
	 * Returns the value of the <code>borderLeftStyle</code> property.
	 */
	public String getBorderLeftStyle() {
		return (String) values.get(CSS.BORDER_LEFT_STYLE);
	}

	/**
	 * Returns the value of the <code>borderRightColor</code> property.
	 */
	public Color getBorderRightColor() {
		return (Color) values.get(CSS.BORDER_RIGHT_COLOR);
	}

	/**
	 * Returns the value of the <code>borderRightStyle</code> property.
	 */
	public String getBorderRightStyle() {
		return (String) values.get(CSS.BORDER_RIGHT_STYLE);
	}

	/**
	 * Returns the value of the <code>borderSpacing</code> property.
	 */
	public BorderSpacingProperty.Value getBorderSpacing() {
		return (BorderSpacingProperty.Value) values.get(CSS.BORDER_SPACING);
	}

	/**
	 * Returns the value of the <code>borderTopColor</code> property.
	 */
	public Color getBorderTopColor() {
		return (Color) values.get(CSS.BORDER_TOP_COLOR);
	}

	/**
	 * Returns the value of the <code>borderTopStyle</code> property.
	 */
	public String getBorderTopStyle() {
		return (String) values.get(CSS.BORDER_TOP_STYLE);
	}

	/**
	 * Returns the value of the <code>color</code> property.
	 */
	public Color getColor() {
		return (Color) values.get(CSS.COLOR);
	}

	/**
	 * @return <code>true</code> if the stylesheet defined content for this element.
	 */
	public boolean isContentDefined() {
		return contentLexicalUnits.size() > 0;
	}

	/**
	 * Returns a <code>List</code> of <code>ContentPart</code> objects representing the <code>content</code> property.
	 * <br />
	 * The content is parsed on every access to get the actual values for attributes. Do not try to get the content via
	 * the {@link #get(String)} method!
	 *
	 * @param node
	 *            The INode to get attr(...) values from
	 */
	public List<String> getTextualContent(final INode node) {
		final List<String> content = new ArrayList<String>();
		for (LexicalUnit lexicalUnit : contentLexicalUnits) {
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
						final String attributeValue = element.getAttributeValue(currentLexicalUnit.getStringValue());
						if (attributeValue != null) {
							content.add(attributeValue);
						}
					}

					@Override
					public void visit(final IProcessingInstruction pi) {
						if (currentLexicalUnit.getStringValue().equalsIgnoreCase(CSS.PSEUDO_TARGET)) {
							content.add(pi.getTarget());
						}
					}
				});
				break;
			}
			lexicalUnit = lexicalUnit.getNextLexicalUnit();
		}
		return content;
	}

	public List<IPropertyContent> getAllContent(final INode node) {
		final List<IPropertyContent> allContent = new ArrayList<IPropertyContent>();
		for (LexicalUnit lexicalUnit : contentLexicalUnits) {
			final IPropertyContent content = getContent(lexicalUnit, node);
			if (content != null) {
				allContent.add(content);
			}
			lexicalUnit = lexicalUnit.getNextLexicalUnit();
		}
		return allContent;
	}

	private IPropertyContent getContent(final LexicalUnit lexicalUnit, final INode node) {
		switch (lexicalUnit.getLexicalUnitType()) {
		case LexicalUnit.SAC_STRING_VALUE:
			// content: "A String"
			return new TextualContent(lexicalUnit.getStringValue());
		case LexicalUnit.SAC_ATTR:
			// content: attr(attributeName)
			final LexicalUnit currentLexicalUnit = lexicalUnit;
			return node.accept(new BaseNodeVisitorWithResult<IPropertyContent>() {
				@Override
				public IPropertyContent visit(final IElement element) {
					final String attributeValue = element.getAttributeValue(currentLexicalUnit.getStringValue());
					if (attributeValue != null) {
						return new TextualContent(attributeValue);
					}
					return null;
				}

				@Override
				public IPropertyContent visit(final IProcessingInstruction pi) {
					if (currentLexicalUnit.getStringValue().equalsIgnoreCase(CSS.PSEUDO_TARGET)) {
						return new TextualContent(pi.getTarget());
					}
					return null;
				}
			});
		case LexicalUnit.SAC_URI:
			// content: url("<some URI of an image>")
			try {
				return new URIContent(new URI(lexicalUnit.getStringValue()));
			} catch (final URISyntaxException e) {
				e.printStackTrace();
				return null;
			}
		default:
			return null;
		}
	}

	/**
	 * Returns the value of the <code>display</code> property.
	 */
	public String getDisplay() {
		return (String) values.get(CSS.DISPLAY);
	}

	/**
	 * @return true if the <code>display</code> property is not 'none'.
	 */
	public boolean isDisplayed() {
		return !CSS.NONE.equals(getDisplay());
	}

	/**
	 * Returns the value of the <code>font</code> property.
	 */
	public FontSpec getFont() {
		return font;
	}

	/**
	 * Returns the value of the <code>fontFamily</code> property.
	 */
	public String[] getFontFamilies() {
		return (String[]) values.get(CSS.FONT_FAMILY);
	}

	/**
	 * Returns the value of the <code>fontSize</code> property.
	 */
	public float getFontSize() {
		return ((Float) values.get(CSS.FONT_SIZE)).floatValue();
	}

	/**
	 * Returns the value of the <code>fontStyle</code> property.
	 */
	public String getFontStyle() {
		return (String) values.get(CSS.FONT_STYLE);
	}

	/**
	 * Returns the value of the <code>fontWeight</code> property.
	 */
	public int getFontWeight() {
		return ((Integer) values.get(CSS.FONT_WEIGHT)).intValue();
	}

	/**
	 * Returns the value of the <code>_vex-inline-marker</code> property.
	 */
	public String getInlineMarker() {
		return (String) values.get(CSS.INLINE_MARKER);
	}

	/**
	 * Returns the value of the <code>lineHeight</code> property.
	 */
	public int getLineHeight() {
		return ((Length) values.get(CSS.LINE_HEIGHT)).get(Math.round(getFontSize()));
	}

	/**
	 * Returns the value of the <code>listStyleType</code> property.
	 */
	public String getListStyleType() {
		return (String) values.get(CSS.LIST_STYLE_TYPE);
	}

	/**
	 * Returns the value of the <code>textAlign</code> property.
	 */
	public String getTextAlign() {
		return (String) values.get(CSS.TEXT_ALIGN);
	}

	/**
	 * Returns the value of the <code>textDecoration</code> property.
	 */
	public String getTextDecoration() {
		return (String) values.get(CSS.TEXT_DECORATION);
	}

	/**
	 * Returns the value of the <code>whiteSpace</code> property.
	 */
	public String getWhiteSpace() {
		return (String) values.get(CSS.WHITE_SPACE);
	}

	/**
	 * Sets the value of a property in this stylesheet.
	 *
	 * @param propertyName
	 *            Name of the property being set.
	 * @param value
	 *            Value of the property.
	 */
	public void put(final String propertyName, final Object value) {
		values.put(propertyName, value);
	}

	public void putPseudoElementStyles(final PseudoElement pseudoElement, final Styles styles) {
		pseudoElementStyles.put(pseudoElement.key(), styles);
	}

	public Styles getPseudoElementStyles(final PseudoElement pseudoElement) {
		if (hasPseudoElement(pseudoElement)) {
			return pseudoElementStyles.get(pseudoElement.key());
		} else {
			// There are no styles for the given pseudo element - return this; better save than sorry!
			return this;
		}
	}

	/**
	 * Check if the given pseudo element is defined for this node.
	 *
	 * @param pseudoElement
	 * @return <code>true</code> when the given pseudo element is defined.
	 */
	public boolean hasPseudoElement(final PseudoElement pseudoElement) {
		return pseudoElementStyles.containsKey(pseudoElement.key());
	}

	/**
	 * Sets the LexicalUnits of the <code>content</code> property.
	 *
	 * @param content
	 *            <code>List</code> of <code>LexicalUnits</code> objects defining the content.
	 */
	public void setContent(final List<LexicalUnit> content) {
		contentLexicalUnits = content;
	}

	/**
	 * Sets the value of the <code>font</code> property.
	 *
	 * @param font
	 *            new value for the <code>font</code> property.
	 */
	public void setFont(final FontSpec font) {
		this.font = font;
	}

	public Length getElementWidth() {
		return (Length) values.get(CSS.WIDTH);
	}

	public Length getElementHeight() {
		return (Length) values.get(CSS.HEIGHT);
	}

	public boolean hasBackgroundImage() {
		return values.get(CSS.BACKGROUND_IMAGE) != null;
	}

	public String getBackgroundImage() {
		final Object value = values.get(CSS.BACKGROUND_IMAGE);
		if (value == null) {
			return BackgroundImageProperty.DEFAULT;
		}
		return value.toString();
	}

	/**
	 * @return the value of border-bottom-width
	 */
	public int getBorderBottomWidth() {
		return ((Integer) values.get(CSS.BORDER_BOTTOM_WIDTH)).intValue();
	}

	/**
	 * @return the value of border-left-width
	 */
	public int getBorderLeftWidth() {
		return ((Integer) values.get(CSS.BORDER_LEFT_WIDTH)).intValue();
	}

	/**
	 * @return the value of border-right-width
	 */
	public int getBorderRightWidth() {
		return ((Integer) values.get(CSS.BORDER_RIGHT_WIDTH)).intValue();
	}

	/**
	 * @return the value of border-top-width
	 */
	public int getBorderTopWidth() {
		return ((Integer) values.get(CSS.BORDER_TOP_WIDTH)).intValue();
	}

	/**
	 * @return the value of margin-bottom
	 */
	public Length getMarginBottom() {
		return (Length) values.get(CSS.MARGIN_BOTTOM);
		// return marginBottom;
	}

	/**
	 * @return the value of margin-left
	 */
	public Length getMarginLeft() {
		return (Length) values.get(CSS.MARGIN_LEFT);
	}

	/**
	 * @return the value of margin-right
	 */
	public Length getMarginRight() {
		return (Length) values.get(CSS.MARGIN_RIGHT);
	}

	/**
	 * @return the value of margin-top
	 */
	public Length getMarginTop() {
		return (Length) values.get(CSS.MARGIN_TOP);
	}

	/**
	 * @return the value of padding-bottom
	 */
	public Length getPaddingBottom() {
		return (Length) values.get(CSS.PADDING_BOTTOM);
	}

	/**
	 * @return the value of padding-left
	 */
	public Length getPaddingLeft() {
		return (Length) values.get(CSS.PADDING_LEFT);
	}

	/**
	 * @return the value of padding-right
	 */
	public Length getPaddingRight() {
		return (Length) values.get(CSS.PADDING_RIGHT);
	}

	/**
	 * @return the value of padding-top
	 */
	public Length getPaddingTop() {
		return (Length) values.get(CSS.PADDING_TOP);
	}

	/**
	 * @return the IElement whose text content should be used in the outline view.
	 */
	public Object getOutlineContent() {
		return values.get(CSS.OUTLINE_CONTENT);
	}

}
