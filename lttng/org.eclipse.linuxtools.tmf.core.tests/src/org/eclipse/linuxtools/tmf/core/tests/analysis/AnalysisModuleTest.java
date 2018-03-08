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

package org.eclipse.linuxtools.tmf.core.tests.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.Test;

/**
 * Test suite for the TmfAnalysisModule class
 */
public class AnalysisModuleTest {

    private static String MODULE_ID = "test.id";
    private static String MODULE_NAME = "Test analysis";

    /**
     * Test suite for analysis module getters and setters
     */
    @Test
    public void testGettersSetters() {
        IAnalysisModule module = new TestAnalysis();

        module.setName(MODULE_NAME);
        module.setId(MODULE_ID);
        assertEquals(MODULE_ID, module.getId());
        assertEquals(MODULE_NAME, module.getName());

        module.setAutomatic(false);
        assertFalse(module.isAutomatic());
        module.setAutomatic(true);
        assertTrue(module.isAutomatic());
        module.addParameter(TestAnalysis.PARAM_TEST);
        assertNull(module.getParameter(TestAnalysis.PARAM_TEST));
        module.setParameter(TestAnalysis.PARAM_TEST, 1);
        assertEquals(1, module.getParameter(TestAnalysis.PARAM_TEST));
    }

    private static TestAnalysis setUpAnalysis() {
        TestAnalysis module = new TestAnalysis();

        module.setName(MODULE_NAME);
        module.setId(MODULE_ID);
        module.addParameter(TestAnalysis.PARAM_TEST);

        return module;

    }

    /**
     * Test suite for analysis module waitForCompletion with successful
     * execution
     */
    @Test
    public void testWaitForCompletionSuccess() {
        TestAnalysis module = setUpAnalysis();

        IStatus status = module.schedule();
        assertEquals(IStatus.ERROR, status.getSeverity());

        /* Set a stub trace for analysis */
        TmfTraceStub trace = new TmfTraceStub();
        trace.init("t1");
        try {
            module.setTrace(trace);
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }

        /* Default execution, with output 1 */
        status = module.schedule();
        assertEquals(Status.OK_STATUS, status);
        boolean completed = module.waitForCompletion();
        assertTrue(completed);
        assertEquals(1, module.getAnalysisOutput());
    }

    /**
     * Test suite for analysis module waitForCompletion with cancellation
     */
    @Test
    public void testWaitForCompletionCancelled() {
        TestAnalysis module = setUpAnalysis();

        /* Set a stub trace for analysis */
        TmfTraceStub trace = new TmfTraceStub();
        trace.init("t1");
        try {
            module.setTrace(trace);
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }

        module.setParameter(TestAnalysis.PARAM_TEST, 1);
        IStatus status = module.schedule();
        assertEquals(Status.OK_STATUS, status);
        boolean completed = module.waitForCompletion();
        assertFalse(completed);
        assertEquals(0, module.getAnalysisOutput());
    }

}
