/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statistics;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Implementation of ITmfStatistics which uses event requests to the trace to
 * retrieve its information.
 *
 * There is almost no setup time, but queries themselves are longer than with a
 * TmfStateStatistics. Queries are O(n * m), where n is the size of the trace,
 * and m is the portion of the trace covered by the selected interval.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfEventsStatistics implements ITmfStatistics {

    private final ITmfTrace trace;

    /**
     * Constructor
     *
     * @param trace
     *            The trace for which we are building the statistics
     */
    public TmfEventsStatistics(ITmfTrace trace) {
        this.trace = trace;
    }

    @Override
    public long getEventsTotal() {
        final Map<String, Long> stats =  new HashMap<String, Long>();
        final StatsRequest request = new StatsRequest(trace, TmfTimeRange.ETERNITY, stats);

        send(request);
        return addContentsOfMap(stats);
    }

    @Override
    public Map<String, Long> getEventTypesTotal() {
        final Map<String, Long> stats =  new HashMap<String, Long>();
        final StatsRequest request = new StatsRequest(trace, TmfTimeRange.ETERNITY, stats);

        send(request);
        return stats;
    }

    @Override
    public long getEventsInRange(ITmfTimestamp start, ITmfTimestamp end) {
        final TmfTimeRange range = new TmfTimeRange(start, end);
        final Map<String, Long> stats =  new HashMap<String, Long>();
        final StatsRequest request = new StatsRequest(trace, range, stats);

        send(request);
        return addContentsOfMap(stats);
    }

    @Override
    public Map<String, Long> getEventTypesInRange(ITmfTimestamp start,
            ITmfTimestamp end) {
        final TmfTimeRange range = new TmfTimeRange(start, end);
        final Map<String, Long> stats =  new HashMap<String, Long>();
        final StatsRequest request = new StatsRequest(trace, range, stats);

        send(request);
        return stats;
    }

    private synchronized void send(StatsRequest request) {
        trace.sendRequest(request);
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static long addContentsOfMap(Map<String, Long> map) {
        long count = 0;
        for (long val : map.values()) {
            count += val;
        }
        return count;
    }


    /**
     * Event request that the TmfEventsStatistics will send to the trace
     */
    private class StatsRequest extends TmfEventRequest {

        private final static int chunkSize = 50000;

        /* Map in which the results are saved */
        private final Map<String, Long> stats;

        public StatsRequest(ITmfTrace trace, TmfTimeRange range, Map<String, Long> stats) {
            super(trace.getEventType(), range, TmfDataRequest.ALL_DATA,
                    chunkSize, ITmfDataRequest.ExecutionType.BACKGROUND);
            this.stats = stats;
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (event != null) {
                if (event.getTrace() == trace) {
                    processEvent(event);
                }
            }
        }

        private void processEvent(ITmfEvent event) {
            String eventType = event.getType().getName();
            if (stats.containsKey(eventType)) {
                long curValue = stats.get(eventType);
                stats.put(eventType, curValue + 1L);
            } else {
                stats.put(eventType, 1L);
            }
        }
    }

}
