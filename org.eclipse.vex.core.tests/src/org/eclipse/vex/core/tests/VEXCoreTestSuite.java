/*******************************************************************************
 * Copyright (c) 2004, 2010 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - bug 306639 - remove serializability from StyleSheet
 *                       and dependend classes
 *******************************************************************************/
package org.eclipse.vex.core.tests;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.vex.core.internal.css.BatikBehaviorTest;
import org.eclipse.vex.core.internal.css.CssTest;
import org.eclipse.vex.core.internal.css.PropertyTest;
import org.eclipse.vex.core.internal.css.RuleTest;
import org.eclipse.vex.core.internal.dom.BasicNodeTest;
import org.eclipse.vex.core.internal.dom.BlockElementBoxTest;
import org.eclipse.vex.core.internal.dom.DTDValidatorTest;
import org.eclipse.vex.core.internal.dom.DocumentContentModelTest;
import org.eclipse.vex.core.internal.dom.DocumentFragmentTest;
import org.eclipse.vex.core.internal.dom.DocumentReaderTest;
import org.eclipse.vex.core.internal.dom.DocumentTest;
import org.eclipse.vex.core.internal.dom.DocumentWriterTest;
import org.eclipse.vex.core.internal.dom.GapContentTest;
import org.eclipse.vex.core.internal.dom.L1ElementHandlingTest;
import org.eclipse.vex.core.internal.dom.L1TextHandlingTest;
import org.eclipse.vex.core.internal.dom.NamespaceStackTest;
import org.eclipse.vex.core.internal.dom.NamespaceTest;
import org.eclipse.vex.core.internal.dom.ParentTest;
import org.eclipse.vex.core.internal.dom.RangeTest;
import org.eclipse.vex.core.internal.dom.SchemaValidatorTest;
import org.eclipse.vex.core.internal.dom.SpaceNormalizerTest;
import org.eclipse.vex.core.internal.dom.TextWrapperTest;
import org.eclipse.vex.core.internal.layout.ImageBoxTest;
import org.eclipse.vex.core.internal.layout.TableLayoutTest;
import org.eclipse.vex.core.internal.layout.TestBlockElementBox;
import org.eclipse.vex.core.internal.layout.TestBlocksInInlines;
import org.eclipse.vex.core.internal.layout.TestDocumentTextBox;
import org.eclipse.vex.core.internal.layout.TestStaticTextBox;
import org.eclipse.vex.core.internal.widget.VexWidgetTest;

public class VEXCoreTestSuite extends TestSuite {
	public static Test suite() {
		return new VEXCoreTestSuite();
	}

	public VEXCoreTestSuite() {
		super("Vex Core Tests");
		addTest(new JUnit4TestAdapter(NamespaceStackTest.class));
		addTest(new JUnit4TestAdapter(NamespaceTest.class));
		addTest(new JUnit4TestAdapter(DocumentReaderTest.class));
		addTest(new JUnit4TestAdapter(DocumentContentModelTest.class));
		addTest(new JUnit4TestAdapter(SchemaValidatorTest.class));
		addTest(new JUnit4TestAdapter(CssTest.class));
		addTest(new JUnit4TestAdapter(BatikBehaviorTest.class));
		addTest(new JUnit4TestAdapter(RangeTest.class));
		addTest(new JUnit4TestAdapter(BasicNodeTest.class));
		addTest(new JUnit4TestAdapter(ParentTest.class));
		addTest(new JUnit4TestAdapter(DocumentTest.class));
		addTest(new JUnit4TestAdapter(L1TextHandlingTest.class));
		addTest(new JUnit4TestAdapter(L1ElementHandlingTest.class));
		addTest(new JUnit4TestAdapter(DocumentFragmentTest.class));
		addTestSuite(PropertyTest.class);
		addTestSuite(RuleTest.class);
		addTestSuite(BlockElementBoxTest.class);
		addTest(new JUnit4TestAdapter(ImageBoxTest.class));
		addTest(new JUnit4TestAdapter(DocumentWriterTest.class));
		addTestSuite(DTDValidatorTest.class);
		addTest(new JUnit4TestAdapter(GapContentTest.class));
		addTestSuite(SpaceNormalizerTest.class);
		addTestSuite(TextWrapperTest.class);
		addTestSuite(TestBlockElementBox.class);
		addTestSuite(TestBlocksInInlines.class);
		addTestSuite(TestDocumentTextBox.class);
		addTestSuite(TestStaticTextBox.class);
		addTestSuite(TableLayoutTest.class);
		addTestSuite(ListenerListTest.class);
		addTest(new JUnit4TestAdapter(VexWidgetTest.class));
	}
}
