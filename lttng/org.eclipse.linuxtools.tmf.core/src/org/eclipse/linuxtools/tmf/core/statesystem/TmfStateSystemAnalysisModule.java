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

package org.eclipse.linuxtools.tmf.core.statesystem;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;

/**
 * Abstract analysis module to generate state system. The executeAnalysis method
 * builds by default a full history, saved on disk. For state system not on
 * disk, sub-classes will need to override the executeAnalysis method
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public abstract class TmfStateSystemAnalysisModule extends TmfAbstractAnalysisModule {

    private ITmfStateSystem fStateSystem = null;
    private static final String EXTENSION = ".ht"; //$NON-NLS-1$

    /**
     * State system backend types
     *
     * @author Geneviève Bastien
     */
    protected enum StateSystemBackend {
        /** Full history in file */
        FULL,
        /** In memory state system */
        INMEM,
        /** Null history */
        NULL,
        /** State system backed with partial history */
        PARTIAL
    }

    /**
     * Get the state provider for this analysis module
     *
     * @return the state provider
     */
    protected abstract ITmfStateProvider createStateProvider();

    /**
     * Get the supplementary file name where to save this state system. The
     * default is the ID of the analysis followed by the extension.
     *
     * @return The supplementary file name
     */
    protected String getSsFileName() {
        return getId() + EXTENSION;
    }

    /**
     * Get the state system generated by this analysis
     *
     * @return The state system
     */
    public ITmfStateSystem getStateSystem() {
        return fStateSystem;
    }

    /**
     * Get the state system backend used by this module
     *
     * @return The {@link StateSystemBackend}
     */
    protected StateSystemBackend getBackend() {
        return StateSystemBackend.NULL;
    }

    @Override
    protected boolean executeAnalysis(final IProgressMonitor monitor) {

        final ITmfStateProvider htInput = createStateProvider();

        if (htInput == null) {
            return false;
        }

        /* FIXME: State systems should make use of the monitor, to be cancelled */
        try {
            /* Get the state system according to backend */
            StateSystemBackend backend = getBackend();
            switch (backend) {
            case FULL:
            case PARTIAL:
                String directory = TmfTraceManager.getSupplementaryFileDir(getTrace());
                final File htFile = new File(directory + getSsFileName());
                if (backend.equals(StateSystemBackend.FULL)) {
                    fStateSystem = TmfStateSystemFactory.newFullHistory(htFile, htInput, true);
                } else {
                    fStateSystem = TmfStateSystemFactory.newPartialHistory(htFile, htInput, true);
                }
                break;
            case INMEM:
                fStateSystem = TmfStateSystemFactory.newInMemHistory(htInput, true);
                break;
            case NULL:
                fStateSystem = TmfStateSystemFactory.newNullHistory(htInput);
                break;
            default:
                break;
            }
        } catch (TmfTraceException e) {
            return false;
        }
        return true;
    }

    @Override
    protected void canceling() {
        /*
         * FIXME: I guess that will do to cancel the state system building, but
         * it may be preferable to just tell the state system and he will handle
         * himself how to cancel its work
         */
        fStateSystem.dispose();
    }
}
