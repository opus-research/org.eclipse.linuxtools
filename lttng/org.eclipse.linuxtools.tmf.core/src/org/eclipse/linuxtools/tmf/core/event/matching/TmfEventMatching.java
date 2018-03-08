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

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Abstract class to extend to match certain type of events in a trace
 *
 * @author gbastien
 *
 *         TODO The matching is not hooked yet to anything in TMF, maybe a
 *         signal handler will be necessary, maybe something else?
 * @since 2.0
 */
public abstract class TmfEventMatching implements ITmfEventMatching {

    /**
     * The array of traces to match
     */
    protected ITmfTrace[] fTraces;

    /**
     * The class to call once a match is found
     */
    protected TmfEventMatches fMatches;

    /**
     * Map for unmatches events
     */
    protected List<Map<String, ITmfEvent>> fUnmatched;

    /**
     * Constructor with one trace
     *
     * @param trace
     *            The trace in which to match events
     *
     */
    public TmfEventMatching(ITmfTrace trace) {
        this(trace, new TmfEventMatches());
    }

    /**
     * Constructor with multiple traces
     *
     * @param traces
     *            The set of traces for which to match events
     */
    public TmfEventMatching(ITmfTrace[] traces) {
        this(traces, new TmfEventMatches());
    }

    /**
     * Constructor with multiple traces and a match processing object
     *
     * @param traces
     *            The set of traces for which to match events
     * @param tmfEventMatches
     *            The match processing class
     */
    public TmfEventMatching(ITmfTrace[] traces, TmfEventMatches tmfEventMatches) {
        fTraces = traces;
        fMatches = tmfEventMatches;
    }

    /**
     * Constructor with one trace and a match processing object
     *
     * @param trace
     *            The trace in which to match events
     * @param tmfEventMatches
     *            The match processing class
     */
    public TmfEventMatching(ITmfTrace trace, TmfEventMatches tmfEventMatches) {
        ITmfTrace[] traces = new ITmfTrace[1];
        traces[0] = trace;
        fTraces = traces;
        fMatches = tmfEventMatches;
    }

    /**
     * Method that initializes any data structure for the event matching
     */
    @Override
    public void initMatching() {
        /* Initialize the matching infrastructure (unmatched event lists) */
        fUnmatched = new ArrayList<Map<String, ITmfEvent>>();
        for (int i = 0; i < fTraces.length; i++) {
            fUnmatched.add(new HashMap<String, ITmfEvent>());
        }
    }

    /**
     * TODO Was in lttv, necessary here too?
     */
    @Override
    public void destroyMatching() {
        // TODO Auto-generated method stub

    }

    /**
     * TODO Was in lttv, necessary here too? Maybe to save the matching data
     * somewhere to be reused later?
     */
    @Override
    public void finalizeMatching() {
        // TODO Auto-generated method stub

    }

    /**
     * Prints stats from the matching
     *
     * @return string of statistics
     *
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < fTraces.length; i++) {
            b.append("Trace " + i + ": " + fUnmatched.get(i).size() + " unmatched events");
        }
        return b.toString();
    }

    /**
     * Matches one event
     *
     * @param event
     *            The event to match
     * @param traceno
     *            The number of the trace this event belongs to
     *
     * @return A pair of event if a match was found, false otherwise
     */
    protected abstract TmfEventDependency matchEvent(ITmfEvent event, int traceno);

    /**
     * Method that start the process of matching events
     *
     * @return Whether the match was completed correctly or not
     */
    @Override
    public boolean matchEvents() {

        /* Are there traces to match? If no, return false */
        if (!(fTraces.length > 0)) {
            return false;
        }

        // TODO Start a new thread here? maybe
        initMatching();

        /*
         * For each trace, get the events and for each event, call the
         * MatchEvent method
         */
        TmfEventDependency dep;
        for (int i = 0; i < fTraces.length; i++) {
            Iterator<ITmfEvent> it = fTraces[i].iterator();
            while (it.hasNext()) {
                // Does not work because ITmfTrace is not an iterable
                // for (ITmfEvent event : fTraces[i]) {
                dep = matchEvent(it.next(), i);
                /* For each match returned, call fMatches.addMatch */
                if (dep != null) {
                    fMatches.addMatch(dep);
                }
            }
        }
        return true;
    }
}
