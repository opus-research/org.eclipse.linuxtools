/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event.matching;

import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * Abstract class to extend to match certain type of events in a trace
 *
 * @author gbastien
 *
 * TODO  The matching is not hooked yet to anything in TMF, maybe a signal handler will be necessary, maybe something else?
 */
public abstract class TmfEventMatching implements ITmfEventMatching {

    /**
     *
     */
    protected TmfTrace[] fTraces;

    /**
     * The class to call once a match is found
     */
    protected TmfEventMatches fMatches;

    /**
     * @param trace The trace in which to match events
     *
     */
    public TmfEventMatching(TmfTrace trace) {
        this(trace, new TmfEventMatches());
    }

    /**
     * @param traces The set of traces for which to match events
     */
    public TmfEventMatching(TmfTrace[] traces) {
        this(traces, new TmfEventMatches());
    }

    /**
     * @param traces The set of traces for which to match events
     * @param tmfEventMatches The match processing class
     */
    public TmfEventMatching(TmfTrace[] traces, TmfEventMatches tmfEventMatches) {
        fTraces = traces;
        fMatches = tmfEventMatches;
    }

    /**
     * @param trace The trace in which to match events
     * @param tmfEventMatches The match processing class
     */
    public TmfEventMatching(TmfTrace trace, TmfEventMatches tmfEventMatches) {
        TmfTrace[] traces = new TmfTrace[1];
        traces[0] = trace;
        fTraces = traces;
        fMatches = tmfEventMatches;
    }

    @Override
    public void DestroyMatching() {
        // TODO Auto-generated method stub

    }

    @Override
    public void FinalizeMatching() {
        // TODO Auto-generated method stub

    }

    @Override
    public void PrintMatchingStats() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean MatchEvents() {
        // Are there traces to match?  If no, return false

        // Start a new thread here? maybe
        // Initialize the matching infrastructure (unmatched event lists)
        // For each trace, get the events and for each event, call the MatchEvent method
        // For each match returned, call fMatches.addMatch

        return true;
    }
}
