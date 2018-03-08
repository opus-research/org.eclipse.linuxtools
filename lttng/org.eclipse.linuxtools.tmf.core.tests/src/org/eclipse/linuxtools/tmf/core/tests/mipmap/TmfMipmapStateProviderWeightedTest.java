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

package org.eclipse.linuxtools.tmf.core.tests.mipmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.StateSystem;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.InMemoryBackend;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Patrick Tasse
 *
 */
public class TmfMipmapStateProviderWeightedTest {
    private static final String TEST_ATTRIBUTE_NAME = TmfMipmapStateProvider.TEST_ATTRIBUTE_NAME;
    private static final long END_TIME = 250000L;
    private static final long INTERVAL = 1000L;
    private static final int RESOLUTION = 2;
    private static final double DELTA = 0.0001;
    private static StateSystem ssqi;
    private static StateSystem ssqd;

    /**
     * Startup code, build a state system with uneven state durations
     */
    @BeforeClass
    public static void init() {
        /* setup for INTEGER test */
        TmfMipmapStateProvider mmpi = new TmfMipmapStateProvider(RESOLUTION, Type.INTEGER);
        IStateHistoryBackend bei = new InMemoryBackend(0);
        ssqi = new StateSystem(bei);
        mmpi.assignTargetStateSystem(ssqi);
        /* setup for DOUBLE test */
        TmfMipmapStateProvider mmpd = new TmfMipmapStateProvider(RESOLUTION, Type.DOUBLE);
        IStateHistoryBackend bed = new InMemoryBackend(0);
        ssqd = new StateSystem(bed);
        mmpd.assignTargetStateSystem(ssqd);
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
            /* update for INTEGER test */
            mmpi.processEvent(mmpi.createEvent(time, null));
            mmpi.processEvent(mmpi.createEvent(time + 1000, 10L));
            mmpi.processEvent(mmpi.createEvent(time + 2000, null));
            mmpi.processEvent(mmpi.createEvent(time + 3000, 20L));
            mmpi.processEvent(mmpi.createEvent(time + 5000, null));
            mmpi.processEvent(mmpi.createEvent(time + 6000, 30L));
            mmpi.processEvent(mmpi.createEvent(time + 9000, null));
            /* update for DOUBLE test */
            mmpd.processEvent(mmpd.createEvent(time, null));
            mmpd.processEvent(mmpd.createEvent(time + 1000, 10L));
            mmpd.processEvent(mmpd.createEvent(time + 2000, null));
            mmpd.processEvent(mmpd.createEvent(time + 3000, 20L));
            mmpd.processEvent(mmpd.createEvent(time + 5000, null));
            mmpd.processEvent(mmpd.createEvent(time + 6000, 30L));
            mmpd.processEvent(mmpd.createEvent(time + 9000, null));
        }
        /* cleanup for INTEGER test */
        mmpi.processEvent(mmpi.createEvent(END_TIME, 0L));
        mmpi.dispose();
        ssqi.waitUntilBuilt();
        /* cleanup for DOUBLE test */
        mmpd.processEvent(mmpd.createEvent(END_TIME, 0L));
        mmpd.dispose();
        ssqd.waitUntilBuilt();
    }

    /**
     * Test a query range to the state system to get the maximum value in the
     * range. The test values are INTEGER.
     */
    @Test
    public void testQueryMipmapRangeMaxInteger() {
        assertNotNull(ssqi);
        try {
            int quark = ssqi.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(TmfStateValue.nullValue(), ssqi.queryRangeMax(0, 0, quark));
            assertEquals(10, ssqi.queryRangeMax(500, 1500, quark).unboxInt());
            assertEquals(20, ssqi.queryRangeMax(1500, 5000, quark).unboxInt());
            assertEquals(30, ssqi.queryRangeMax(5000, 10000, quark).unboxInt());
            assertEquals(30, ssqi.queryRangeMax(0, 10000, quark).unboxInt());
            assertEquals(TmfStateValue.nullValue(), ssqi.queryRangeMax(120000, 120000, quark));
            assertEquals(10, ssqi.queryRangeMax(120500, 121500, quark).unboxInt());
            assertEquals(20, ssqi.queryRangeMax(121500, 125000, quark).unboxInt());
            assertEquals(30, ssqi.queryRangeMax(125000, 130000, quark).unboxInt());
            assertEquals(30, ssqi.queryRangeMax(120000, 130000, quark).unboxInt());
            assertEquals(30, ssqi.queryRangeMax(100000, 150000, quark).unboxInt());
            assertEquals(30, ssqi.queryRangeMax(240000, 250000, quark).unboxInt());
            assertEquals(30, ssqi.queryRangeMax(0, 250000, quark).unboxInt());
            assertEquals(00, ssqi.queryRangeMax(250000, 250000, quark).unboxInt());
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the minimum value in the
     * range. The test values are INTEGER.
     */
    @Test
    public void testQueryMipmapRangeMinInteger() {
        assertNotNull(ssqi);
        try {
            int quark = ssqi.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(TmfStateValue.nullValue(), ssqi.queryRangeMin(0, 0, quark));
            assertEquals(10, ssqi.queryRangeMin(500, 1500, quark).unboxInt());
            assertEquals(10, ssqi.queryRangeMin(1500, 5000, quark).unboxInt());
            assertEquals(30, ssqi.queryRangeMin(5000, 10000, quark).unboxInt());
            assertEquals(10, ssqi.queryRangeMin(0, 10000, quark).unboxInt());
            assertEquals(TmfStateValue.nullValue(), ssqi.queryRangeMin(120000, 120000, quark));
            assertEquals(10, ssqi.queryRangeMin(120500, 121500, quark).unboxInt());
            assertEquals(10, ssqi.queryRangeMin(121500, 125000, quark).unboxInt());
            assertEquals(30, ssqi.queryRangeMin(125000, 130000, quark).unboxInt());
            assertEquals(10, ssqi.queryRangeMin(120000, 130000, quark).unboxInt());
            assertEquals(10, ssqi.queryRangeMin(100000, 150000, quark).unboxInt());
            assertEquals(00, ssqi.queryRangeMin(240000, 250000, quark).unboxInt());
            assertEquals(00, ssqi.queryRangeMin(0, 250000, quark).unboxInt());
            assertEquals(00, ssqi.queryRangeMin(250000, 250000, quark).unboxInt());
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the average value in the
     * range. The test values are INTEGER.
     */
    @Test
    public void testQueryMipmapRangeAvgInteger() {
        assertNotNull(ssqi);
        try {
            int quark = ssqi.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(0.0, ssqi.queryRangeAverage(0, 0, quark), DELTA);
            assertEquals(5.0, ssqi.queryRangeAverage(500, 1500, quark), DELTA);
            assertEquals(90.0 / 7, ssqi.queryRangeAverage(1500, 5000, quark), DELTA);
            assertEquals(90.0 / 5, ssqi.queryRangeAverage(5000, 10000, quark), DELTA);
            assertEquals(14.0, ssqi.queryRangeAverage(0, 10000, quark), DELTA);
            assertEquals(14.0, ssqi.queryRangeAverage(0, 20000, quark), DELTA);
            assertEquals(14.0, ssqi.queryRangeAverage(500, 20500, quark), DELTA);
            assertEquals(14.0, ssqi.queryRangeAverage(1000, 21000, quark), DELTA);
            assertEquals(14.0, ssqi.queryRangeAverage(2000, 22000, quark), DELTA);
            assertEquals(14.0, ssqi.queryRangeAverage(3000, 23000, quark), DELTA);
            assertEquals(14.0, ssqi.queryRangeAverage(4000, 24000, quark), DELTA);
            assertEquals(14.0, ssqi.queryRangeAverage(5000, 25000, quark), DELTA);
            assertEquals(0.0, ssqi.queryRangeAverage(120000, 120000, quark), DELTA);
            assertEquals(5.0, ssqi.queryRangeAverage(120500, 121500, quark), DELTA);
            assertEquals(90.0 / 7, ssqi.queryRangeAverage(121500, 125000, quark), DELTA);
            assertEquals(90.0 / 5, ssqi.queryRangeAverage(125000, 130000, quark), DELTA);
            assertEquals(14.0, ssqi.queryRangeAverage(120000, 130000, quark), DELTA);
            assertEquals(14.0, ssqi.queryRangeAverage(100000, 150000, quark), DELTA);
            assertEquals(14.0, ssqi.queryRangeAverage(240000, 250000, quark), DELTA);
            assertEquals(14.0, ssqi.queryRangeAverage(0, 250000, quark), DELTA);
            assertEquals(0.0, ssqi.queryRangeAverage(250000, 250000, quark), DELTA);
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the maximum value in the
     * range. The test values are DOUBLE.
     */
    @Test
    public void testQueryMipmapRangeMaxDouble() {
        assertNotNull(ssqd);
        try {
            int quark = ssqd.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(TmfStateValue.nullValue(), ssqd.queryRangeMax(0, 0, quark));
            assertEquals(10.0, ssqd.queryRangeMax(500, 1500, quark).unboxDouble(), DELTA);
            assertEquals(20.0, ssqd.queryRangeMax(1500, 5000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, ssqd.queryRangeMax(5000, 10000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, ssqd.queryRangeMax(0, 10000, quark).unboxDouble(), DELTA);
            assertEquals(TmfStateValue.nullValue(), ssqd.queryRangeMax(120000, 120000, quark));
            assertEquals(10.0, ssqd.queryRangeMax(120500, 121500, quark).unboxDouble(), DELTA);
            assertEquals(20.0, ssqd.queryRangeMax(121500, 125000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, ssqd.queryRangeMax(125000, 130000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, ssqd.queryRangeMax(120000, 130000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, ssqd.queryRangeMax(100000, 150000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, ssqd.queryRangeMax(240000, 250000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, ssqd.queryRangeMax(0, 250000, quark).unboxDouble(), DELTA);
            assertEquals(00.0, ssqd.queryRangeMax(250000, 250000, quark).unboxDouble(), DELTA);
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the minimum value in the
     * range. The test values are DOUBLE.
     */
    @Test
    public void testQueryMipmapRangeMinDouble() {
        assertNotNull(ssqd);
        try {
            int quark = ssqd.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(TmfStateValue.nullValue(), ssqd.queryRangeMin(0, 0, quark));
            assertEquals(10.0, ssqd.queryRangeMin(500, 1500, quark).unboxDouble(), DELTA);
            assertEquals(10.0, ssqd.queryRangeMin(1500, 5000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, ssqd.queryRangeMin(5000, 10000, quark).unboxDouble(), DELTA);
            assertEquals(10.0, ssqd.queryRangeMin(0, 10000, quark).unboxDouble(), DELTA);
            assertEquals(TmfStateValue.nullValue(), ssqd.queryRangeMin(120000, 120000, quark));
            assertEquals(10.0, ssqd.queryRangeMin(120500, 121500, quark).unboxDouble(), DELTA);
            assertEquals(10.0, ssqd.queryRangeMin(121500, 125000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, ssqd.queryRangeMin(125000, 130000, quark).unboxDouble(), DELTA);
            assertEquals(10.0, ssqd.queryRangeMin(120000, 130000, quark).unboxDouble(), DELTA);
            assertEquals(10.0, ssqd.queryRangeMin(100000, 150000, quark).unboxDouble(), DELTA);
            assertEquals(00.0, ssqd.queryRangeMin(240000, 250000, quark).unboxDouble(), DELTA);
            assertEquals(00.0, ssqd.queryRangeMin(0, 250000, quark).unboxDouble(), DELTA);
            assertEquals(00.0, ssqd.queryRangeMin(250000, 250000, quark).unboxDouble(), DELTA);
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the average value in the
     * range. The test values are DOUBLE.
     */
    @Test
    public void testQueryMipmapRangeAvgDouble() {
        assertNotNull(ssqd);
        try {
            int quark = ssqd.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(0.0, ssqd.queryRangeAverage(0, 0, quark), DELTA);
            assertEquals(5.0, ssqd.queryRangeAverage(500, 1500, quark), DELTA);
            assertEquals(90.0 / 7, ssqd.queryRangeAverage(1500, 5000, quark), DELTA);
            assertEquals(90.0 / 5, ssqd.queryRangeAverage(5000, 10000, quark), DELTA);
            assertEquals(14.0, ssqd.queryRangeAverage(0, 10000, quark), DELTA);
            assertEquals(14.0, ssqd.queryRangeAverage(0, 20000, quark), DELTA);
            assertEquals(14.0, ssqd.queryRangeAverage(500, 20500, quark), DELTA);
            assertEquals(14.0, ssqd.queryRangeAverage(1000, 21000, quark), DELTA);
            assertEquals(14.0, ssqd.queryRangeAverage(2000, 22000, quark), DELTA);
            assertEquals(14.0, ssqd.queryRangeAverage(3000, 23000, quark), DELTA);
            assertEquals(14.0, ssqd.queryRangeAverage(4000, 24000, quark), DELTA);
            assertEquals(14.0, ssqd.queryRangeAverage(5000, 25000, quark), DELTA);
            assertEquals(0.0, ssqd.queryRangeAverage(120000, 120000, quark), DELTA);
            assertEquals(5.0, ssqd.queryRangeAverage(120500, 121500, quark), DELTA);
            assertEquals(90.0 / 7, ssqd.queryRangeAverage(121500, 125000, quark), DELTA);
            assertEquals(90.0 / 5, ssqd.queryRangeAverage(125000, 130000, quark), DELTA);
            assertEquals(14.0, ssqd.queryRangeAverage(120000, 130000, quark), DELTA);
            assertEquals(14.0, ssqd.queryRangeAverage(100000, 150000, quark), DELTA);
            assertEquals(14.0, ssqd.queryRangeAverage(240000, 250000, quark), DELTA);
            assertEquals(14.0, ssqd.queryRangeAverage(0, 250000, quark), DELTA);
            assertEquals(0.0, ssqd.queryRangeAverage(250000, 250000, quark), DELTA);
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }
    }
}
