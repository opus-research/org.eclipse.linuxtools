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

package org.eclipse.linuxtools.tmf.tests.stubs.analysis;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.osgi.framework.Bundle;

/**
 * Simple analysis type for test
 */
public class TestCtfAnalysis extends TmfAbstractAnalysisModule{

    @Override
    public boolean appliesToTraceType(Class<? extends ITmfTrace> trace) {
        return CtfTmfTrace.class.isAssignableFrom(trace);
    }

    @Override
    public boolean canExecute(ITmfTrace trace) {
        return true;
    }

    @Override
    protected void canceling() {

    }

    @Override
    protected boolean executeAnalysis(final IProgressMonitor monitor) {
        return false;
    }

    @Override
    public Bundle getBundle() {
        return Platform.getBundle("org.eclipse.linuxtools.tmf.core.tests");
    }
}
