/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Christian Kouamé - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
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
import org.eclipse.linuxtools.tmf.core.statesystem.MipMapProvider;
import org.eclipse.linuxtools.tmf.core.util.Pair;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jean-Christian Kouamé
 *
 */
public class CtfMipMapProviderTest {
    private static StateSystem ssq;
    private String fEventName = "test_attribute";
    private final long maxLevel = 5;
    private static final long END_TIME = 100000000L;
    private static final long START_TIME = 0L;
    private static final int RANGE = 16;
    static final long INTERSTINGTIMESTAMP = 12345000L;

    /**
     * Startup code, build a state system with n attributes always going up
     * linearly
     */
    @BeforeClass
    public static void init() {
        CtfMipMapProviderForTest ctfmmp = new CtfMipMapProviderForTest(null, RANGE);
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
            mipmapQuark = ssq.getQuarkAbsolute(MipMapProvider.MIPMAP, fEventName);
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

            int LevelMaxQuark = ssq.getQuarkAbsolute(MipMapProvider.MIPMAP, fEventName, MipMapProvider.NB_LEVELS);
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

            list1 = ssq.queryFullState(INTERSTINGTIMESTAMP);
            eventFieldQuark = ssq.getQuarkAbsolute(MipMapProvider.MIPMAP, fEventName);
            interval = list1.get(eventFieldQuark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(INTERSTINGTIMESTAMP / 1000L, valueLong);
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

            list1 = ssq.queryFullState(INTERSTINGTIMESTAMP);
            mipmapQuark = ssq.getQuarkAbsolute(MipMapProvider.MIPMAP, fEventName, MipMapProvider.NB_LEVELS);
            quark = ssq.getQuarkRelative(mipmapQuark, MipMapProvider.LEVEL + String.valueOf(level), MipMapProvider.MIN);
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(((INTERSTINGTIMESTAMP / 1000) / 256) * 256, valueLong);
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

            list1 = ssq.queryFullState(INTERSTINGTIMESTAMP);
            mipmapQuark = ssq.getQuarkAbsolute(MipMapProvider.MIPMAP, fEventName, MipMapProvider.NB_LEVELS);
            quark = ssq.getQuarkRelative(mipmapQuark, MipMapProvider.LEVEL + String.valueOf(level), MipMapProvider.MAX);
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals((((INTERSTINGTIMESTAMP / 1000L) / 256) * 256) + 255, valueLong);
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

            list1 = ssq.queryFullState(INTERSTINGTIMESTAMP);
            mipmapQuark = ssq.getQuarkAbsolute(MipMapProvider.MIPMAP, fEventName, MipMapProvider.NB_LEVELS);
            quark = ssq.getQuarkRelative(mipmapQuark, MipMapProvider.LEVEL + String.valueOf(level), MipMapProvider.AVRG);
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals((((INTERSTINGTIMESTAMP / 1000L) / 256) * 256) + 127, valueLong);
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
            time = INTERSTINGTIMESTAMP;
            level = 2;

            list1 = ssq.queryFullState(time);
            mipmapQuark = ssq.getQuarkAbsolute(MipMapProvider.MIPMAP, fEventName, MipMapProvider.NB_LEVELS);
            quark = ssq.getQuarkRelative(mipmapQuark, MipMapProvider.LEVEL + String.valueOf(level), MipMapProvider.MAX);
            interval = list1.get(quark);
            max = interval.getStateValue().unboxLong();
            assertEquals((((INTERSTINGTIMESTAMP / 1000L) / 256) * 256) + 255, max);

            quark = ssq.getQuarkRelative(mipmapQuark, MipMapProvider.LEVEL + String.valueOf(level), MipMapProvider.MIN);
            interval = list1.get(quark);
            min = interval.getStateValue().unboxLong();
            assertEquals((((INTERSTINGTIMESTAMP / 1000L) / 256) * 256), min);

            quark = ssq.getQuarkRelative(mipmapQuark, MipMapProvider.LEVEL + String.valueOf(level), MipMapProvider.AVRG);
            interval = list1.get(quark);
            avg = interval.getStateValue().unboxLong();

            assertEquals((((INTERSTINGTIMESTAMP / 1000L) / 256) * 256) + 127, avg);
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
            int quark, fieldQuark, maxLevelQuark;
            long valueLong, fieldValue;

            list1 = ssq.queryFullState(START_TIME);

            fieldQuark = ssq.getQuarkAbsolute(MipMapProvider.MIPMAP, fEventName);
            interval = list1.get(fieldQuark);
            fieldValue = interval.getStateValue().unboxLong();
            assertEquals(0L, fieldValue);

            maxLevelQuark = ssq.getQuarkRelative(fieldQuark, MipMapProvider.NB_LEVELS);
            interval = list1.get(maxLevelQuark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(maxLevel, valueLong);

            quark = ssq.getQuarkRelative(maxLevelQuark, MipMapProvider.LEVEL + String.valueOf(1L), MipMapProvider.MIN);
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(0L, valueLong);

            quark = ssq.getQuarkRelative(maxLevelQuark, MipMapProvider.LEVEL + String.valueOf(1L), MipMapProvider.MAX);
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(RANGE - 1L, valueLong);

            quark = ssq.getQuarkRelative(maxLevelQuark, MipMapProvider.LEVEL + String.valueOf(maxLevel), MipMapProvider.MIN);
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(0L, valueLong);

            quark = ssq.getQuarkRelative(maxLevelQuark, MipMapProvider.LEVEL + String.valueOf(maxLevel), MipMapProvider.MAX);
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
            int quark, fieldQuark, maxLevelQuark;
            long valueLong, fieldValue;

            list1 = ssq.queryFullState(END_TIME);

            fieldQuark = ssq.getQuarkAbsolute(MipMapProvider.MIPMAP, fEventName);
            interval = list1.get(fieldQuark);
            fieldValue = interval.getStateValue().unboxLong();
            assertEquals(END_TIME / 1000L, fieldValue);

            maxLevelQuark = ssq.getQuarkRelative(fieldQuark, MipMapProvider.NB_LEVELS);
            interval = list1.get(maxLevelQuark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(maxLevel, valueLong);

            quark = ssq.getQuarkRelative(maxLevelQuark, MipMapProvider.LEVEL + String.valueOf(1L), MipMapProvider.MIN);
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(((END_TIME / 1000L / 16) * 16), valueLong);

            quark = ssq.getQuarkRelative(maxLevelQuark, MipMapProvider.LEVEL + String.valueOf(1L), MipMapProvider.MAX);
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals((END_TIME / 1000L), valueLong);

            quark = ssq.getQuarkRelative(maxLevelQuark, MipMapProvider.LEVEL + String.valueOf(maxLevel), MipMapProvider.MIN);
            interval = list1.get(quark);
            valueLong = interval.getStateValue().unboxLong();
            assertEquals(0L, valueLong);

            quark = ssq.getQuarkRelative(maxLevelQuark, MipMapProvider.LEVEL + String.valueOf(maxLevel), MipMapProvider.MAX);
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
        long max = queryRangeMipmapMax(START_TIME, END_TIME / 2);
        assertEquals(((END_TIME / 2) / 1000L), max);
        assertTrue(true);
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
        long min = queryRangeMipmapMin(START_TIME, END_TIME / 2);
        assertEquals((START_TIME / 1000L), min);
        assertTrue(true);
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
        long avg = queryRangeMipmapAverage(START_TIME, END_TIME / 2);
        assertEquals(((END_TIME / 2) - START_TIME) / 2 / 1000, avg);
        assertTrue(true);
    }

    long queryRangeMipmapMax(long t1, long t2) {
        long max = -1;
        try {
            List<ITmfStateInterval> intervals = queryMipmapAttribute(t1, t2, MipMapProvider.MAX);
            for (ITmfStateInterval si : intervals) {
                max = Math.max(max, si.getStateValue().unboxLong());
            }
        } catch (StateValueTypeException e) {
        }
        return max;
    }

    long queryRangeMipmapMin(long t1, long t2) {
        long min = -1;
        try {
            List<ITmfStateInterval> intervals = queryMipmapAttribute(t1, t2, MipMapProvider.MIN);
            min = intervals.get(0).getStateValue().unboxLong();
            for (ITmfStateInterval si : intervals) {
                min = Math.min(min, si.getStateValue().unboxLong());
            }
        } catch (StateValueTypeException e) {
        }
        return min;
    }

    long queryRangeMipmapAverage(long t1, long t2) {
        double avg = 0;
        try {
            List<ITmfStateInterval> intervals = queryMipmapAttribute(t1, t2, MipMapProvider.AVRG);
            for (ITmfStateInterval si : intervals) {
                long delta = si.getEndTime() - si.getStartTime() + 1;
                avg += si.getStateValue().unboxLong() * ((double) delta / (double) (t2 - t1 + 1));
            }
        } catch (StateValueTypeException e) {
        }
        return (long) Math.ceil(avg);
    }

    List<ITmfStateInterval> queryMipmapAttribute(long t1, long t2, String attributeType) {
        Pair<Long, Long> timeRange = new Pair<Long, Long>(t1, t2);
        int attributeQuark, maxLevelQuark;
        List<ITmfStateInterval> intervals = new ArrayList<ITmfStateInterval>();
        try {
            attributeQuark = ssq.getQuarkAbsolute(MipMapProvider.MIPMAP, fEventName);
            maxLevelQuark = ssq.getQuarkRelative(attributeQuark, MipMapProvider.NB_LEVELS);
            ITmfStateInterval maxLevelInterval = ssq.querySingleState(timeRange.getSecond(), maxLevelQuark);
            long levelMax = maxLevelInterval.getStateValue().unboxLong();
            queryRangeMipmapAttribute(0L, levelMax, attributeQuark, maxLevelQuark, timeRange, intervals, attributeType);
        } catch (AttributeNotFoundException e) {
        } catch (TimeRangeException e) {
        } catch (StateSystemDisposedException e) {
        } catch (StateValueTypeException e) {
        }
        return intervals;
    }

    void queryRangeMipmapAttribute(long currentLevel, long levelMax, int attributeQuark, int maxLevelQuark, Pair<Long, Long> timeRange, List<ITmfStateInterval> intervals, String attributeType) {
        long level = currentLevel;
        Pair<Long, Long> range = timeRange;
        ITmfStateInterval currentLevelInterval = null, nextLevelInterval = null;
        if (range == null || range.getFirst() > range.getSecond()) {
            return;
        }
        if (level > levelMax || level < 0) {
            return;
        }
        try {
            if (range.getFirst().longValue() == range.getSecond().longValue()) {
                level = 0;
                currentLevelInterval = ssq.querySingleState(range.getFirst(), attributeQuark);
                intervals.add(currentLevelInterval);
                range = null;
                return;
            }
            if (level == 0) {
                currentLevelInterval = ssq.querySingleState(range.getFirst(), attributeQuark);
            } else {
                int quark = ssq.getQuarkRelative(maxLevelQuark, MipMapProvider.LEVEL + String.valueOf(level), attributeType);
                currentLevelInterval = ssq.querySingleState(range.getFirst(), quark);
            }

            if (level < levelMax) {
                int quark = ssq.getQuarkRelative(maxLevelQuark, MipMapProvider.LEVEL + String.valueOf(level + 1), attributeType);
                nextLevelInterval = ssq.querySingleState(range.getFirst(), quark);
            }

            if (nextLevelInterval != null && isFullyOverlapped(range.getFirst(), range.getSecond(), nextLevelInterval.getStartTime(), nextLevelInterval.getEndTime())) {
                level++;
            } else if (currentLevelInterval != null && isFullyOverlapped(range.getFirst(), range.getSecond(), currentLevelInterval.getStartTime(), currentLevelInterval.getEndTime())) {
                intervals.add(currentLevelInterval);
                range = updateTimeRange(range, currentLevelInterval);
            } else {
                if (level == 0) {
                    intervals.add(currentLevelInterval);
                    updateTimeRange(range, currentLevelInterval);
                } else {
                    level--;
                }
            }
            queryRangeMipmapAttribute(level, levelMax, attributeQuark, maxLevelQuark, range, intervals, attributeType);
        } catch (AttributeNotFoundException e) {
        } catch (TimeRangeException e) {
        } catch (StateSystemDisposedException e) {
        }
    }

    Pair<Long, Long> updateTimeRange(Pair<Long, Long> timeRange, ITmfStateInterval currentLevelInterval) {
        if (timeRange.getFirst() >= timeRange.getSecond()) {
            return null;
        }
        if (timeRange.getFirst() != currentLevelInterval.getStartTime()) {
            return null;
        }
        if (timeRange.getSecond() <= currentLevelInterval.getEndTime()) {
            return null;
        }
        return new Pair<Long, Long>(currentLevelInterval.getEndTime() + 1L, timeRange.getSecond());
    }

    static boolean isFullyOverlapped(long t1, long t2, long startTime, long endTime) {
        if (t1 >= t2 || startTime >= endTime) {
            return false;
        }
        if (t1 <= startTime && t2 >= endTime) {
            return true;
        }
        return false;
    }
}
