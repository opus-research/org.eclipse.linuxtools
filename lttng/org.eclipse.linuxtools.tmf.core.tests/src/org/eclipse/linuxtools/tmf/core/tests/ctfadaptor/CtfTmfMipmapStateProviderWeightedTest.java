/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Tasse - Updates to mipmap feature
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.StateSystem;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.InMemoryBackend;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Patrick Tasse
 *
 */
public class CtfTmfMipmapStateProviderWeightedTest {
    private static final String TEST_ATTRIBUTE_NAME = CtfTmfMipmapStateProviderForTest.TEST_ATTRIBUTE_NAME;
    private static final long END_TIME = 250000L;
    private static final long INTERVAL = 1000L;
    private static final int RESOLUTION = 2;
    private static final double DELTA = 0.0001;
    private static StateSystem ssq;

    /**
     * Startup code, build a state system with uneven state durations
     */
    @BeforeClass
    public static void init() {
        CtfTmfMipmapStateProviderForTest ctfmmp = new CtfTmfMipmapStateProviderForTest(null, RESOLUTION, Type.INTEGER);
        IStateHistoryBackend be = new InMemoryBackend(0);
        ssq = new StateSystem(be);
        ctfmmp.assignTargetStateSystem(ssq);
        /*
         * Every 10,000 ns chunk contains the following states:
         *
         * | null |  10  | null |      20     | null |        30          | null |
         * 0     1000   2000   3000   4000   5000   6000   7000   8000   9000  10,000
         *
         * The weighted average for a chunk is (1 x 10 + 2 x 20 + 3 x 30) / 10 = 14.
         */
        for (int i = 0; i < END_TIME / INTERVAL / 10; i++) {
            long time = i * 10 * INTERVAL;
            ctfmmp.processEvent(CtfTmfEventFactoryForTest.createEvent(time, -1));
            ctfmmp.processEvent(CtfTmfEventFactoryForTest.createEvent(time + 1000, 10));
            ctfmmp.processEvent(CtfTmfEventFactoryForTest.createEvent(time + 2000, -1));
            ctfmmp.processEvent(CtfTmfEventFactoryForTest.createEvent(time + 3000, 20));
            ctfmmp.processEvent(CtfTmfEventFactoryForTest.createEvent(time + 5000, -1));
            ctfmmp.processEvent(CtfTmfEventFactoryForTest.createEvent(time + 6000, 30));
            ctfmmp.processEvent(CtfTmfEventFactoryForTest.createEvent(time + 9000, -1));
        }
        ctfmmp.processEvent(CtfTmfEventFactoryForTest.createEvent(END_TIME, 0));
        ctfmmp.dispose();
        ssq.waitUntilBuilt();
    }

    /**
     * Test a query range to the state system to get the maximum value in the
     * range
     *
     * Make sure the state system has data.
     *
     *
     */
    @Test
    public void testQueryMipmapRangeMax() {
        assertNotNull(ssq);
        try {
            int quark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(00, ssq.queryRangeMax(0, 0, quark));
            assertEquals(10, ssq.queryRangeMax(500, 1500, quark));
            assertEquals(20, ssq.queryRangeMax(1500, 5000, quark));
            assertEquals(30, ssq.queryRangeMax(5000, 10000, quark));
            assertEquals(30, ssq.queryRangeMax(0, 10000, quark));
            assertEquals(00, ssq.queryRangeMax(120000, 120000, quark));
            assertEquals(10, ssq.queryRangeMax(120500, 121500, quark));
            assertEquals(20, ssq.queryRangeMax(121500, 125000, quark));
            assertEquals(30, ssq.queryRangeMax(125000, 130000, quark));
            assertEquals(30, ssq.queryRangeMax(120000, 130000, quark));
            assertEquals(30, ssq.queryRangeMax(100000, 150000, quark));
            assertEquals(30, ssq.queryRangeMax(240000, 250000, quark));
            assertEquals(30, ssq.queryRangeMax(0, 250000, quark));
            assertEquals(00, ssq.queryRangeMax(250000, 250000, quark));
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the minimum value in the
     * range
     *
     * Make sure the state system has data.
     *
     *
     */
    @Test
    public void testQueryMipmapRangeMin() {
        assertNotNull(ssq);
        try {
            int quark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(00, ssq.queryRangeMin(0, 0, quark));
            assertEquals(10, ssq.queryRangeMin(500, 1500, quark));
            assertEquals(10, ssq.queryRangeMin(1500, 5000, quark));
            assertEquals(30, ssq.queryRangeMin(5000, 10000, quark));
            assertEquals(10, ssq.queryRangeMin(0, 10000, quark));
            assertEquals(00, ssq.queryRangeMin(120000, 120000, quark));
            assertEquals(10, ssq.queryRangeMin(120500, 121500, quark));
            assertEquals(10, ssq.queryRangeMin(121500, 125000, quark));
            assertEquals(30, ssq.queryRangeMin(125000, 130000, quark));
            assertEquals(10, ssq.queryRangeMin(120000, 130000, quark));
            assertEquals(10, ssq.queryRangeMin(100000, 150000, quark));
            assertEquals(00, ssq.queryRangeMin(240000, 250000, quark));
            assertEquals(00, ssq.queryRangeMin(0, 250000, quark));
            assertEquals(00, ssq.queryRangeMin(250000, 250000, quark));
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the average value in the
     * range
     *
     * Make sure the state system has data.
     *
     */
    @Test
    public void testQueryMipmapRangeAvg() {
        assertNotNull(ssq);
        try {
            int quark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(0.0, ssq.queryRangeAverage(0, 0, quark), DELTA);
            assertEquals(5.0, ssq.queryRangeAverage(500, 1500, quark), DELTA);
            assertEquals(90.0 / 7, ssq.queryRangeAverage(1500, 5000, quark), DELTA);
            assertEquals(90.0 / 5, ssq.queryRangeAverage(5000, 10000, quark), DELTA);
            assertEquals(14.0, ssq.queryRangeAverage(0, 10000, quark), DELTA);
            assertEquals(14.0, ssq.queryRangeAverage(0, 20000, quark), DELTA);
            assertEquals(14.0, ssq.queryRangeAverage(500, 20500, quark), DELTA);
            assertEquals(14.0, ssq.queryRangeAverage(1000, 21000, quark), DELTA);
            assertEquals(14.0, ssq.queryRangeAverage(2000, 22000, quark), DELTA);
            assertEquals(14.0, ssq.queryRangeAverage(3000, 23000, quark), DELTA);
            assertEquals(14.0, ssq.queryRangeAverage(4000, 24000, quark), DELTA);
            assertEquals(14.0, ssq.queryRangeAverage(5000, 25000, quark), DELTA);
            assertEquals(0.0, ssq.queryRangeAverage(120000, 120000, quark), DELTA);
            assertEquals(5.0, ssq.queryRangeAverage(120500, 121500, quark), DELTA);
            assertEquals(90.0 / 7, ssq.queryRangeAverage(121500, 125000, quark), DELTA);
            assertEquals(90.0 / 5, ssq.queryRangeAverage(125000, 130000, quark), DELTA);
            assertEquals(14.0, ssq.queryRangeAverage(120000, 130000, quark), DELTA);
            assertEquals(14.0, ssq.queryRangeAverage(100000, 150000, quark), DELTA);
            assertEquals(14.0, ssq.queryRangeAverage(240000, 250000, quark), DELTA);
            assertEquals(14.0, ssq.queryRangeAverage(0, 250000, quark), DELTA);
            assertEquals(0.0, ssq.queryRangeAverage(250000, 250000, quark), DELTA);
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }
    }
}
