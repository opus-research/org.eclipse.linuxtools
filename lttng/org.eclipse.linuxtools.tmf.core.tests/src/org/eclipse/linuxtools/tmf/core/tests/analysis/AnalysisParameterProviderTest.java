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
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
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
    }

    /**
     * Test that the provider's value is used
     */
    @Test
    public void testProviderTmfTrace() {
        ITmfTrace trace = TmfTestTrace.A_TEST_10K.getTrace();
        /* Make sure the value is set to null */
        TmfAnalysisModuleHelper helper = TmfAnalysisManager.getAnalysisModule(AnalysisManagerTest.MODULE_PARAM);
        IAnalysisModule module;
        try {
            module = helper.newModule(trace);
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
            return;
        }
        assertEquals(10, module.getParameter(TestAnalysis.PARAM_TEST));
    }

}
