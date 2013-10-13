/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Florian Thienel - bug 306639 - remove serializability from StyleSheet
 *                       and dependend classes
 *     Carsten Hiesserich - bug 408501 - keep whitespace when copying fragments
 *     						into pre elements
 *******************************************************************************/
package org.eclipse.vex.core.tests;

import org.eclipse.vex.core.internal.core.AfterNIteratorTest;
import org.eclipse.vex.core.internal.core.FilterIteratorTest;
import org.eclipse.vex.core.internal.core.FirstNIteratorTest;
import org.eclipse.vex.core.internal.core.XmlTest;
import org.eclipse.vex.core.internal.css.BatikBehaviorTest;
import org.eclipse.vex.core.internal.css.CssTest;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicyTest;
import org.eclipse.vex.core.internal.css.PropertyTest;
import org.eclipse.vex.core.internal.css.RuleTest;
import org.eclipse.vex.core.internal.dom.AxisTest;
import org.eclipse.vex.core.internal.dom.BasicNodeTest;
import org.eclipse.vex.core.internal.dom.BlockElementBoxTest;
import org.eclipse.vex.core.internal.dom.ContentRangeTest;
import org.eclipse.vex.core.internal.dom.CopyVisitorTest;
import org.eclipse.vex.core.internal.dom.DeepCopyTest;
import org.eclipse.vex.core.internal.dom.DocumentEventTest;
import org.eclipse.vex.core.internal.dom.DocumentFragmentTest;
import org.eclipse.vex.core.internal.dom.DocumentTest;
import org.eclipse.vex.core.internal.dom.GapContentTest;
import org.eclipse.vex.core.internal.dom.L1CommentHandlingTest;
import org.eclipse.vex.core.internal.dom.L1DeletionTests;
import org.eclipse.vex.core.internal.dom.L1ElementHandlingTest;
import org.eclipse.vex.core.internal.dom.L1FragmentHandlingTest;
import org.eclipse.vex.core.internal.dom.L1ProcessingInstructionHandlingTest;
import org.eclipse.vex.core.internal.dom.L1TextHandlingTest;
import org.eclipse.vex.core.internal.dom.NamespaceTest;
import org.eclipse.vex.core.internal.dom.ParentTest;
import org.eclipse.vex.core.internal.io.DocumentContentModelTest;
import org.eclipse.vex.core.internal.io.DocumentReaderTest;
import org.eclipse.vex.core.internal.io.DocumentWriterTest;
import org.eclipse.vex.core.internal.io.NamespaceStackTest;
import org.eclipse.vex.core.internal.io.SpaceNormalizerTest;
import org.eclipse.vex.core.internal.io.TextWrapperTest;
import org.eclipse.vex.core.internal.io.XMLFragmentTest;
import org.eclipse.vex.core.internal.layout.ImageBoxTest;
import org.eclipse.vex.core.internal.layout.LayoutTestSuite;
import org.eclipse.vex.core.internal.layout.TableLayoutTest;
import org.eclipse.vex.core.internal.layout.TestBlockElementBox;
import org.eclipse.vex.core.internal.layout.TestBlocksInInlines;
import org.eclipse.vex.core.internal.layout.TestDocumentTextBox;
import org.eclipse.vex.core.internal.layout.TestStaticTextBox;
import org.eclipse.vex.core.internal.validator.DTDValidatorTest;
import org.eclipse.vex.core.internal.validator.SchemaValidatorTest;
import org.eclipse.vex.core.internal.widget.L2CommentEditingTest;
import org.eclipse.vex.core.internal.widget.L2ProcessingInstructionEditingTest;
import org.eclipse.vex.core.internal.widget.L2SelectionTest;
import org.eclipse.vex.core.internal.widget.L2SimpleEditingTest;
import org.eclipse.vex.core.internal.widget.L2StyleSheetTest;
import org.eclipse.vex.core.internal.widget.L2XmlInsertionTest;
import org.eclipse.vex.core.internal.widget.VexWidgetTest;
import org.eclipse.vex.core.internal.widget.swt.DocumentFragmentTransferTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ FilterIteratorTest.class, FirstNIteratorTest.class, AfterNIteratorTest.class, AxisTest.class, NamespaceStackTest.class, NamespaceTest.class, DocumentReaderTest.class,
		DocumentContentModelTest.class, SchemaValidatorTest.class, CssTest.class, CssWhitespacePolicyTest.class, BatikBehaviorTest.class, ContentRangeTest.class, BasicNodeTest.class,
		ParentTest.class, DocumentTest.class, L1TextHandlingTest.class, L1CommentHandlingTest.class, L1ProcessingInstructionHandlingTest.class, L1ElementHandlingTest.class,
		L1FragmentHandlingTest.class, L1DeletionTests.class, DocumentFragmentTest.class, CopyVisitorTest.class, DeepCopyTest.class, PropertyTest.class, RuleTest.class, BlockElementBoxTest.class,
		ImageBoxTest.class, DocumentWriterTest.class, DTDValidatorTest.class, GapContentTest.class, SpaceNormalizerTest.class, TextWrapperTest.class, TestBlockElementBox.class,
		TestBlocksInInlines.class, TestDocumentTextBox.class, TestStaticTextBox.class, TableLayoutTest.class, LayoutTestSuite.class, ListenerListTest.class, DocumentFragmentTransferTest.class,
		XMLFragmentTest.class, VexWidgetTest.class, L2SimpleEditingTest.class, L2SelectionTest.class, L2CommentEditingTest.class, L2ProcessingInstructionEditingTest.class, L2XmlInsertionTest.class,
		DocumentEventTest.class, L2StyleSheetTest.class, XmlTest.class

})
public class VEXCoreTestSuite {
}
