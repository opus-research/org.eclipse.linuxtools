/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.alltests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Master test suite for all Linux Tools LTTng UI unit tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    org.eclipse.linuxtools.gdbtrace.ui.tests.AllGdbTraceUITests.class,
    org.eclipse.linuxtools.lttng2.control.ui.tests.AllTests.class,
    org.eclipse.linuxtools.lttng2.kernel.ui.tests.AllTests.class,
    org.eclipse.linuxtools.lttng2.ust.ui.tests.AllTests.class,
    org.eclipse.linuxtools.tmf.ui.tests.AllTmfUITests.class,
})

public class RunAllUiTests {

}
