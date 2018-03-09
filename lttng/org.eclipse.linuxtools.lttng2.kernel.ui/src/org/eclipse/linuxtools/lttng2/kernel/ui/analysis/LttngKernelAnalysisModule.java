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

package org.eclipse.linuxtools.lttng2.kernel.ui.analysis;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LttngKernelStateProvider;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.controlflow.ControlFlowView;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources.ResourcesView;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.analysis.TmfAnalysisViewOutput;

/**
 * State System Module for lttng kernel traces
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class LttngKernelAnalysisModule extends TmfStateSystemAnalysisModule {

    /**
     * The file name of the History Tree
     */
    public final static String HISTORY_TREE_FILE_NAME = "stateHistory.ht"; //$NON-NLS-1$

    /** The ID of this analysis module */
    public final static String ID = "org.eclipse.linuxtools.lttng2.kernel.analysis"; //$NON-NLS-1$

    /**
     * Constructor adding the views to the analysis
     */
    public LttngKernelAnalysisModule() {
        super();
        this.registerOutput(new TmfAnalysisViewOutput(ControlFlowView.ID));
        this.registerOutput(new TmfAnalysisViewOutput(ResourcesView.ID));
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        ITmfTrace trace = getTrace();
        if (!(trace instanceof LttngKernelTrace)) {
            throw new IllegalStateException("LttngKernelStateSystemModule: trace should be of type LttngKernelTrace"); //$NON-NLS-1$
        }
        return new LttngKernelStateProvider((LttngKernelTrace) trace);
    }

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.FULL;
    }

    @Override
    protected String getSsFileName() {
        return HISTORY_TREE_FILE_NAME;
    }

    @Override
    protected String getFullHelpText() {
        return Messages.LttngKernelAnalysis_Help;
    }

}
