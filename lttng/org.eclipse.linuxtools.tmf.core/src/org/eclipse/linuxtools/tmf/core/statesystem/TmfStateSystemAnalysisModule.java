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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;

/**
 * Abstract analysis module to generate a state system. It is a base class that
 * can be used as a shortcut by analysis who just need to build a single state
 * system with a state provider.
 *
 * Analysis implementing this class should only need to provide a state system
 * and optionally a backend (default to NULL) and, if required, a filename
 * (defaults to the analysis'ID)
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public abstract class TmfStateSystemAnalysisModule extends TmfAbstractAnalysisModule
        implements ITmfStateSystemAnalysisModule {

    private ITmfStateSystem fStateSystem = null;
    private ITmfHistoryBuilder fBuilder = null;
    private static final String EXTENSION = ".ht"; //$NON-NLS-1$

    /**
     * State system backend types
     *
     * @author Geneviève Bastien
     */
    protected enum StateSystemBackendType {
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
    @NonNull
    protected abstract ITmfStateProvider createStateProvider();

    /**
     * Get the state system backend type used by this module
     *
     * @return The {@link StateSystemBackendType}
     */
    protected abstract StateSystemBackendType getBackendType();

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

    @Override
    protected boolean executeAnalysis(final IProgressMonitor monitor) {

        final ITmfStateProvider htInput = createStateProvider();

        /* FIXME: State systems should make use of the monitor, to be cancelled */
        try {
            /* Get the state system according to backend */
            StateSystemBackendType backend = getBackendType();
            String directory;
            File htFile;
            ITmfHistoryBuilder builder = null;
            switch (backend) {
            case FULL:
                directory = TmfTraceManager.getSupplementaryFileDir(getTrace());
                htFile = new File(directory + getSsFileName());
                builder = TmfStateSystemFactory.newFullHistory(htFile, htInput);
                break;
            case PARTIAL:
                directory = TmfTraceManager.getSupplementaryFileDir(getTrace());
                htFile = new File(directory + getSsFileName());
                builder = TmfStateSystemFactory.newPartialHistory(htFile, htInput);
                break;
            case INMEM:
                builder = TmfStateSystemFactory.newInMemHistory(htInput);
                break;
            case NULL:
                builder = TmfStateSystemFactory.newNullHistory(htInput);
                break;
            default:
                break;
            }
            if (builder != null) {
                synchronized (TmfStateSystemAnalysisModule.this) {
                    fBuilder = builder;
                    builder.setNotifyRequestPendingNeeded(true);
                }

                builder.build();
                fStateSystem = builder.getStateSystem();
                setNotifyPendingReqNeeded(builder.isNotifyPendingRequestNeeded());
            }

        } catch (TmfTraceException e) {
            return false;
        }
        return true;
    }

    @Override
    protected synchronized void canceling() {
        if (fBuilder != null) {
            fBuilder.cancel();
        }
    }

    @Override
    public Map<String, ITmfStateSystem> getStateSystems() {
        Map<String, ITmfStateSystem> map = new HashMap<String, ITmfStateSystem>();
        map.put(getId(), fStateSystem);
        return map;
    }

}
