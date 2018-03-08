/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adjusted for new Trace Model
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for org.eclipse.linuxtools.tmf.core.trace
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TmfCheckpointIndexTest.class,
    TmfCheckpointIndexTest2.class,
    TmfCheckpointTest.class,
    TmfContextTest.class,
    TmfExperimentCheckpointIndexTest.class,
    TmfExperimentTest.class,
    TmfLocationTest.class,
    TmfMultiTraceExperimentTest.class,
    TmfTraceTest.class
})
public class AllTests {

}