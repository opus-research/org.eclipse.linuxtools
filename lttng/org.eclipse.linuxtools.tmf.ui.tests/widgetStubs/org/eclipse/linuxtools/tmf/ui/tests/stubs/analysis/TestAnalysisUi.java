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

package org.eclipse.linuxtools.tmf.ui.tests.stubs.analysis;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.analysis.TmfAnalysisViewOutput;

/**
 * Stub for an analysis module implementing the ITmfAnalysisViewable interface
 */
public class TestAnalysisUi extends TmfAbstractAnalysisModule {

    private String fTraceName;

    /**
     * Constructor
     */
    public TestAnalysisUi() {
        super();
        registerOutput(new TmfAnalysisViewOutput("org.eclipse.linuxtools.tmf.ui.tests.testAnalysisView"));
    }

    @Override
    protected boolean executeAnalysis(final IProgressMonitor monitor) {
        return false;
    }

    @Override
    protected void canceling() {

    }

    @Override
    public ITmfTrace getTrace() {
        return super.getTrace();
    }

    /**
     * Returns the name of the trace that should be set
     *
     * @return Name of the trace
     */
    public String getTraceName() {
        return fTraceName;
    }

    @Override
    public void setTraceName(String traceName) {
        fTraceName = traceName;
        super.setTraceName(traceName);
    }

}
