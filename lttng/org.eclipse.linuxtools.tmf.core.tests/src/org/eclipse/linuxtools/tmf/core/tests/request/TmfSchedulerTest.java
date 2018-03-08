/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Simon Delisle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.request;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.component.ITmfComponent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalThrottler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the scheduler.
 */
public class TmfSchedulerTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final int TRACE_INDEX = 0;
    private static final String PATH = CtfTmfTestTraces.getTestTracePath(TRACE_INDEX);
    private static final int NB_EVENTS_TRACE = 695319;
    private static final int NB_EVENTS_TIME_RANGE = 155133;
    private static final TmfTimeRange ETERNITY_TIME_RANGE = new TmfTimeRange(TmfTimeRange.ETERNITY);

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private CtfTmfTrace fixture;

    private long fStartTime;
    private long fEndTime;
    private TmfTimeRange fForegroundTimeRange;

    private TmfSignalThrottler fTimeSyncThrottle;

    /**
     * Perform pre-test initialization.
     *
     * @throws TmfTraceException
     *             If the test trace is not found
     */
    @Before
    public void setUp() throws TmfTraceException {
        assumeTrue(CtfTmfTestTraces.tracesExist());
        fixture = new CtfTmfTrace();
        fixture.initTrace((IResource) null, PATH, CtfTmfEvent.class);
        fixture.indexTrace(true);
        fStartTime = fixture.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        fEndTime = fixture.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        fTimeSyncThrottle = new TmfSignalThrottler(new SchedulerTestComponent(), 200);

        long foregroundStartTime = fStartTime + ((fEndTime - fStartTime) / 4);
        long foregroundEndTime = fStartTime + ((fEndTime - fStartTime) / 2);
        fForegroundTimeRange = new TmfTimeRange(new TmfTimestamp(foregroundStartTime, ITmfTimestamp.NANOSECOND_SCALE, 0), new TmfTimestamp(foregroundEndTime, ITmfTimestamp.NANOSECOND_SCALE, 0));
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        if (fixture != null) {
            fixture.dispose();
        }
    }

    // ------------------------------------------------------------------------
    // Tests cases
    // ------------------------------------------------------------------------

    /**
     * Test one background request
     */
    @Test
    public void backgroundRequest() {
        BackgroundRequest background = new BackgroundRequest(ETERNITY_TIME_RANGE);
        fixture.sendRequest(background);
        try {
            background.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(NB_EVENTS_TRACE, background.getNbEvents());
    }

    /**
     * Test one foreground request
     */
    @Test
    public void foregroundRequest() {
        ForegroundRequest foreground = new ForegroundRequest(ETERNITY_TIME_RANGE);
        fixture.sendRequest(foreground);
        try {
            foreground.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(NB_EVENTS_TRACE, foreground.getNbEvents());
    }

    /**
     * Test one foreground and one background request for the entire trace at
     * the same time
     */
    @Test
    public void TestMultiRequest1() {
        BackgroundRequest background = new BackgroundRequest(ETERNITY_TIME_RANGE);
        ForegroundRequest foreground = new ForegroundRequest(ETERNITY_TIME_RANGE);

        fixture.sendRequest(background);
        fixture.sendRequest(foreground);
        try {
            background.waitForCompletion();
            foreground.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(background.getNbEvents(), foreground.getNbEvents());
    }

    /**
     * Test one background request for the entire trace and one foreground
     * request for smaller time range
     */
    @Test
    public void TestMultiRequest2() {
        BackgroundRequest background2 = new BackgroundRequest(ETERNITY_TIME_RANGE);
        ForegroundRequest foreground2 = new ForegroundRequest(fForegroundTimeRange);

        fixture.sendRequest(background2);
        fixture.sendRequest(foreground2);
        try {
            background2.waitForCompletion();
            foreground2.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(NB_EVENTS_TRACE, background2.getNbEvents());
        assertEquals(NB_EVENTS_TIME_RANGE, foreground2.getNbEvents());
    }

    /**
     * Test two foreground request, one to select a time range and one to select
     * an event in this time range
     */
    @Test
    public void TestMultiRequest3() {
        ForegroundRequest foreground3 = new ForegroundRequest(ETERNITY_TIME_RANGE);
        fixture.sendRequest(foreground3);

        TmfTimeSynchSignal signal3 = new TmfTimeSynchSignal(this, new TmfTimestamp(fForegroundTimeRange.getStartTime()));
        fTimeSyncThrottle.queue(signal3);

        try {
            foreground3.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(NB_EVENTS_TRACE, foreground3.getNbEvents());
    }

    /**
     * Test two foreground request, one to select a time range and one to select
     * an event before this time range
     */
    @Test
    public void TestMultiRequest4() {
        ForegroundRequest foreground4 = new ForegroundRequest(fForegroundTimeRange);
        fixture.sendRequest(foreground4);
        TmfTimeSynchSignal signal4 = new TmfTimeSynchSignal(this, new TmfTimestamp(fStartTime + ((fEndTime - fStartTime) / 8)));
        fTimeSyncThrottle.queue(signal4);

        try {
            foreground4.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(NB_EVENTS_TIME_RANGE, foreground4.getNbEvents());
    }

    /**
     * Test two foreground request, one to select a time range and one to select
     * an event after this time range
     */
    @Test
    public void TestMultiRequest5() {
        ForegroundRequest foreground5 = new ForegroundRequest(fForegroundTimeRange);
        fixture.sendRequest(foreground5);
        TmfTimeSynchSignal signal5 = new TmfTimeSynchSignal(this, new TmfTimestamp(fEndTime - ((fEndTime - fStartTime) / 4)));
        fTimeSyncThrottle.queue(signal5);

        try {
            foreground5.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(NB_EVENTS_TIME_RANGE, foreground5.getNbEvents());
    }

    /**
     * Test one background and one foreground request for the entire trace and
     * one foreground request to select an event
     */
    @Test
    public void TestMultiRequest6() {
        BackgroundRequest background6 = new BackgroundRequest(ETERNITY_TIME_RANGE);
        ForegroundRequest foreground6 = new ForegroundRequest(ETERNITY_TIME_RANGE);

        fixture.sendRequest(background6);
        fixture.sendRequest(foreground6);

        TmfTimeSynchSignal signal6 = new TmfTimeSynchSignal(this, new TmfTimestamp(fStartTime + ((fEndTime - fStartTime) / 8)));
        fTimeSyncThrottle.queue(signal6);

        try {
            background6.waitForCompletion();
            foreground6.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(NB_EVENTS_TRACE, background6.getNbEvents());
        assertEquals(NB_EVENTS_TRACE, foreground6.getNbEvents());
    }

    /**
     * Four request, two foreground and two background
     */
    @Test
    public void TestMultiRequest7() {
        ForegroundRequest foreground7 = new ForegroundRequest(ETERNITY_TIME_RANGE);
        ForegroundRequest foreground8 = new ForegroundRequest(fForegroundTimeRange);
        BackgroundRequest background7 = new BackgroundRequest(ETERNITY_TIME_RANGE);
        BackgroundRequest background8 = new BackgroundRequest(ETERNITY_TIME_RANGE);
        fixture.sendRequest(foreground7);
        fixture.sendRequest(foreground8);
        fixture.sendRequest(background7);
        fixture.sendRequest(background8);
        try {
            foreground7.waitForCompletion();
            foreground8.waitForCompletion();
            background7.waitForCompletion();
            background8.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(NB_EVENTS_TRACE, foreground7.getNbEvents());
        assertEquals(NB_EVENTS_TIME_RANGE, foreground8.getNbEvents());
        assertEquals(NB_EVENTS_TRACE, background7.getNbEvents());
        assertEquals(NB_EVENTS_TRACE, background8.getNbEvents());
    }

    /**
     * One long foreground request and one short foreground request, the short
     * one should finish first
     */
    @Test
    public void preemptedForegroundRequest() {
        ForegroundRequest foreground9 = new ForegroundRequest(ETERNITY_TIME_RANGE);
        TmfTimeRange shortTimeRange = new TmfTimeRange(new TmfTimestamp(fStartTime, ITmfTimestamp.NANOSECOND_SCALE, 0), new TmfTimestamp(fStartTime + ((fEndTime - fStartTime) / 16), ITmfTimestamp.NANOSECOND_SCALE, 0));
        ForegroundRequest shortForeground = new ForegroundRequest(shortTimeRange);
        fixture.sendRequest(foreground9);
        fixture.sendRequest(shortForeground);
        try {
            shortForeground.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(foreground9.isCompleted());
    }

    /**
     * One long background request and one short foreground request, the
     * foreground request should finish first
     */
    @Test
    public void preemptedBackgroundRequest() {
        BackgroundRequest background9 = new BackgroundRequest(ETERNITY_TIME_RANGE);
        ForegroundRequest foreground10 = new ForegroundRequest(fForegroundTimeRange);
        fixture.sendRequest(background9);
        fixture.sendRequest(foreground10);
        try {
            foreground10.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(foreground10.isCompleted());
        assertFalse(background9.isCompleted());
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private class SchedulerTestComponent implements ITmfComponent {

        @Override
        public String getName() {
            return this.getClass().getSimpleName();
        }

        @Override
        public void dispose() {
        }

        @Override
        public void broadcast(TmfSignal signal) {
        }

    }

    private class BackgroundRequest extends TmfEventRequest {
        private static final int CHUNK_SIZE = 0;
        private int nbEvents = 0;

        BackgroundRequest(TmfTimeRange timeRange) {
            super(fixture.getEventType(), timeRange,
                    TmfDataRequest.ALL_DATA,
                    CHUNK_SIZE,
                    ExecutionType.BACKGROUND);
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            ++nbEvents;
        }

        public int getNbEvents() {
            return nbEvents;
        }
    }

    private class ForegroundRequest extends TmfEventRequest {
        private static final int CHUNK_SIZE = 0;
        private int nbEvents = 0;

        ForegroundRequest(TmfTimeRange timeRange) {
            super(fixture.getEventType(), timeRange,
                    TmfDataRequest.ALL_DATA,
                    CHUNK_SIZE,
                    ExecutionType.FOREGROUND);
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            ++nbEvents;
        }

        public int getNbEvents() {
            return nbEvents;
        }
    }
}