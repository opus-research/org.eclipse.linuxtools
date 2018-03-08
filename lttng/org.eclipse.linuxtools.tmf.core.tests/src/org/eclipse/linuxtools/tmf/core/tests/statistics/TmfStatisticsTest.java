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

package org.eclipse.linuxtools.tmf.core.tests.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.statistics.ITmfStatistics;
import org.junit.Test;

/**
 * Base unit test class for any type of ITmfStatistics. Sub-classes should
 * implement a "@BeforeClass" method to setup the 'backend' fixture accordingly.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TmfStatisticsTest {

    protected static ITmfStatistics backend;

    /* Known values about the trace */
    private static final int SCALE = ITmfTimestamp.NANOSECOND_SCALE;
    private static final int totalNbEvents = 695319;
    private static final ITmfTimestamp tStart = new TmfTimestamp(1332170682440133097L, SCALE); /* Timestamp of first event */
    private static final ITmfTimestamp tEnd   = new TmfTimestamp(1332170692664579801L, SCALE); /* Timestamp of last event */

    /* Timestamps of interest */
    private static final ITmfTimestamp t1 = new TmfTimestamp(1332170682490946000L, SCALE);
    private static final ITmfTimestamp t2 = new TmfTimestamp(1332170682490947524L, SCALE); /* event exactly here */
    private static final ITmfTimestamp t3 = new TmfTimestamp(1332170682490948000L, SCALE);
    private static final ITmfTimestamp t4 = new TmfTimestamp(1332170682490949000L, SCALE);
    private static final ITmfTimestamp t5 = new TmfTimestamp(1332170682490949270L, SCALE); /* following event here */
    private static final ITmfTimestamp t6 = new TmfTimestamp(1332170682490949300L, SCALE);

    private static final String eventType = "lttng_statedump_process_state"; //$NON-NLS-1$

    /**
     * Test for {@link ITmfStatistics#getEventsTotal()}
     */
    @Test
    public void testGetEventsTotal() {
        long count = backend.getEventsTotal();
        assertEquals(totalNbEvents, count);

        /* Passing the trace range manually should give the same result */
        count = backend.getEventsInRange(tStart, tEnd);
        assertEquals(totalNbEvents, count);
    }

    /**
     * Query the whole range, but without the first event (there is only one
     * event at that timestamp).
     */
    @Test
    public void testGetTotalMinusStart() {
        long count = backend.getEventsInRange(new TmfTimestamp(tStart.getValue() + 1, SCALE), tEnd);
        assertEquals(totalNbEvents - 1, count);
    }

    /**
     * Query the whole range, but without the last event (there is only one
     * event at the end timestamp).
     */
    @Test
    public void testGetTotalMinusEnd() {
        long count = backend.getEventsInRange(tStart, new TmfTimestamp(tEnd.getValue() - 1, SCALE));
        assertEquals(totalNbEvents - 1, count);
    }

    /**
     * Test when both the start and end times don't match an event
     */
    @Test
    public void testRangeNormal() {
        long count = backend.getEventsInRange(t1, t6);
        assertEquals(2, count);
    }

    /**
     * Test when the *start* of the interval is exactly on an event (the event
     * should be included)
     */
    @Test
    public void testRangeEventAtStart() {
        long count = backend.getEventsInRange(t2, t3);
        assertEquals(1, count);

        count = backend.getEventsInRange(t2, t6);
        assertEquals(2, count);
    }

    /**
     * Test when the *end* of the interval is exactly on an event (the event
     * should be included)
     */
    @Test
    public void testRangeEventAtEnd() {
        long count = backend.getEventsInRange(t4, t5);
        assertEquals(1, count);

        count = backend.getEventsInRange(t1, t5);
        assertEquals(2, count);
    }

    /**
     * Test when there are events matching exactly both the start and end times
     * of the range (both should be included).
     */
    @Test
    public void testRangeEventAtBoth() {
        long count = backend.getEventsInRange(t2, t5);
        assertEquals(2, count);
    }

    /**
     * Test when there are no events in a given range.
     */
    @Test
    public void testRangeNoEvents() {
        long count = backend.getEventsInRange(t3, t4);
        assertEquals(0, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventTypesInRange}
     */
    @Test
    public void testEventTypesInRange() {
        Map<String, Long> result = backend.getEventTypesInRange(t1, t5);
        assertEquals(new Long(2L), result.get(eventType));

        /*
         * It should also be the only value in the map (we'll ignore the entries
         * with 0 for value).
         */
        long count = 0;
        for (Long val : result.values()) {
            count += val;
        }
        assertEquals(2, count);
    }

    /**
     * Test for the event types query over the whole range.
     */
    @Test
    public void testEventTypesTotal() {
        Map<String, Long> res1, res2;
        res1 = backend.getEventTypesTotal();
        res2 = backend.getEventTypesInRange(tStart, tEnd);
        assertTrue(res1.equals(res2));
        assertEquals(126, res1.size()); /* Number of different event types in the trace */

        long count = 0;
        for (Long val : res1.values()) {
            count += val;
        }
        assertEquals(totalNbEvents, count);
    }
}
