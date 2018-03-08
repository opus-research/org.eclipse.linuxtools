/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.event.matchandsync;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.Activator;

/**
 * Test suite for org.eclipse.linuxtools.tmf.core.event
 */
@SuppressWarnings({ "nls" })
public class AllTests {

    /**
     * @return the CTF COre Event test suite
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite("Test suite for " + Activator.PLUGIN_ID + ".event"); //$NON-NLS-1$;
        //$JUnit-BEGIN$
        suite.addTestSuite(MatchAndSyncTest.class);
        //$JUnit-END$
        return suite;
    }

}
