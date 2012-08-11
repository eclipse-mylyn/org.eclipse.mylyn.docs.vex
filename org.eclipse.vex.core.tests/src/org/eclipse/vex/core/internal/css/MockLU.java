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

	public static LexicalUnit INHERIT = new MockLU(LexicalUnit.SAC_INHERIT);

	public static LexicalUnit createFloat(final short units, final float value) {
		final MockLU lu = new MockLU(units);
		lu.setFloatValue(value);
		return lu;
	}

	public static LexicalUnit createIdent(final String s) {
		final MockLU lu = new MockLU(LexicalUnit.SAC_IDENT);
		lu.setStringValue(s);
		return lu;
	}

	public static LexicalUnit createString(final String s) {
		final MockLU lu = new MockLU(LexicalUnit.SAC_STRING_VALUE);
		lu.setStringValue(s);
		return lu;
	}

	public static LexicalUnit createAttr(final String attributeName) {
		final MockLU result = new MockLU(LexicalUnit.SAC_ATTR);
		result.setStringValue(attributeName);
		return result;
	}

	public String getDimensionUnitText() {
		return dimensionUnitText;
	}

	public float getFloatValue() {
		return floatValue;
	}

	public String getFunctionName() {
		return functionName;
	}

	public int getIntegerValue() {
		return integerValue;
	}

	public short getLexicalUnitType() {
		return lexicalUnitType;
	}

	public LexicalUnit getNextLexicalUnit() {
		return nextLexicalUnit;
	}

	public LexicalUnit getParameters() {
		return parameters;
	}

	public LexicalUnit getPreviousLexicalUnit() {
		return previousLexicalUnit;
	}

	public String getStringValue() {
		return stringValue;
	}

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
