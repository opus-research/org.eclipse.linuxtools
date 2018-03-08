/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng2.core.tests.control.model.impl;

import junit.framework.Test;
import junit.framework.TestSuite;

@SuppressWarnings("javadoc")
public class AllTests {

    public static Test suite() {

        TestSuite suite = new TestSuite(AllTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(BaseEventInfoTest.class);
        suite.addTestSuite(DomainInfoTest.class);
        suite.addTestSuite(EventInfoTest.class);
        suite.addTestSuite(ProbeEventInfoTest.class);
        suite.addTestSuite(ChannelInfoTest.class);
        suite.addTestSuite(SessionInfoTest.class);
        suite.addTestSuite(TraceInfoTest.class);
        suite.addTestSuite(UstProviderInfoTest.class);
        //$JUnit-END$
        return suite;
    }
}
