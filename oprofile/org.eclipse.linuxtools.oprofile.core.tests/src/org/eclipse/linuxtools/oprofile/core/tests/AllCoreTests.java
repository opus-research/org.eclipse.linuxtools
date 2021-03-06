/*******************************************************************************
 * Copyright (c) 2009, 2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.core.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestModelDataParse.class, TestModelDataPreParse.class, TestSessionsParse.class,
		TestCheckEventsParse.class, TestCheckEventsPreParse.class, TestInfoParse.class, TestDataModel.class })
public class AllCoreTests {
}
