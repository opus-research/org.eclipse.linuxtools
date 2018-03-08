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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfStatsUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.StateSystemManager;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Implementation of ITmfStatistics which uses a state history for storing its
 * information.
 *
 * It requires building the history first, but gives very fast response times
 * when built : Queries are O(log n) wrt the size of the trace, and O(1) wrt to
 * the size of the time interval selected.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */

public class TmfStateStatistics implements ITmfStatistics {

    /** ID for the statistics state system */
    public static final String STATE_ID = "org.eclipse.linuxtools.tmf.statistics"; //$NON-NLS-1$

    /** Filename the "statistics state history" file will have */
    private static final String STATS_STATE_FILENAME = "statistics.ht"; //$NON-NLS-1$

    private final ITmfTrace trace;

    /**
     * The state system that's used to stored the statistics. It's hidden from
     * the trace, so that it doesn't conflict with ITmfTrace.getStateSystem()
     * (which is something else!)
     */
    private final ITmfStateSystem stats;

    /**
     * Empty constructor. The resulting TmfStatistics object will not be usable,
     * but it might be needed for sub-classes.
     */
    public TmfStateStatistics() {
        stats = null;
        trace = null;
    }

    /**
     * Constructor
     *
     * @param trace
     *            The trace for which we build these statistics
     * @throws TmfTraceException
     *             If something went wrong trying to initialize the statistics
     */
    public TmfStateStatistics(ITmfTrace trace) throws TmfTraceException {
        /* Set up the path to the history tree file we'll use */
        this.trace = trace;
        IResource resource = trace.getResource();
        final IStateChangeInput htInput = new StatsStateProvider(trace);
        File htFile = null;
        boolean manual;

        try {
            if (resource != null) {
                // get the directory where the history file will be stored.
                String supplDirectory = resource.getPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER);
                htFile = new File(supplDirectory + File.separator + STATS_STATE_FILENAME);
                manual = false;
            } else {
                /* This is probably a unit test running, just use a temporary file */
                htFile = File.createTempFile("stats-test", ".ht"); //$NON-NLS-1$ //$NON-NLS-2$
                htFile.deleteOnExit();
                manual = true;
            }
        } catch (CoreException e) {
            throw new TmfTraceException(e.toString(), e);
        } catch (IOException e) {
            throw new TmfTraceException(e.toString(), e);
        }


