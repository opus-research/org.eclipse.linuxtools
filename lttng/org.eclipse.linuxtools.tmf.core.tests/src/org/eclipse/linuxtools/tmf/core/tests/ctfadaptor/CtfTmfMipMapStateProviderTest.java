/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Christian KouamÃ© - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Random;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.StateSystem;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.InMemoryBackend;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfMipMapStateProvider;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jean-Christian KouamÃ©
 *
 */
public class CtfTmfMipMapStateProviderTest {
    private static StateSystem ssq;
    private String fEventName = "test_attribute";
    private final long maxLevel = 5;
    private static final long END_TIME = 100000000L;
    private static final long START_TIME = 0L;
    private static final int RESOLUTION = 16;
    static final long INTERESTINGTIMESTAMP = 12345000L;

    /**
     * Startup code, build a state system with n attributes always going up
     * linearly
     */
    @BeforeClass
    public static void init() {
        CtfTmfMipMapStateProviderForTest ctfmmp = new CtfTmfMipMapStateProviderForTest(null, new int[] {RESOLUTION, RESOLUTION, RESOLUTION});
        IStateHistoryBackend be = new InMemoryBackend(0);
        ssq = new StateSystem(be);
        ctfmmp.assignTargetStateSystem(ssq);

        for (long rank = 0L; rank <= END_TIME / 1000; rank++) {
            long time = rank * 1000L;
            CtfTmfEvent event = CtfTmfEventFactoryForTest.createEvent(time, rank);
            ctfmmp.processEvent(event);
        }
        ctfmmp.dispose();
        ssq.waitUntilBuilt();
    }

    /**
     * Test a single query to the state system.
     *
     * Make sure the state system has data.
     *
     * Hint: the value read should always be t / 1000
     *
     */
    @Test
    public void testQuery() {
        assertNotNull(ssq);
        try {
            List<ITmfStateInterval> list1;
            ITmfStateInterval interval;
            int mipmapQuark;
            long valueLong, time;
            Random rn = new Random();
            long r = rn.nextInt(Integer.MAX_VALUE);
            time = (r % END_TIME);
            list1 = ssq.queryFullState(time);
            mipmapQuark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName);
            interval = list1.get(mipmapQuark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(time / 1000L, valueLong);

        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateValueTypeException e) {
            fail();
        }
        assertTrue(true);
    }

    /**
     * Test a single query to the state system for the maxLevel.
     *
     * Make sure the state system has data.
     *
     */
    @Test
    public void testMaxLevel() {
        assertNotNull(ssq);
        try {
            List<ITmfStateInterval> list1;
            ITmfStateInterval interval;
            long valueLong, time;
            Random rn = new Random();
            time = rn.nextInt(Integer.MAX_VALUE) % END_TIME;
            list1 = ssq.queryFullState(time);

            int LevelMaxQuark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName, AbstractTmfMipMapStateProvider.getMimapNbLevelString(AbstractTmfMipMapStateProvider.MAX_STRING));
            interval = list1.get(LevelMaxQuark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(maxLevel, valueLong);

        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateValueTypeException e) {
            fail();
        }
        assertTrue(true);

    }

    /**
     * Test a single query to the state system for a mip
     *
     * Make sure the state system has data.
     *
     */
    @Test
    public void testQueryEventField() {
        assertNotNull(ssq);
        try {
            List<ITmfStateInterval> list1;
            ITmfStateInterval interval;
            int eventFieldQuark;
            long valueLong;

            list1 = ssq.queryFullState(INTERESTINGTIMESTAMP);
            eventFieldQuark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName);
            interval = list1.get(eventFieldQuark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(INTERESTINGTIMESTAMP / 1000L, valueLong);
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateValueTypeException e) {
            fail();
        }
        assertTrue(true);
    }

