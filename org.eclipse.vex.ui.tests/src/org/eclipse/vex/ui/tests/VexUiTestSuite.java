/*******************************************************************************
 * Copyright (c) 2009, 2013 Holger Voormann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Holger Voormann - initial API and implementation
 *     Carsten Hiesserich - added OutlineProvider tests
 *******************************************************************************/
package org.eclipse.vex.ui.tests;

import org.eclipse.vex.ui.internal.config.tests.ConfigLoaderJobTest;
import org.eclipse.vex.ui.internal.config.tests.ConfigurationRegistryTest;
import org.eclipse.vex.ui.internal.editor.tests.FindReplaceTargetTest;
import org.eclipse.vex.ui.internal.editor.tests.HandlerUtilTest;
import org.eclipse.vex.ui.internal.namespace.tests.EditNamespacesControllerTest;
import org.eclipse.vex.ui.internal.outline.tests.OutlineFilterTest;
import org.eclipse.vex.ui.internal.outline.tests.OutlineProviderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ConfigLoaderJobTest.class, ConfigurationRegistryTest.class, EditNamespacesControllerTest.class, FindReplaceTargetTest.class, OutlineProviderTest.class, OutlineFilterTest.class,
		HandlerUtilTest.class })
public class VexUiTestSuite {
}
