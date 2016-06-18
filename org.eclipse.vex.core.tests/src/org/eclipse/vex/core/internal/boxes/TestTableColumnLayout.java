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
package org.eclipse.vex.core.internal.boxes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class TestTableColumnLayout {

	private TableColumnDefinitions layout;

	@Before
	public void initLayout() {
		layout = new TableColumnDefinitions();
	}

	@Test
	public void shouldCountGivenColumns() throws Exception {
		layout.addColumn(0, null, null);
		layout.addColumn(0, null, null);
		layout.addColumn(0, null, null);
		assertThat(layout.getLastIndex(), is(equalTo(3)));
	}

	@Test
	public void givenColumnIndex_whenColumnIndexIsZero_shouldReturnActualColumnIndex() throws Exception {
		assertThat(layout.addColumn(0, null, null), is(equalTo(1)));
	}

	@Test
	public void givenColumnIndex_whenColumnIndexGreaterThanLastColumnIndex_shouldReturnGivenColumnIndex() throws Exception {
		assertThat(layout.addColumn(2, null, null), is(equalTo(2)));
	}

	@Test
	public void givenColumnIndex_whenColumnIndexLessThanLastColumnIndex_shouldReturnGivenColumnIndex() throws Exception {
		layout.addColumn(2, null, null);
		assertThat(layout.addColumn(1, null, null), is(equalTo(3)));
	}

	@Test
	public void givenColumnName_whenColumnIsDefined_shouldReturnColumnIndex() throws Exception {
		layout.addColumn(4, "myColumn", null);
		assertThat(layout.getIndex("myColumn"), is(equalTo(4)));
	}

	@Test
	public void givenColumnName_whenColumnIsNotDefined_shoudlReturnZero() throws Exception {
		assertThat(layout.getIndex("someUndefinedColumn"), is(equalTo(0)));
	}

	@Test
	public void givenSpan_shouldProvideStartIndexByName() throws Exception {
		layout.addSpan(2, 4, "span2To4");
		assertThat(layout.getStartIndex("span2To4"), is(equalTo(2)));
	}

	@Test
	public void givenSpan_shouldProvideEndIndexByName() throws Exception {
		layout.addSpan(3, 8, "span3To8");
		assertThat(layout.getEndIndex("span3To8"), is(equalTo(8)));
	}

	@Test
	public void givenChildLayout_whenColumnIsOnlyDefinedInParent_shouldProvideIndexFromParent() throws Exception {
		layout.addColumn(1, "columnInParent", null);
		assertThat(new TableColumnDefinitions(layout).getIndex("columnInParent"), is(equalTo(1)));
	}
}
