/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.ust.core.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runner for the lttng2.kernel unit tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ActivatorTest.class,
    org.eclipse.linuxtools.lttng2.ust.core.tests.trace.callstack.AllTests.class
})
public class AllTests { }
