/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.project.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the TmfTraceElement class.
 */
public class ProjectModelTraceTest {

    private TmfProjectElement fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        assumeTrue(CtfTmfTestTraces.tracesExist());
        try {
            fixture = ProjectModelTestObjects.getFilledProject();
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Cleans up the project after tests have been executed
     */
    @After
    public void cleanUp() {
        try {
            ProjectModelTestObjects.deleteProject(fixture);
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test the getTrace() and trace opening
     */
    @Test
    public void testOpenTrace() {
        assertNotNull(fixture);

        final TmfTraceElement traceElement = fixture.getTracesFolder().getTraces().get(0);

        /*
         * Get the trace from the element, from main thread should throw an
         * exception
         */
        try {
            traceElement.getTrace();
            fail("There should be an exception because of thread access");
        } catch (RuntimeException e) {
            assertTrue(true);
        }
        Thread thread = new Thread() {
            @Override
            public void run() {
                /*
                 * This will be blocked because the main thread is unavailable,
                 * but at least it should return
                 */
                ITmfTrace trace = traceElement.getTrace();
                assertNull(trace);
            }
        };
        thread.start();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }

        /*
         * TODO the open trace needs the display (main) thread which is occupied
         * by this code! Can't do much else :(
         */
        // /* Open the trace from project, then get from element, both should be
        // the exact same element as the active trace */
        // TmfOpenTraceHelper.openTraceFromElement(traceElement);
        //
        // ITmfTrace trace2 = TmfTraceManager.getInstance().getActiveTrace();
        // assertFalse(trace2 == trace);
        // trace = traceElement.getTrace();
        // assertTrue(trace2 == trace);
    }
}
