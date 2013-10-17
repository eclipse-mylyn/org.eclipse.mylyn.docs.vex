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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.FontSpec;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitor;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.w3c.css.sac.LexicalUnit;

/**
 * Represents the computed style properties for a particular element.
 */
public class Styles {

	/** Maps property name (String) => value (Object) */
	private final Map<String, Object> values = new HashMap<String, Object>();

	/**
	 * This Map contains the Styles for all pseudo elements of the element that this Style belongs to. Key is the pseudo
	 * elements name.
	 */
	private final Map<String, Styles> pseudoElementStyles = new HashMap<String, Styles>();

	private List<LexicalUnit> contentLexicalUnits;
	private FontSpec font;

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
	 * Returns a <code>List</code> of <code>ContentPart</code> objects representing the <code>content</code> property.<br />
	 * The content is parsed on every access to get the actual values for attributes. Do not try to get the content via
	 * the {@link #get(String)} method!
	 * 
	 * @param node
	 *            The INode to get attr(...) values from
	 */
	public List<String> getContent(final INode node) {
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
	 * Returns the value of the <code>lineHeight</code> property.
	 */
	public int getLineHeight() {
		return ((RelativeLength) values.get(CSS.LINE_HEIGHT)).get(Math.round(getFontSize()));
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

	public void putPseudoElementStyles(final String pseudoElementName, final Styles pseudoElStyles) {
		pseudoElementStyles.put(pseudoElementName, pseudoElStyles);
	}

	public Styles getPseudoElementStyles(final String pseudoElementName) {
		if (pseudoElementStyles.containsKey(pseudoElementName.toLowerCase())) {
			return pseudoElementStyles.get(pseudoElementName.toLowerCase());
		} else {
			// There are no styles for the given pseudo element - return this
			return this;
		}
	}

	/**
	 * Check if the given pseudo element is defined for this node.
	 * 
	 * @param pseudoElementName
	 * @return <code>true</code> when the given pseudo element is defined.
	 */
	public boolean hasPseudoElement(final String pseudoElementName) {
		return pseudoElementStyles.containsKey(pseudoElementName.toLowerCase());
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

	public RelativeLength getElementWidth() {
		return (RelativeLength) values.get(CSS.WIDTH);
	}

	public RelativeLength getElementHeight() {
		return (RelativeLength) values.get(CSS.HEIGHT);
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
	public RelativeLength getMarginBottom() {
		return (RelativeLength) values.get(CSS.MARGIN_BOTTOM);
		// return marginBottom;
	}

	/**
	 * @return the value of margin-left
	 */
	public RelativeLength getMarginLeft() {
		return (RelativeLength) values.get(CSS.MARGIN_LEFT);
	}

	/**
	 * @return the value of margin-right
	 */
	public RelativeLength getMarginRight() {
		return (RelativeLength) values.get(CSS.MARGIN_RIGHT);
	}

	/**
	 * @return the value of margin-top
	 */
	public RelativeLength getMarginTop() {
		return (RelativeLength) values.get(CSS.MARGIN_TOP);
	}

	/**
	 * @return the value of padding-bottom
	 */
	public RelativeLength getPaddingBottom() {
		return (RelativeLength) values.get(CSS.PADDING_BOTTOM);
	}

	/**
	 * @return the value of padding-left
	 */
	public RelativeLength getPaddingLeft() {
		return (RelativeLength) values.get(CSS.PADDING_LEFT);
	}

	/**
	 * @return the value of padding-right
	 */
	public RelativeLength getPaddingRight() {
		return (RelativeLength) values.get(CSS.PADDING_RIGHT);
	}

	/**
	 * @return the value of padding-top
	 */
	public RelativeLength getPaddingTop() {
		return (RelativeLength) values.get(CSS.PADDING_TOP);
	}

	/**
	 * @return the IElement whose text content should be used in the outline view.
	 */
	public Object getOutlineContent() {
		return values.get(CSS.OUTLINE_CONTENT);
	}

}
