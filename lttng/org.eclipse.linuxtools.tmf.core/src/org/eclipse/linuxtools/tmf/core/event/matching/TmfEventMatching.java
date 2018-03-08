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

import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceIterator;

/**
 * Abstract class to extend to match certain type of events in a trace
 *
 * @author gbastien
 *
 * TODO  The matching is not hooked yet to anything in TMF, maybe a signal handler will be necessary, maybe something else?
 * @since 2.0
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
     * Hashtables for unmatches events
     */
    protected Hashtable<String, ITmfEvent>[] fUnmatched;

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
    public void initMatching() {
        // Initialize the matching infrastructure (unmatched event lists)
        fUnmatched = new Hashtable[fTraces.length];
        for (int i = 0; i<fTraces.length; i++) {
            fUnmatched[i] = new Hashtable<String, ITmfEvent>();
        }
    }

    @Override
    public void destroyMatching() {
        // TODO Auto-generated method stub

    }

    @Override
    public void finalizeMatching() {
        // TODO Auto-generated method stub

    }

    @Override
    public String printMatchingStats() {
        String stats = "";
        for (int i = 0; i<fTraces.length; i++) {
            stats += "Trace " + i + ": " + fUnmatched[i].size() + " unmatched events";   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        }
        return stats;
    }

    @Override
    public boolean matchEvents() {

        // Are there traces to match?  If no, return false
        if ( !(fTraces.length > 0)) {
            return false;
        }

        // Start a new thread here? maybe
        initMatching();

        // For each trace, get the events and for each event, call the MatchEvent method
        TmfEventDependency dep;
        for (int i = 0; i < fTraces.length; i++) {
            Iterator<ITmfEvent> it = fTraces[i].iterator();
            while (it.hasNext()) {
                dep = matchEvent(it.next(), i);
                // For each match returned, call fMatches.addMatch
                if (dep != null) {
                    fMatches.addMatch(dep);
                }
            }
        }
        return true;
    }
}
