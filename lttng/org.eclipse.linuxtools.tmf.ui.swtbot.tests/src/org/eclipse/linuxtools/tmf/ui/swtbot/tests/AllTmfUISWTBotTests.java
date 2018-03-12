/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.swtbot.tests;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;

/**
 * SWTBot test suite for tmf.ui
 *
 * @author Matthew Khouzam
 */
public class AllTmfUISWTBotTests extends TestSuite {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite(AllTmfUISWTBotTests.class.getName());
        for (int i = 0; i < 100; i++) {
            suite.addTest(new JUnit4TestAdapter(TracingPerspectiveChecker.class));
            suite.addTest(new JUnit4TestAdapter(TestCustomXmlWizard.class));
            suite.addTest(new JUnit4TestAdapter(TestCustomTxtWizard.class));
        }
        return suite;
    }
}