    /**
     * Test a single query to the state system for a min
     *
     * Make sure the state system has data.
     *
     * Hint: the value read should always be less than(t / 1000)
     */
    @Test
    public void testQueryMipMin() {
        assertNotNull(ssq);
        try {
            List<ITmfStateInterval> list1;
            ITmfStateInterval interval;
            int quark, mipmapQuark, level;
            long valueLong;
            level = 2;

            list1 = ssq.queryFullState(INTERESTINGTIMESTAMP);
            mipmapQuark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName, AbstractTmfMipMapStateProvider.getMimapNbLevelString(AbstractTmfMipMapStateProvider.MIN_STRING));
            quark = ssq.getQuarkRelative(mipmapQuark, AbstractTmfMipMapStateProvider.MIN_STRING + String.valueOf(level));
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(((INTERESTINGTIMESTAMP / 1000) / 256) * 256, valueLong);
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateValueTypeException e) {
            fail();
        }
        assertTrue(true);
    }

    /**
     * Test a single query to the state system for a max
     *
     * Make sure the state system has data.
     *
     * Hint: the value read should always be greater than(t / 1000)
     *
     */
    @Test
    public void testQueryMipMax() {
        assertNotNull(ssq);
        try {
            List<ITmfStateInterval> list1;
            ITmfStateInterval interval;
            int quark, mipmapQuark, level;
            long valueLong;
            level = 2;

            list1 = ssq.queryFullState(INTERESTINGTIMESTAMP);
            mipmapQuark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName, AbstractTmfMipMapStateProvider.getMimapNbLevelString(AbstractTmfMipMapStateProvider.MAX_STRING));
            quark = ssq.getQuarkRelative(mipmapQuark, AbstractTmfMipMapStateProvider.MAX_STRING + String.valueOf(level));
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals((((INTERESTINGTIMESTAMP / 1000L) / 256) * 256) + 255, valueLong);
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateValueTypeException e) {
            fail();
        }
        assertTrue(true);
    }

    /**
     * Test a single query to the state system for an average
     *
     * Make sure the state system has data.
     *
     * Hint: the value read should always be more or less(t / 1000)
     *
     */
    @Test
    public void testQueryMipAvg() {
        assertNotNull(ssq);
        try {
            List<ITmfStateInterval> list1;
            ITmfStateInterval interval;
            int quark, mipmapQuark, level;
            long valueLong;
            level = 2;

            list1 = ssq.queryFullState(INTERESTINGTIMESTAMP);
            mipmapQuark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName, AbstractTmfMipMapStateProvider.getMimapNbLevelString(AbstractTmfMipMapStateProvider.AVG_STRING));
            quark = ssq.getQuarkRelative(mipmapQuark, AbstractTmfMipMapStateProvider.AVG_STRING + String.valueOf(level));
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals((((INTERESTINGTIMESTAMP / 1000L) / 256) * 256) + 127, valueLong);
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateValueTypeException e) {
            fail();
        }
        assertTrue(true);
    }

    /**
     * Test a single query to the state system for a min, a max and an average
     *
     * Make sure the state system has data.
     *
     * Hint: the value read should always be about the average and about min +
     * max /2, which should be about the average also.
     *
     */
    @Test
    public void testQueryMipMinMaxAvg() {
        assertNotNull(ssq);
        try {
            List<ITmfStateInterval> list1;
            ITmfStateInterval interval;
            int quark, mipmapQuark, level;
            long min, max, avg, time;
            time = INTERESTINGTIMESTAMP;
            level = 2;

            list1 = ssq.queryFullState(time);
            mipmapQuark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName, AbstractTmfMipMapStateProvider.getMimapNbLevelString(AbstractTmfMipMapStateProvider.MAX_STRING));
            quark = ssq.getQuarkRelative(mipmapQuark, AbstractTmfMipMapStateProvider.MAX_STRING + String.valueOf(level));
            interval = list1.get(quark);
            max = interval.getStateValue().unboxLong();
            assertEquals((((INTERESTINGTIMESTAMP / 1000L) / 256) * 256) + 255, max);

            mipmapQuark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName, AbstractTmfMipMapStateProvider.getMimapNbLevelString(AbstractTmfMipMapStateProvider.MIN_STRING));
            quark = ssq.getQuarkRelative(mipmapQuark, AbstractTmfMipMapStateProvider.MIN_STRING + String.valueOf(level));
            interval = list1.get(quark);
            min = interval.getStateValue().unboxLong();
            assertEquals((((INTERESTINGTIMESTAMP / 1000L) / 256) * 256), min);

            mipmapQuark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName, AbstractTmfMipMapStateProvider.getMimapNbLevelString(AbstractTmfMipMapStateProvider.AVG_STRING));
            quark = ssq.getQuarkRelative(mipmapQuark, AbstractTmfMipMapStateProvider.AVG_STRING + String.valueOf(level));
            interval = list1.get(quark);
            avg = interval.getStateValue().unboxLong();

            assertEquals((((INTERESTINGTIMESTAMP / 1000L) / 256) * 256) + 127, avg);
            assertTrue(max >= min);
            assertTrue(max >= avg);
            assertTrue(avg >= min);
            assertTrue((max + min) / 2 == avg);

        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateValueTypeException e) {
            fail();
        }
        assertTrue(true);
    }

    /**
     * Test a full query to the state system at the startTime
     *
     * Make sure the state system has data.
     *
     * Hint: the value read should always be more or less(t / 1000)
     *
     */
    @Test
    public void testQueryValuesOnStart() {
        assertNotNull(ssq);
        try {
            List<ITmfStateInterval> list1;
            ITmfStateInterval interval;
            int quark, fieldQuark, maxLevelQuark, minLevelQuark;
            long valueLong, fieldValue;

            list1 = ssq.queryFullState(START_TIME);

            fieldQuark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName);
            interval = list1.get(fieldQuark);
            fieldValue = interval.getStateValue().unboxLong();
            assertEquals(0L, fieldValue);

            maxLevelQuark = ssq.getQuarkRelative(fieldQuark, AbstractTmfMipMapStateProvider.getMimapNbLevelString(AbstractTmfMipMapStateProvider.MAX_STRING));
            interval = list1.get(maxLevelQuark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(maxLevel, valueLong);

            minLevelQuark = ssq.getQuarkRelative(fieldQuark, AbstractTmfMipMapStateProvider.getMimapNbLevelString(AbstractTmfMipMapStateProvider.MIN_STRING));
            quark = ssq.getQuarkRelative(minLevelQuark, AbstractTmfMipMapStateProvider.MIN_STRING + String.valueOf(1L));
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(0L, valueLong);

            quark = ssq.getQuarkRelative(maxLevelQuark, AbstractTmfMipMapStateProvider.MAX_STRING + String.valueOf(1L));
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(RESOLUTION - 1L, valueLong);

            quark = ssq.getQuarkRelative(minLevelQuark, AbstractTmfMipMapStateProvider.MIN_STRING + String.valueOf(maxLevel));
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(0L, valueLong);

            quark = ssq.getQuarkRelative(maxLevelQuark, AbstractTmfMipMapStateProvider.MAX_STRING + String.valueOf(maxLevel));
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals((END_TIME / 1000L), valueLong);
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateValueTypeException e) {
            fail();
        }
        assertTrue(true);
    }

    /**
     * Test a full query to the state system when the end time
     *
     * Make sure the state system has data.
     *
     * Hint: the value read should always be more or less(t / 1000)
     *
     */
    @Test
    public void testQueryValuesOnClose() {
        assertNotNull(ssq);
        try {
            List<ITmfStateInterval> list1;
            ITmfStateInterval interval;
            int quark, fieldQuark, maxLevelQuark, minLevelQuark;
            long valueLong, fieldValue;

            list1 = ssq.queryFullState(END_TIME);

            fieldQuark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName);
            interval = list1.get(fieldQuark);
            fieldValue = interval.getStateValue().unboxLong();
            assertEquals(END_TIME / 1000L, fieldValue);

            maxLevelQuark = ssq.getQuarkRelative(fieldQuark, AbstractTmfMipMapStateProvider.getMimapNbLevelString(AbstractTmfMipMapStateProvider.MAX_STRING));
            interval = list1.get(maxLevelQuark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(maxLevel, valueLong);

            minLevelQuark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName, AbstractTmfMipMapStateProvider.getMimapNbLevelString(AbstractTmfMipMapStateProvider.MIN_STRING));
            quark = ssq.getQuarkRelative(minLevelQuark, AbstractTmfMipMapStateProvider.MIN_STRING + String.valueOf(1L));
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(((END_TIME / 1000L / 16) * 16), valueLong);

            quark = ssq.getQuarkRelative(maxLevelQuark, AbstractTmfMipMapStateProvider.MAX_STRING + String.valueOf(1L));
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals((END_TIME / 1000L), valueLong);

            quark = ssq.getQuarkRelative(minLevelQuark, AbstractTmfMipMapStateProvider.MIN_STRING + String.valueOf(maxLevel));
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(0L, valueLong);

            quark = ssq.getQuarkRelative(maxLevelQuark, AbstractTmfMipMapStateProvider.MAX_STRING + String.valueOf(maxLevel));
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals((END_TIME / 1000L), valueLong);
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateValueTypeException e) {
            fail();
        }
        assertTrue(true);
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
        int quark;
        try {
            quark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName);
            long max = ssq.queryRangeMipmapMax(START_TIME, END_TIME / 2, quark);
            assertEquals(((END_TIME / 2) / 1000L), max);
            assertTrue(true);
        } catch (AttributeNotFoundException e) {
            fail();
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
        int quark;
        try {
            quark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName);
            long min = ssq.queryRangeMipmapMin(START_TIME, END_TIME / 2, quark);
            assertEquals((START_TIME / 1000L), min);
            assertTrue(true);
        } catch (AttributeNotFoundException e) {
            fail();
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
        int quark;
        try {
            quark = ssq.getQuarkAbsolute(AbstractTmfMipMapStateProvider.MIPMAP, fEventName);
            long avg = ssq.queryRangeMipmapAverage(START_TIME, END_TIME / 2, quark);
            assertEquals(((END_TIME / 2) - START_TIME) / 2 / 1000, avg);
            assertTrue(true);
        } catch (AttributeNotFoundException e) {
            fail();
        }
    }
}
