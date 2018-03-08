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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfAnalysisElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfAnalysisOutputElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfNavigatorContentProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the TmfAnalysisOutputElement class.
 */
public class ProjectModelOutputTest {

    private TmfProjectElement fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        assumeTrue(CtfTmfTestTraces.tracesExist());
        try {
            fixture = ProjectModelTestData.getFilledProject();
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
            ProjectModelTestData.deleteProject(fixture);
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    private TmfTraceElement getTraceElement() {
        TmfTraceElement trace = null;
        for (ITmfProjectModelElement element : fixture.getTracesFolder().getChildren()) {
            if (element instanceof TmfTraceElement) {
                TmfTraceElement traceElement = (TmfTraceElement) element;
                if (traceElement.getName().equals(ProjectModelTestData.getTraceName())) {
                    trace = traceElement;
                }
            }
        }
        assertNotNull(trace);
        return trace;
    }

    private TmfAnalysisElement getTestAnalysisUi() {
        TmfTraceElement trace = getTraceElement();

        /* Make sure the analysis list is not empty */
        List<TmfAnalysisElement> analysisList = trace.getAvailableAnalysis();

        /* Make sure TestAnalysisUi is there */
        TmfAnalysisElement analysis = null;
        for (TmfAnalysisElement analysisElement : analysisList) {
            if (analysisElement.getAnalysisId().equals("org.eclipse.linuxtools.tmf.ui.tests.test")) {
                analysis = analysisElement;
            }
        }
        assertNotNull(analysis);
        return analysis;
    }

    /**
     * Test the getAvailableOutputs() method
     */
    @Test
    public void testListOutputs() {
        TmfAnalysisElement analysis = getTestAnalysisUi();

        /* Make sure the output list is not empty */
        List<TmfAnalysisOutputElement> outputList = analysis.getAvailableOutputs();
        assertFalse(outputList.isEmpty());
    }

    /**
     * Test to see if outputs are correctly populated
     */
    @Test
    public void testPopulate() {
        TmfAnalysisElement analysis = getTestAnalysisUi();

        final TmfNavigatorContentProvider ncp = new TmfNavigatorContentProvider();
        // force the model to be populated
        ncp.getChildren(fixture);

        /* Make sure the output list is not empty */
        List<ITmfProjectModelElement> outputList = analysis.getChildren();
        assertFalse(outputList.isEmpty());

        boolean found = false;
        for (ITmfProjectModelElement element : outputList) {
            if (element instanceof TmfAnalysisOutputElement) {
                TmfAnalysisOutputElement outputElement = (TmfAnalysisOutputElement) element;
                if (outputElement.getName().equals("Test Analysis View")) {
                    found = true;
                }
            }
        }
        assertTrue(found);
    }
}
