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

package org.eclipse.linuxtools.lttng2.kernel.core.event.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.TcpEventStrings;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.matching.IMatchProcessingUnit;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventDependency;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Class to match tcp type events. See
 * {@link org.eclipse.linuxtools.internal.lttng2.kernel.core.TcpEventStrings}
 * for details on how to obtain a trace with those events
 *
 * @author gbastien
 * @since 2.0
 */
public class TcpEventMatching extends TmfEventMatching {

    /**
     * Hashtables for unmatches incoming events
     */
    private final List<Map<List<Object>, ITmfEvent>> fUnmatchedIn = new ArrayList<Map<List<Object>, ITmfEvent>>();

    /**
     * Hashtables for unmatches outgoing events
     */
    private final List<Map<List<Object>, ITmfEvent>> fUnmatchedOut = new ArrayList<Map<List<Object>, ITmfEvent>>();

    /**
     * Enum for in and out types
     */
    private enum Direction {
        IN,
        OUT,
    }

    /**
     * Constructor taking one trace
     *
     * @param trace
     *            The trace in which to match events
     */
    public TcpEventMatching(ITmfTrace trace) {
        super(trace);
    }

    /**
     * Constructor with multiple traces
     *
     * @param traces
     *            The set of traces for which to match events
     */
    public TcpEventMatching(ITmfTrace[] traces) {
        super(traces);
    }

    /**
     * Constructor with multiple traces and match processing object
     *
     * @param traces
     *            The set of traces for which to match events
     * @param tmfEventMatches
     *            The match processing class
     */
    public TcpEventMatching(ITmfTrace[] traces, IMatchProcessingUnit tmfEventMatches) {
        super(traces, tmfEventMatches);
    }

    /**
     * Constructor with one trace and match processing object
     *
     * @param trace
     *            The trace in which to match events
     * @param tmfEventMatches
     *            The match processing class
     */
    public TcpEventMatching(ITmfTrace trace, IMatchProcessingUnit tmfEventMatches) {
        super(trace, tmfEventMatches);
    }

    /**
     * Default constructor
     */
    public TcpEventMatching() {
        super();
    }

    /**
     * Method that initializes any data structure for the event matching
     */
    @Override
    public void initMatching() {
        // Initialize the matching infrastructure (unmatched event lists)
        fUnmatchedIn.clear();
        fUnmatchedOut.clear();
        for (int i = 0; i < fTraces.length; i++) {
            fUnmatchedIn.add(new HashMap<List<Object>, ITmfEvent>());
            fUnmatchedOut.add(new HashMap<List<Object>, ITmfEvent>());
        }
    }

    /**
     * Function that counts the events in a hashtable.
     *
     * @param tbl
     *            The table to count events for
     * @return The number of events
     */
    protected int countEvents(Map<List<Object>, ITmfEvent> tbl) {
        return tbl.size();
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
        final String cr = System.getProperty("line.separator");
        StringBuilder b = new StringBuilder();
        b.append(fMatches);
        for (int i = 0; i < fTraces.length; i++) {
            b.append("Trace " + i + ":" + cr +
                    "  " + countEvents(fUnmatchedIn.get(i)) + " unmatched incoming events" + cr +
                    "  " + countEvents(fUnmatchedOut.get(i)) + " unmatched outgoing events" + cr);
        }

        return b.toString();
    }

    /**
     * The key to uniquely identify a TCP packet depends on many fields. This
     * method computes the key for a given event.
     *
     * @param event
     *            The event for which to compute the key
     * @return the unique key for this event
     */
    private static List<Object> getUniqueField(final ITmfEvent event) {

        List<Object> keys = new ArrayList<Object>();

        keys.add(event.getContent().getField(TcpEventStrings.SEQ).getValue());
        keys.add(event.getContent().getField(TcpEventStrings.ACKSEQ).getValue());
        keys.add(event.getContent().getField(TcpEventStrings.FLAGS).getValue());

        return keys;
    }

    /**
     * Matches one event
     *
     * @param event
     *            The event to match
     * @param traceno
     *            The number of the trace this event belongs to
     *
     * @return A pair of event if a match was found, null otherwise
     */
    @Override
    public TmfEventDependency matchEvent(ITmfEvent event, int traceno) {

        String evname = event.getType().getName();
        Direction evtype;
        /* Is the event a tcp socket in or out event */
        if (evname.equals(TcpEventStrings.INET_SOCK_LOCAL_IN)) {
            evtype = Direction.IN;
        } else if (evname.equals(TcpEventStrings.INET_SOCK_LOCAL_OUT)) {
            evtype = Direction.OUT;
        } else {
            return null;
        }

        /* Get the event's unique fields */
        List<Object> eventKey = getUniqueField(event);
        List<Map<List<Object>, ITmfEvent>> unmatchedTbl, companionTbl;

        /* Point to the appropriate table */
        switch (evtype) {
        case IN:
            unmatchedTbl = fUnmatchedIn;
            companionTbl = fUnmatchedOut;
            break;
        case OUT:
            unmatchedTbl = fUnmatchedOut;
            companionTbl = fUnmatchedIn;
            break;
        default:
            return null;
        }

        boolean found = false;
        TmfEventDependency dep = null;
        /* Search for the event in the companion table */
        for (Map<List<Object>, ITmfEvent> map : companionTbl) {
            if (map.containsKey(eventKey)) {
                found = true;
                ITmfEvent companionEvent = map.get(eventKey);

                /* Remove the element from the companion table */
                map.remove(eventKey);

                /* Create the dependency object */
                switch (evtype) {
                case IN:
                    dep = new TmfEventDependency(companionEvent, event);
                    break;
                case OUT:
                    dep = new TmfEventDependency(event, companionEvent);
                    break;
                default:
                    break;

                }
            }
        }

        /*
         * If no companion was found, add the event to the appropriate unMatched
         * lists
         */
        if (found) {
            fMatches.addMatch(dep);
        } else {
            unmatchedTbl.get(traceno).put(eventKey, event);
        }

        return dep;
    }

}
