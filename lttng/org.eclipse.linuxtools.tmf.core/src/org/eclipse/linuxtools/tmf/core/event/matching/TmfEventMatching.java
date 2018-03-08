/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event.matching;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Abstract class to extend to match certain type of events in a trace
 *
 * @author gbastien
 * @since 2.0
 */
public abstract class TmfEventMatching implements ITmfEventMatching {

    /**
     * The matching type
     *
     * FIXME Not the best place to put this. Have an array of match types as a
     * parameter of each trace?
     */
    public enum MatchingType {
        /**
         * NETWORK, match network events
         */
        NETWORK
    }

    /**
     * The array of traces to match
     */
    protected ITmfTrace[] fTraces;

    /**
     * The class to call once a match is found
     */
    protected IMatchProcessingUnit fMatches;

    /**
     * Default constructor
     */
    public TmfEventMatching() {
        this(new ITmfTrace[0], new TmfEventMatches());
    }

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
    public TmfEventMatching(ITmfTrace[] traces, IMatchProcessingUnit tmfEventMatches) {
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
    public TmfEventMatching(ITmfTrace trace, IMatchProcessingUnit tmfEventMatches) {
        ITmfTrace[] traces = new ITmfTrace[1];
        traces[0] = trace;
        fTraces = traces;
        fMatches = tmfEventMatches;
    }

    /**
     * Public setter to initialize the traces
     *
     * @param trace
     *            one trace
     */
    @Override
    public void setTraces(ITmfTrace trace) {
        ITmfTrace[] traces = new ITmfTrace[1];
        traces[0] = trace;
        setTraces(traces);
    }

    /**
     * Public setter to initialize the traces
     *
     * @param traces
     *            The array of traces
     */
    @Override
    public void setTraces(ITmfTrace[] traces) {
        fTraces = traces;

    }

    /**
     * Public setter to set the match processing unit
     *
     * @param tmfEventMatches
     *            the processing unit object
     */
    @Override
    public void setProcessingUnit(IMatchProcessingUnit tmfEventMatches) {
        fMatches = tmfEventMatches;
    }

    /**
     * Method that initializes any data structure for the event matching
     */
    protected void initMatching() {
        if (fMatches != null) {
            fMatches.init(fTraces);
        }
    }

    /**
     * Calls any post matching methods of the processing class
     */
    protected void finalizeMatching() {
        fMatches.matchingEnded();
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
        return "TmfEventMatching [ " + fMatches + " ]";
    }

    /**
     * Matches one event
     *
     * @param event
     *            The event to match
     * @param traceno
     *            The number of the trace this event belongs to
     */
    protected abstract void matchEvent(ITmfEvent event, int traceno);

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

        // TODO Start a new thread here?
        initMatching();

        /*
         * For each trace, get the events and for each event, call the
         * MatchEvent method
         *
         * FIXME This would use a lot of memory if the traces are big, because
         * all involved events from first trace will have to be kept before a
         * first match is possible with second trace.
         *
         * Other possible matching strategy:
         * Incremental:
         * Sliding window:
         * Other strategy: start with the shortest trace, take a few events
         * at the beginning and at the end
         */
        for (int i = 0; i < fTraces.length; i++) {

            EventMatchingBuildRequest request = new EventMatchingBuildRequest(this, i);

            /*
             * Send the request to the trace here, since there is probably no
             * experiment.
             */
            fTraces[i].sendRequest(request);
            try {
                request.waitForCompletion();
            } catch (InterruptedException e) {

            }
        }

        finalizeMatching();

        return true;
    }
}

class EventMatchingBuildRequest extends TmfEventRequest {

    /** The amount of events queried at a time through the requests */
    private final static int chunkSize = 50000;

    private final TmfEventMatching matching;
    private final int traceno;

    EventMatchingBuildRequest(TmfEventMatching matching, int traceno) {
        super(CtfTmfEvent.class,
                TmfTimeRange.ETERNITY,
                TmfDataRequest.ALL_DATA,
                chunkSize,
                ITmfDataRequest.ExecutionType.FOREGROUND);
        this.matching = matching;
        this.traceno = traceno;
    }

    @Override
    public void handleData(final ITmfEvent event) {
        super.handleData(event);
        if (event != null) {
            matching.matchEvent(event, traceno);
        }
    }

    @Override
    public void handleSuccess() {
        super.handleSuccess();
    }

    @Override
    public void handleCancel() {
        super.handleCancel();
    }

    @Override
    public void handleFailure() {
        super.handleFailure();
    }
}
