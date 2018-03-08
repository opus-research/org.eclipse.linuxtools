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

package org.eclipse.linuxtools.lttng2.kernel.core.analysis;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LttngKernelStateProvider;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;

/**
 * State System Module for lttng kernel traces
 *
 * @author Geneviève Bastien
 */
public class LttngKernelStateSystemModule extends TmfStateSystemAnalysisModule {

    /**
     * The file name of the History Tree
     */
    public final static String HISTORY_TREE_FILE_NAME = "stateHistory.ht"; //$NON-NLS-1$

    /** The ID of this analysis module */
    public final static String ID = "org.eclipse.linuxtools.lttng2.kernel"; //$NON-NLS-1$

    @Override
    protected ITmfStateProvider getStateProvider() {
        if (!(getTrace() instanceof CtfTmfTrace)) {
            throw new RuntimeException("LttngKernelStateSystemModule: trace should be of type CtfTmfTrace"); //$NON-NLS-1$
        }
        return new LttngKernelStateProvider((CtfTmfTrace) getTrace());
    }

    @Override
    protected String getSsFileName() {
        return HISTORY_TREE_FILE_NAME;
    }

}
