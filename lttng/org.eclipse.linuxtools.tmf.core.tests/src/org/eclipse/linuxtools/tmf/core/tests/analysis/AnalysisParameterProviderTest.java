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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestAnalysisParameterProvider;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the TmfAbstractParameterProvider class
 *
 * @author Geneviève Bastien
 */
public class AnalysisParameterProviderTest {

    /**
     * Registers the parameter provider
     */
    @Before
    public void setup() {
        TmfAnalysisManager.registerParameterProvider(AnalysisManagerTest.MODULE_PARAM, TestAnalysisParameterProvider.class);
        /* Make sure the value is set to null */
        IAnalysisModule module = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_PARAM);
        module.setParameter(TestAnalysis.PARAM_TEST, null);
    }

    /**
     * Test that no provider returns values with no trace
     */
    @Test
    public void testProviderNoTrace() {
        IAnalysisModule module = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_PARAM);
        assertNull(module.getParameter(TestAnalysis.PARAM_TEST));
    }

    /**
     * Test that no provider returns values with a trace that is not ctf trace
     */
    @Test
    public void testProviderTmfTrace() {
        IAnalysisModule module = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_PARAM);
        try {
            module.setTrace(TmfTestTrace.A_TEST_10K.getTrace());
        } catch (TmfAnalysisException e) {
            fail("Cannot set generic trace for analysis TestAnalysis");
        }
        assertNull(module.getParameter(TestAnalysis.PARAM_TEST));
    }

    /**
     * Test that provider returns value with a CtfTrace
     */
    @Test
    public void testProviderCtfTrace() {
        IAnalysisModule module = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_PARAM);
        CtfTmfTrace ctfTrace = CtfTmfTestTrace.KERNEL.getTrace();
        try {
            module.setTrace(ctfTrace);
        } catch (TmfAnalysisException e1) {
            fail("Cannot set ctf trace for analysis TestAnalysis");
        }
        assertEquals(10, module.getParameter(TestAnalysis.PARAM_TEST));

        /* Check that changing to another trace removes the provider */
        try {
            module.setTrace(TmfTestTrace.A_TEST_10K.getTrace());
        } catch (TmfAnalysisException e) {
            fail("Cannot set generic trace for analysis TestAnalysis");
        }
        assertNull(module.getParameter(TestAnalysis.PARAM_TEST));
    }
}