        this.stats = StateSystemManager.loadStateHistory(htFile, htInput, STATE_ID, manual);
    }

    // ------------------------------------------------------------------------
    // ITmfStatistics
    // ------------------------------------------------------------------------

    @Override
    public void updateStats(final boolean isGlobal, final ITmfTimestamp start,
            final ITmfTimestamp end) {
        /*
         * Since we are currently in a signal handler (ie, in the UI thread),
         * and since state system queries can be arbitrarily long (O(log n) wrt
         * the size of the trace), we will run those queries in a separate
         * thread and update the statistics view out-of-band.
         */
        Thread statsThread = new Thread("Statistics update") { //$NON-NLS-1$
            @Override
            public void run() {
                long total;
                Map<String, Long> map;

                /* Wait until the history building completed */
                stats.waitUntilBuilt();

                /* Range should be valid for both global and time range queries */
                total = getEventsInRange(start, end);
                map = getEventTypesInRange(start, end);

                /* Send the signal to notify the stats viewer to update its display */
                TmfSignal sig = new TmfStatsUpdatedSignal(this, trace, isGlobal, total, map);
                TmfSignalManager.dispatchSignal(sig);
            }
        };
        statsThread.start();
        return;

    }

    @Override
    public long getEventsTotal() {
        /* We need the complete state history to be built to answer this. */
        stats.waitUntilBuilt();

        long endTime = stats.getCurrentEndTime();
        int count = 0;

        try {
            final int quark = stats.getQuarkAbsolute(Attributes.TOTAL);
            count= stats.querySingleState(endTime, quark).getStateValue().unboxInt();

        } catch (TimeRangeException e) {
            /* Assume there is no events for that range */
            return 0;
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        }

        return count;
    }

    @Override
    public Map<String, Long> getEventTypesTotal() {
        /* We need the complete state history to be built to answer this. */
        stats.waitUntilBuilt();

        Map<String, Long> map = new HashMap<String, Long>();
        long endTime = stats.getCurrentEndTime();

        try {
            /* Get the list of quarks, one for each even type in the database */
            int quark = stats.getQuarkAbsolute(Attributes.EVENT_TYPES);
            List<Integer> quarks = stats.getSubAttributes(quark, false);

            /* Since we want the total we can look only at the end */
            List<ITmfStateInterval> endState = stats.queryFullState(endTime);

            String curEventName;
            long eventCount;
            for (int typeQuark : quarks) {
                curEventName = stats.getAttributeName(typeQuark);
                eventCount = endState.get(typeQuark).getStateValue().unboxInt();
                map.put(curEventName, eventCount);
            }

        } catch (TimeRangeException e) {
            /* Assume there is no events, nothing will be put in the map. */
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public long getEventsInRange(ITmfTimestamp start, ITmfTimestamp end) {
        // FIXME Instead of waiting until the end, we could check the current
        // end time, and answer as soon as possible...
        stats.waitUntilBuilt();

        int countAtStart = 0, countAtEnd = 0;
        long startTime = checkStartTime(start);
        long endTime = checkEndTime(end);

        try {
            final int quark = stats.getQuarkAbsolute(Attributes.TOTAL);
            if (startTime == stats.getStartTime()) {
                countAtStart = 0;
            } else {
                /* State system works that way... */
                countAtStart = stats.querySingleState(startTime - 1, quark).getStateValue().unboxInt();
            }
            countAtEnd = stats.querySingleState(endTime, quark).getStateValue().unboxInt();

        } catch (TimeRangeException e) {
            /* Assume there is no events for that range */
            return 0;
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        }

        long total = countAtEnd - countAtStart;
        return total;
    }

    @Override
    public Map<String, Long> getEventTypesInRange(ITmfTimestamp start, ITmfTimestamp end) {
        // FIXME Instead of waiting until the end, we could check the current
        // end time, and answer as soon as possible...
        stats.waitUntilBuilt();

        Map<String, Long> map = new HashMap<String, Long>();

        /* Make sure the start/end times are within the state history, so we
         * don't get TimeRange exceptions.
         */
        long startTime = checkStartTime(start);
        long endTime = checkEndTime(end);

        try {
            /* Get the list of quarks, one for each even type in the database */
            int quark = stats.getQuarkAbsolute(Attributes.EVENT_TYPES);
            List<Integer> quarks = stats.getSubAttributes(quark, false);

            /*
             * Get the complete states (in our case, event counts) at the start
             * time and end time of the requested time range.
             */
            List<ITmfStateInterval> startState = stats.queryFullState(startTime);
            List<ITmfStateInterval> endState = stats.queryFullState(endTime);

            /* Save the relevant information in the map we will be returning */
            String curEventName;
            long countAtStart, countAtEnd, eventCount;
            for (int typeQuark : quarks) {
                curEventName = stats.getAttributeName(typeQuark);
                countAtStart = startState.get(typeQuark).getStateValue().unboxInt();
                countAtEnd = endState.get(typeQuark).getStateValue().unboxInt();

                /*
                 * The default value for the statistics is 0, rather than the
                 * value -1 used by the state system for non-initialized state.
                 */
                if (startTime == stats.getStartTime() || countAtStart == -1) {
                    countAtStart = 0;
                }

                /*
                 * If after this it is still at -1, it's because no event of
                 * this type happened during the requested time range.
                 */
                if (countAtEnd < 0) {
                    countAtEnd = 0;
                }

                eventCount = countAtEnd - countAtStart;
                map.put(curEventName, eventCount);
            }
        } catch (TimeRangeException e) {
            /* Assume there is no events, nothing will be put in the map. */
        } catch (AttributeNotFoundException e) {
            /*
             * These other exception types would show a logic problem however,
             * so they should not happen.
             */
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        }
        return map;
    }

    protected long checkStartTime(ITmfTimestamp startTs) {
        long start = startTs.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        if (start < stats.getStartTime()) {
            return stats.getStartTime();
        }
        return start;
    }

    protected long checkEndTime(ITmfTimestamp endTs) {
        long end = endTs.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        if (end > stats.getCurrentEndTime()) {
            return stats.getCurrentEndTime();
        }
        return end;
    }


    /**
     * The attribute names that are used in the state provider
     */
    public static class Attributes {

        /** Total nb of events */
        public static final String TOTAL = "total"; //$NON-NLS-1$

        /** event_types */
        public static final String EVENT_TYPES = "event_types"; //$NON-NLS-1$<
    }
}
