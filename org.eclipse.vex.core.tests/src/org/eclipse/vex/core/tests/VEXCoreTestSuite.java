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

import org.eclipse.vex.core.internal.core.AfterNIteratorTest;
import org.eclipse.vex.core.internal.core.FilterIteratorTest;
import org.eclipse.vex.core.internal.core.FirstNIteratorTest;
import org.eclipse.vex.core.internal.css.BatikBehaviorTest;
import org.eclipse.vex.core.internal.css.CssTest;
import org.eclipse.vex.core.internal.css.PropertyTest;
import org.eclipse.vex.core.internal.css.RuleTest;
import org.eclipse.vex.core.internal.dom.AxisTest;
import org.eclipse.vex.core.internal.dom.BasicNodeTest;
import org.eclipse.vex.core.internal.dom.BlockElementBoxTest;
import org.eclipse.vex.core.internal.dom.CopyVisitorTest;
import org.eclipse.vex.core.internal.dom.DTDValidatorTest;
import org.eclipse.vex.core.internal.dom.DeepCopyTest;
import org.eclipse.vex.core.internal.dom.DocumentContentModelTest;
import org.eclipse.vex.core.internal.dom.DocumentFragmentTest;
import org.eclipse.vex.core.internal.dom.DocumentTest;
import org.eclipse.vex.core.internal.dom.GapContentTest;
import org.eclipse.vex.core.internal.dom.L1CommentHandlingTest;
import org.eclipse.vex.core.internal.dom.L1ElementHandlingTest;
import org.eclipse.vex.core.internal.dom.L1FragmentHandlingTest;
import org.eclipse.vex.core.internal.dom.L1TextHandlingTest;
import org.eclipse.vex.core.internal.dom.NamespaceTest;
import org.eclipse.vex.core.internal.dom.ParentTest;
import org.eclipse.vex.core.internal.dom.RangeTest;
import org.eclipse.vex.core.internal.dom.SchemaValidatorTest;
import org.eclipse.vex.core.internal.io.DocumentReaderTest;
import org.eclipse.vex.core.internal.io.DocumentWriterTest;
import org.eclipse.vex.core.internal.io.NamespaceStackTest;
import org.eclipse.vex.core.internal.io.SpaceNormalizerTest;
import org.eclipse.vex.core.internal.io.TextWrapperTest;
import org.eclipse.vex.core.internal.layout.ImageBoxTest;
import org.eclipse.vex.core.internal.layout.LayoutTestSuite;
import org.eclipse.vex.core.internal.layout.TableLayoutTest;
import org.eclipse.vex.core.internal.layout.TestBlockElementBox;
import org.eclipse.vex.core.internal.layout.TestBlocksInInlines;
import org.eclipse.vex.core.internal.layout.TestDocumentTextBox;
import org.eclipse.vex.core.internal.layout.TestStaticTextBox;
import org.eclipse.vex.core.internal.widget.L2CommentEditingTest;
import org.eclipse.vex.core.internal.widget.L2SelectionTest;
import org.eclipse.vex.core.internal.widget.L2SimpleEditingTest;
import org.eclipse.vex.core.internal.widget.VexWidgetTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ FilterIteratorTest.class, FirstNIteratorTest.class, AfterNIteratorTest.class, AxisTest.class, NamespaceStackTest.class, NamespaceTest.class, DocumentReaderTest.class,
		DocumentContentModelTest.class, SchemaValidatorTest.class, CssTest.class, BatikBehaviorTest.class, RangeTest.class, BasicNodeTest.class, ParentTest.class, DocumentTest.class,
		L1TextHandlingTest.class, L1CommentHandlingTest.class, L1ElementHandlingTest.class, L1FragmentHandlingTest.class, DocumentFragmentTest.class, CopyVisitorTest.class, DeepCopyTest.class,
		PropertyTest.class, RuleTest.class, BlockElementBoxTest.class, ImageBoxTest.class, DocumentWriterTest.class, DTDValidatorTest.class, GapContentTest.class, SpaceNormalizerTest.class,
		TextWrapperTest.class, TestBlockElementBox.class, TestBlocksInInlines.class, TestDocumentTextBox.class, TestStaticTextBox.class, TableLayoutTest.class, LayoutTestSuite.class,
		ListenerListTest.class, VexWidgetTest.class, L2SimpleEditingTest.class, L2SelectionTest.class, L2CommentEditingTest.class

})
public class VEXCoreTestSuite {
}
