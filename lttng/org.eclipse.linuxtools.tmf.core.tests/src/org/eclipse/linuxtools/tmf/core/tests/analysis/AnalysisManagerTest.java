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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestAnalysis;
import org.junit.Test;

/**
 * Test suite for the TmfAnalysisModule class
 */
public class AnalysisManagerTest {

    /** Id of analysis module with parameter */
    public static final String MODULE_PARAM = "org.eclipse.linuxtools.tmf.core.tests.analysis.test";
    /** ID of analysis module with parameter and default value */
    public static final String MODULE_PARAM_DEFAULT = "org.eclipse.linuxtools.tmf.core.tests.analysis.test2";
    /** ID of analysis module for CTF traces only */
    public static final String MODULE_CTF = "org.eclipse.linuxtools.tmf.core.tests.analysis.testctf";

    /**
     * Test suite for the getAnalaysisModule method
     */
    @Test
    public void testGetAnalysisModules() {
        Map<String, IAnalysisModule> modules = TmfAnalysisManager.getAnalysisModules();
        /* At least 3 modules should be found */
        assertTrue(modules.size() >= 3);

        IAnalysisModule module = modules.get(MODULE_PARAM_DEFAULT);
        assertTrue(module.isAutomatic());

        module = modules.get(MODULE_PARAM);
        assertFalse(module.isAutomatic());
    }

    /**
     * Test suite for getAnalysisModule(ITmfTrace) Use the test TMF trace and
     * test Ctf trace as sample traces
     */
    @Test
    public void testListForTraces() {
        /* Generic TmfTrace */
        ITmfTrace trace = TmfTestTrace.A_TEST_10K.getTrace();
        Map<String, IAnalysisModule> map = TmfAnalysisManager.getAnalysisModules(trace);

        assertTrue(map.containsKey(MODULE_PARAM));
        assertTrue(map.containsKey(MODULE_PARAM_DEFAULT));
        assertFalse(map.containsKey(MODULE_CTF));

        /* Ctf trace */
        CtfTmfTrace ctftrace = CtfTmfTestTrace.KERNEL.getTrace();

        map = TmfAnalysisManager.getAnalysisModules(ctftrace);

        assertTrue(map.containsKey(MODULE_PARAM));
        assertTrue(map.containsKey(MODULE_PARAM_DEFAULT));
        assertTrue(map.containsKey(MODULE_CTF));
    }

    /**
     * Test for the initialization of parameters from the extension points
     */
    @Test
    public void testParameters() {
        /* This analysis has a parameter, but no default value. we should be able to set the parameter */
        IAnalysisModule module = TmfAnalysisManager.getAnalysisModule(MODULE_PARAM);
        assertNull(module.getParameter(TestAnalysis.PARAM_TEST));
        module.setParameter(TestAnalysis.PARAM_TEST, 1);
        assertEquals(1, module.getParameter(TestAnalysis.PARAM_TEST));

        /* This module has a parameter with default value */
        module = TmfAnalysisManager.getAnalysisModule(MODULE_PARAM_DEFAULT);
        assertEquals(3, module.getParameter(TestAnalysis.PARAM_TEST));
        module.setParameter(TestAnalysis.PARAM_TEST, 1);
        assertEquals(1, module.getParameter(TestAnalysis.PARAM_TEST));

        /* This module does not have a parameter so setting it should throw an error */
        module = TmfAnalysisManager.getAnalysisModule(MODULE_CTF);
        assertNull(module.getParameter(TestAnalysis.PARAM_TEST));
        Exception exception = null;
        try {
            module.setParameter(TestAnalysis.PARAM_TEST, 1);
        } catch (RuntimeException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    /**
     * Test suite for the getAnalysisModule(ITmfTrace, String) method
     */
    @Test
    public void testGetAnalysisModule() {
        IAnalysisModule module = TmfAnalysisManager.getAnalysisModule(TmfTestTrace.A_TEST_10K.getTrace(), MODULE_CTF);
        assertNull(module);

        module = TmfAnalysisManager.getAnalysisModule(CtfTmfTestTrace.KERNEL.getTrace(), MODULE_CTF);
        assertNotNull(module);
    }
}
