/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.statistics.ITmfStatistics;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStateStatistics;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.analysis.TmfAnalysisViewOutput;

/**
 * Analysis module to compute the statistics of a trace.
 *
 * @author Alexandre Montplaisir
 */
public class TmfStatisticsModule extends TmfAbstractAnalysisModule
        implements ITmfStateSystemAnalysisModule {

    /** ID of this analysis module */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.statistics.analysis"; //$NON-NLS-1$

    /** Map of the statistics' state systems */
    private final Map<String, ITmfStateSystem> fStateSystems = new HashMap<String, ITmfStateSystem>();

    /** The trace's statistics */
    private ITmfStatistics fStatistics = null;

    /**
     * Constructor
     */
    public TmfStatisticsModule() {
        super();
        this.registerOutput(new TmfAnalysisViewOutput(TmfStatisticsView.ID));
    }

    /**
     * Get the statistics object built by this analysis
     *
     * @return The ITmfStatistics object
     */
    public ITmfStatistics getStatistics() {
        return fStatistics;
    }

    // ------------------------------------------------------------------------
    // TmfAbstractAnalysisModule
    // ------------------------------------------------------------------------

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) throws TmfAnalysisException {
        /*
         * Initialize the statistics provider, but only if a Resource has been
         * set (so we don't build it for experiments, for unit tests, etc.)
         */
        ITmfTrace trace = getTrace();
        try {
            /*
             * FIXME We hard-code the StateStatistics type here, but it could
             * be configurable via an analysis parameter.
             */
            fStatistics = (trace.getResource() == null ? null : new TmfStateStatistics(trace));
        } catch (TmfTraceException e) {
            return false;
        }

        /* Fill the state system map (if relevant) */
        if (fStatistics instanceof TmfStateStatistics) {
            TmfStateStatistics stats = (TmfStateStatistics) fStatistics;
            fStateSystems.put(TmfStateStatistics.TOTALS_STATE_ID, stats.getTotalsSS());
            fStateSystems.put(TmfStateStatistics.TYPES_STATE_ID, stats.getEventTypesSS());
        }

        return true;
    }

    @Override
    protected void canceling() {
        /*
         * FIXME The "right" way to cancel state system construction is not
         * available yet...
         */
        ITmfStatistics stats = fStatistics;
        if (stats != null) {
            stats.dispose();
        }
    }

    // ------------------------------------------------------------------------
    // ITmfStateSystemAnalysisModule
    // ------------------------------------------------------------------------

    @Override
    public Map<String, ITmfStateSystem> getStateSystems() {
        return Collections.unmodifiableMap(fStateSystems);
    }

}
