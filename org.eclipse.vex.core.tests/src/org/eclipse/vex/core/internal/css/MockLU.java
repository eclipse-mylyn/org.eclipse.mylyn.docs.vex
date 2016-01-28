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
package org.eclipse.vex.core.internal.css;

import org.w3c.css.sac.LexicalUnit;

/**
 * Dummy LexicalUnit implementation.
 */
public class MockLU implements LexicalUnit {

	public MockLU(final short type) {
		lexicalUnitType = type;
	}

	public static final MockLU INHERIT = new MockLU(LexicalUnit.SAC_INHERIT);

	public static MockLU createFloat(final short units, final float value) {
		final MockLU lu = new MockLU(units);
		lu.setFloatValue(value);
		return lu;
	}

	public static MockLU createIdent(final String s) {
		final MockLU lu = new MockLU(LexicalUnit.SAC_IDENT);
		lu.setStringValue(s);
		return lu;
	}

	public static MockLU createString(final String s) {
		final MockLU lu = new MockLU(LexicalUnit.SAC_STRING_VALUE);
		lu.setStringValue(s);
		return lu;
	}

	public static MockLU createAttr(final String attributeName) {
		final MockLU result = new MockLU(LexicalUnit.SAC_ATTR);
		result.setStringValue(attributeName);
		return result;
	}

	public static MockLU createUri(final String uri) {
		final MockLU result = new MockLU(LexicalUnit.SAC_URI);
		result.setStringValue(uri);
		return result;
	}

	public static MockLU createImage(final MockLU... parameters) {
		final MockLU result = new MockLU(LexicalUnit.SAC_FUNCTION);
		result.setFunctionName(CSS.IMAGE_FUNCTION);
		MockLU firstParameter = null;
		MockLU lastParameter = null;
		for (final MockLU parameter : parameters) {
			if (firstParameter == null) {
				firstParameter = parameter;
			}
			if (lastParameter != null) {
				lastParameter.setNextLexicalUnit(parameter);
				parameter.setPreviousLexicalUnit(lastParameter);
			}
			lastParameter = parameter;
		}
		result.setParameters(firstParameter);
		return result;
	}

	@Override
	public String getDimensionUnitText() {
		return dimensionUnitText;
	}

	@Override
	public float getFloatValue() {
		return floatValue;
	}

	@Override
	public String getFunctionName() {
		return functionName;
	}

	@Override
	public int getIntegerValue() {
		return integerValue;
	}

	@Override
	public short getLexicalUnitType() {
		return lexicalUnitType;
	}

	@Override
	public LexicalUnit getNextLexicalUnit() {
		return nextLexicalUnit;
	}

	@Override
	public LexicalUnit getParameters() {
		return parameters;
	}

	@Override
	public LexicalUnit getPreviousLexicalUnit() {
		return previousLexicalUnit;
	}

	@Override
	public String getStringValue() {
		return stringValue;
	}

	@Override
	public LexicalUnit getSubValues() {
		return subValues;
	}

	public void setDimensionUnitText(final String dimensionUnitText) {
		this.dimensionUnitText = dimensionUnitText;
	}

	public void setFloatValue(final float floatValue) {
		this.floatValue = floatValue;
	}

	public void setFunctionName(final String functionName) {
		this.functionName = functionName;
	}

	public void setIntegerValue(final int integerValue) {
		this.integerValue = integerValue;
	}

	public void setLexicalUnitType(final short lexicalUnitType) {
		this.lexicalUnitType = lexicalUnitType;
	}

	public void setNextLexicalUnit(final LexicalUnit nextLexicalUnit) {
		this.nextLexicalUnit = nextLexicalUnit;
	}

	public void setParameters(final LexicalUnit parameters) {
		this.parameters = parameters;
	}

	public void setPreviousLexicalUnit(final LexicalUnit previousLexicalUnit) {
		this.previousLexicalUnit = previousLexicalUnit;
	}

	public void setStringValue(final String stringValue) {
		this.stringValue = stringValue;
	}

	public void setSubValues(final LexicalUnit subValues) {
		this.subValues = subValues;
	}

	private short lexicalUnitType;
	private LexicalUnit nextLexicalUnit;
	private LexicalUnit previousLexicalUnit;
	private int integerValue;
	private float floatValue;
	private String dimensionUnitText;
	private String functionName;
	private LexicalUnit parameters;
	private String stringValue;
	private LexicalUnit subValues;

}
