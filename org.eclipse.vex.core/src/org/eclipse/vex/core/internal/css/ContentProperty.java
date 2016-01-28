/*******************************************************************************
 * Copyright (c) 2016 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.w3c.css.sac.LexicalUnit;

public class ContentProperty extends AbstractProperty {

	public ContentProperty() {
		super(CSS.CONTENT);
	}

	@Override
	public List<IPropertyContent> calculate(final LexicalUnit lu, final Styles parentStyles, final Styles styles, final INode node) {
		final List<IPropertyContent> result = new ArrayList<IPropertyContent>();

		LexicalUnit currentLexicalUnit = lu;
		while (currentLexicalUnit != null) {
			IPropertyContent propertyContent;
			switch (currentLexicalUnit.getLexicalUnitType()) {
			case LexicalUnit.SAC_STRING_VALUE:
				propertyContent = stringValue(currentLexicalUnit, result);
				break;
			case LexicalUnit.SAC_ATTR:
				propertyContent = attr(currentLexicalUnit, node, result);
				break;
			case LexicalUnit.SAC_FUNCTION:
				if (CSS.IMAGE_FUNCTION.equalsIgnoreCase(currentLexicalUnit.getFunctionName())) {
					String stylesBaseURI;
					if (styles != null && styles.getBaseUrl() != null) {
						stylesBaseURI = styles.getBaseUrl().toString();
					} else {
						stylesBaseURI = null;
					}
					propertyContent = image(currentLexicalUnit, node, stylesBaseURI);
				} else {
					propertyContent = null;
				}
				break;
			default:
				// ignore other LexicalUnit types
				propertyContent = null;
				break;
			}

			if (propertyContent != null) {
				result.add(propertyContent);
			}

			currentLexicalUnit = currentLexicalUnit.getNextLexicalUnit();
		}

		return result;
	}

	private static IPropertyContent stringValue(final LexicalUnit lexicalUnit, final List<IPropertyContent> result) {
		return new TextualContent(lexicalUnit.getStringValue());
	}

	private static IPropertyContent attr(final LexicalUnit currentLexicalUnit, final INode node, final List<IPropertyContent> result) {
		final String stringValue = currentLexicalUnit.getStringValue();
		return node.accept(new BaseNodeVisitorWithResult<IPropertyContent>() {
			@Override
			public IPropertyContent visit(final IElement element) {
				return new AttributeDependendContent(element, new QualifiedName(null, stringValue));
			}

			@Override
			public IPropertyContent visit(final IProcessingInstruction pi) {
				if (CSS.PSEUDO_TARGET.equalsIgnoreCase(stringValue)) {
					return new ProcessingInstructionTargetContent(pi);
				}
				return null;
			}
		});
	}

	private static IPropertyContent uri(final LexicalUnit lexicalUnit) {
		return new URIContent(lexicalUnit.getStringValue());
	}

	private static IPropertyContent image(final LexicalUnit lexicalUnit, final INode node, final String stylesBaseURI) {
		final List<IPropertyContent> parameters = parseImageParameters(lexicalUnit.getParameters(), node);
		final String baseURI = determineImageBaseURI(parameters, node, stylesBaseURI);

		return new ImageContent(baseURI, parameters);
	}

	private static List<IPropertyContent> parseImageParameters(final LexicalUnit parameters, final INode node) {
		final List<IPropertyContent> result = new ArrayList<IPropertyContent>();

		LexicalUnit currentLexicalUnit = parameters;
		while (currentLexicalUnit != null) {
			IPropertyContent propertyContent;
			switch (currentLexicalUnit.getLexicalUnitType()) {
			case LexicalUnit.SAC_STRING_VALUE:
				propertyContent = stringValue(currentLexicalUnit, result);
				break;
			case LexicalUnit.SAC_ATTR:
				propertyContent = attr(currentLexicalUnit, node, result);
				break;
			case LexicalUnit.SAC_URI:
				propertyContent = uri(currentLexicalUnit);
				break;
			default:
				// ignore other LexicalUnit types
				propertyContent = null;
				break;
			}

			if (propertyContent != null) {
				result.add(propertyContent);
			}

			currentLexicalUnit = currentLexicalUnit.getNextLexicalUnit();
		}

		return result;
	}

	private static String determineImageBaseURI(final List<IPropertyContent> parameters, final INode node, final String stylesBaseURI) {
		for (final IPropertyContent parameter : parameters) {
			if (parameter instanceof AttributeDependendContent) {
				return node.getBaseURI();
			}
		}
		return stylesBaseURI;
	}
}
