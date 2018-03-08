/*******************************************************************************
* Copyright (c) 2012 Red Hat, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Red Hat, Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	LaunchTabsTest.class,ModelTest.class,DataManipulatorTest.class,SaveSessionTest.class,
	StatsComparisonTest.class,LaunchTest.class, LaunchRemoteTest.class, FindActionTest.class
})

public class AllPerfTests {
}
