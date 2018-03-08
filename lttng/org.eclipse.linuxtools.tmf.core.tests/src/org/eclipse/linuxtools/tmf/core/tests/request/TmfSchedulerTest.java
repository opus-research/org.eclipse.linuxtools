/*******************************************************************************
 * Copyright (c) 2013 Ericsson
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
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
public class TmfSchedulerTest extends TmfRequestExecutorTest {

    // ------------------------------------------------------------------------
    // Tests cases
    // ------------------------------------------------------------------------

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

    /**
     * Test if the scheduler is working as expected
     */
    @Test
    public void executionOrder() {
        List<String> expectedOrder = new LinkedList<String>();
        expectedOrder.add("FOREGROUND1");
        expectedOrder.add("FOREGROUND2");
        expectedOrder.add("FOREGROUND3");
        expectedOrder.add("FOREGROUND4");
        expectedOrder.add("BACKGROUND1");
        expectedOrder.add("FOREGROUND1");
        expectedOrder.add("FOREGROUND2");
        expectedOrder.add("FOREGROUND3");
        expectedOrder.add("FOREGROUND4");
        expectedOrder.add("BACKGROUND2");

        fOrderList.clear();
        fForegroundId = 0;
        fBackgroundId = 0;

        BackgroundRequest background1 = new BackgroundRequest(TmfTimeRange.ETERNITY);
        BackgroundRequest background2 = new BackgroundRequest(TmfTimeRange.ETERNITY);

        ForegroundRequest foreground1 = new ForegroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground2 = new ForegroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground3 = new ForegroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground4 = new ForegroundRequest(TmfTimeRange.ETERNITY);

        fixture.sendRequest(foreground1);
        fixture.sendRequest(foreground2);
        fixture.sendRequest(foreground3);
        fixture.sendRequest(foreground4);
        fixture.sendRequest(background1);
        fixture.sendRequest(background2);
        try {
            foreground1.waitForCompletion();
            foreground2.waitForCompletion();
            foreground3.waitForCompletion();
            foreground4.waitForCompletion();
            background1.waitForCompletion();
            background2.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(expectedOrder, fOrderList.subList(0, expectedOrder.size()));
    }

}