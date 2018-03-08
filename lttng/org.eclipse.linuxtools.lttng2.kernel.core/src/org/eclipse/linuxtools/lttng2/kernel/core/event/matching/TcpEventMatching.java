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

package org.eclipse.linuxtools.lttng2.kernel.core.event.matching;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.TcpEventStrings;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventDependency;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventMatches;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * Class to match tcp type events. See
 * {@link org.eclipse.linuxtools.internal.lttng2.kernel.core.TcpEventStrings}
 * for details on how to obtain a trace with those events
 *
 * @author gbastien
 * @since 2.0
 *
 */
public class TcpEventMatching extends TmfEventMatching {

    /**
     * Hashtables for unmatches incoming events
     */
    protected Hashtable<Object, LinkedList<ITmfEvent>>[] fUnmatchedIn;

    /**
     * Hashtables for unmatches outgoing events
     */
    protected Hashtable<Object, LinkedList<ITmfEvent>>[] fUnmatchedOut;

    /**
     * Constants for in and out types
     */
    protected static final int IN = 0;
    /**
     * Constants for in and out types
     */
    protected static final int OUT = 1;

    /**
     * Constructor taking one trace
     *
     * @param trace
     *            The trace in which to match events
     */
    public TcpEventMatching(TmfTrace trace) {
        super(trace);
    }

    /**
     * Constructor with multiple traces
     *
     * @param traces
     *            The set of traces for which to match events
     */
    public TcpEventMatching(TmfTrace[] traces) {
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
    public TcpEventMatching(TmfTrace[] traces, TmfEventMatches tmfEventMatches) {
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
    public TcpEventMatching(TmfTrace trace, TmfEventMatches tmfEventMatches) {
        super(trace, tmfEventMatches);
    }

    /**
     * Method that initializes any data structure for the event matching
     */
    @Override
    public void initMatching() {
        // Initialize the matching infrastructure (unmatched event lists)
        fUnmatchedIn = new Hashtable[fTraces.length];
        fUnmatchedOut = new Hashtable[fTraces.length];
        for (int i = 0; i < fTraces.length; i++) {
            fUnmatchedIn[i] = new Hashtable<Object, LinkedList<ITmfEvent>>();
            fUnmatchedOut[i] = new Hashtable<Object, LinkedList<ITmfEvent>>();
        }
    }

    /**
     * Function that counts the events in a hashtable. Since for each key there
     * can be a number of events, we need to iterator through all the hashtable
     *
     * @param tbl
     *            The table to count events for
     * @return The number of events
     */
    protected int countEvents(Hashtable<Object, LinkedList<ITmfEvent>> tbl) {
        Iterator<Entry<Object, LinkedList<ITmfEvent>>> iterator = tbl.entrySet().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            count += iterator.next().getValue().size();
        }
        return count;
    }

    /**
     * Prints stats from the matching
     *
     * @return string of statistics
     *
     */
    @SuppressWarnings("nls")
    @Override
    public String printMatchingStats() {
        final String cr = System.getProperty("line.separator");
        StringBuilder b = new StringBuilder();
        b.append("Number of matches found: " + fMatches.getMatches().size() + cr);
        for (int i = 0; i < fTraces.length; i++) {
            b.append("" +
                    "Trace " + i + ":" + cr +
                    "  " + countEvents(fUnmatchedIn[i]) + " unmatched incoming events" + cr +
                    "  " + countEvents(fUnmatchedOut[i]) + " unmatched outgoing events" + cr);
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
     * @return A pair of event if a match was found, null otherwise
     */
    @Override
    public TmfEventDependency matchEvent(ITmfEvent event, int traceno) {

        String evname = event.getType().getName();
        int evtype;
        // Is the event a tcp socket in or out event
        if (evname.equals(TcpEventStrings.INET_SOCK_LOCAL_IN)) {
            evtype = IN;
        } else if (evname.equals(TcpEventStrings.INET_SOCK_LOCAL_OUT)) {
            evtype = OUT;
        } else {
            return null;
        }

        // Get the event's sequence number
        ITmfEventField seq = event.getContent().getField(TcpEventStrings.SEQ);
        Hashtable<Object, LinkedList<ITmfEvent>>[] unmatchedTbl, companionTbl;

        // Point to the appropriate table
        if (evtype == IN) {
            unmatchedTbl = fUnmatchedIn;
            companionTbl = fUnmatchedOut;
        } else {
            unmatchedTbl = fUnmatchedOut;
            companionTbl = fUnmatchedIn;
        }

        boolean found = false;
        TmfEventDependency dep = null;
        // Search for the event in the companion table
        for (int i = 0; i < fTraces.length; i++) {
            if (companionTbl[i].containsKey(seq.getValue())) {
                found = true;
                ITmfEvent companionEvent = companionTbl[i].get(seq.getValue()).poll();

                // Remove the key if there are no more elements
                if (companionTbl[i].get(seq.getValue()).isEmpty()) {
                    companionTbl[i].remove(seq.getValue());
                }

                if (evtype == IN) {
                    dep = new TmfEventDependency(companionEvent, event);
                } else {
                    dep = new TmfEventDependency(event, companionEvent);
                }
            }
        }
        if (!found) {
            if (!unmatchedTbl[traceno].containsKey(seq.getValue())) {
                unmatchedTbl[traceno].put(seq.getValue(), new LinkedList<ITmfEvent>());
            }
            unmatchedTbl[traceno].get(seq.getValue()).add(event);
        }

        return dep;
    }

}
