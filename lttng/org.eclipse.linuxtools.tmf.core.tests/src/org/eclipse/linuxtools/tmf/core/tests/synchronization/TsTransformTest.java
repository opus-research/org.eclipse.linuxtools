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

package org.eclipse.linuxtools.tmf.core.tests.synchronization;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.core.synchronization.TmfTimestampTransform;
import org.eclipse.linuxtools.internal.tmf.core.synchronization.TmfTimestampTransformLinear;
import org.eclipse.linuxtools.internal.tmf.core.synchronization.TmfTimestampTransformLinearFast;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.junit.Test;

/**
 * Tests for {@link TmfTimestampTransform} and its descendants
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("nls")
public class TsTransformTest {

    private long ts = 1361657893526374091L;
    private ITmfTimestamp oTs = new TmfTimestamp(ts);

    /**
     * Test the linear transform
     */
    @Test
    public void testLinearTransform() {
        /* Default constructor */
        TmfTimestampTransformLinear ttl = new TmfTimestampTransformLinear();
        assertEquals(1361657893526374091L, ttl.transform(ts));
        assertEquals(1361657893526374091L, ttl.transform(oTs).getValue());

        /* Just an offset */
        ttl = new TmfTimestampTransformLinear(BigDecimal.valueOf(1.0), BigDecimal.valueOf(3));
        assertEquals(1361657893526374094L, ttl.transform(ts));
        assertEquals(1361657893526374094L, ttl.transform(oTs).getValue());

        /* Just a slope */
        ttl = new TmfTimestampTransformLinear(BigDecimal.valueOf(2.0), BigDecimal.valueOf(0));
        assertEquals(2723315787052748182L, ttl.transform(ts));
        assertEquals(2723315787052748182L, ttl.transform(oTs).getValue());

        /* Offset and slope */
        ttl = new TmfTimestampTransformLinear(BigDecimal.valueOf(2.0), BigDecimal.valueOf(3));
        assertEquals(2723315787052748185L, ttl.transform(ts));
        assertEquals(2723315787052748185L, ttl.transform(oTs).getValue());

        /* Offset and slope */
        ttl = new TmfTimestampTransformLinear(BigDecimal.valueOf(0.5), BigDecimal.valueOf(0));
        assertEquals(680828946763187045L, ttl.transform(ts));
        assertEquals(680828946763187045L, ttl.transform(oTs).getValue());
    }

    /**
     * Test for the identity transform
     */
    @Test
    public void testIdentityTransform() {
        ITmfTimestampTransform tt = TmfTimestampTransform.IDENTITY;
        assertEquals(ts, tt.transform(ts));
        assertEquals(oTs, tt.transform(oTs));
    }

    /**
     * Test hash and equals function
     */
    @Test
    public void testEquality() {
        Map<ITmfTimestampTransform, String> map = new HashMap<>();
        ITmfTimestampTransform ttl = new TmfTimestampTransformLinear(BigDecimal.valueOf(2.0), BigDecimal.valueOf(3));
        ITmfTimestampTransform ttl2 = new TmfTimestampTransformLinear(BigDecimal.valueOf(2.0), BigDecimal.valueOf(3));
        ITmfTimestampTransform ttl3 = new TmfTimestampTransformLinear(BigDecimal.valueOf(3), BigDecimal.valueOf(3));
        assertEquals(ttl, ttl2);
        assertFalse(ttl.equals(ttl3));
        assertFalse(ttl2.equals(ttl3));

        map.put(ttl, "a");
        assertTrue(map.containsKey(ttl2));
        assertEquals("a", map.get(ttl));

        ITmfTimestampTransform ti = TmfTimestampTransform.IDENTITY;
        assertEquals(TmfTimestampTransform.IDENTITY, ti);
        assertFalse(TmfTimestampTransform.IDENTITY.equals(ttl));

        map.put(ti, "b");
        assertTrue(map.containsKey(TmfTimestampTransform.IDENTITY));
        assertEquals("b", map.get(ti));

        assertFalse(ti.equals(ttl));
        assertFalse(ttl.equals(ti));

    }

    /**
     * Test the transform composition function
     */
    @Test
    public void testComposition() {
        long t = 100;
        ITmfTimestampTransform ti = TmfTimestampTransform.IDENTITY;
        ITmfTimestampTransform ttl = new TmfTimestampTransformLinear(BigDecimal.valueOf(2.0), BigDecimal.valueOf(3));
        ITmfTimestampTransform ttl2 = new TmfTimestampTransformLinear(BigDecimal.valueOf(1.5), BigDecimal.valueOf(8));

        ITmfTimestampTransform tc1 = ti.composeWith(ttl);
        /* Should be ttl */
        assertEquals(ttl, tc1);
        assertEquals(203, tc1.transform(t));

        tc1 = ttl.composeWith(ti);
        /* Should be ttl also */
        assertEquals(ttl, tc1);
        assertEquals(203, tc1.transform(t));

        tc1 = ti.composeWith(ti);
        /* Should be identity */
        assertEquals(tc1, TmfTimestampTransform.IDENTITY);
        assertEquals(100, tc1.transform(t));

        tc1 = ttl.composeWith(ttl2);
        assertEquals(ttl.transform(ttl2.transform(t)), tc1.transform(t));
        assertEquals(319, tc1.transform(t));

        tc1 = ttl2.composeWith(ttl);
        assertEquals(ttl2.transform(ttl.transform(t)), tc1.transform(t));
        assertEquals(312, tc1.transform(t));

    }

    /**
     * Test whether the fast linear transform always yields the same value for
     * the same timestamp
     */
    @Test
    public void testFLTRepeatability() {
        TmfTimestampTransformLinear precise = new TmfTimestampTransformLinear(Math.PI, 0);
        TmfTimestampTransformLinearFast fast = new TmfTimestampTransformLinearFast(precise);

        // Initialize the transform
        fast.transform(ts);
        long tsMiss = ts + (1 << 30) + 1;
        long tsNoMiss = tsMiss - 10;

        // Get the transformed value to a timestamp with no cache miss
        long tsTNoMiss = fast.transform(tsNoMiss);
        assertEquals(1, fast.getCacheMisses());

        // Cause a cache miss
        fast.transform(tsMiss);
        assertEquals(2, fast.getCacheMisses());

        /*
         * Get the transformed value of the same previous timestamp after the
         * miss
         */
        long tsTAfterMiss = fast.transform(tsNoMiss);
        assertEquals(tsTNoMiss, tsTAfterMiss);
    }

    /**
     * Test that 2 equal fast transform always give the same results for the
     * same values
     */
    @Test
    public void testFLTEquivalence() {
        TmfTimestampTransformLinear precise = new TmfTimestampTransformLinear(Math.PI, 0);
        TmfTimestampTransformLinearFast fast = new TmfTimestampTransformLinearFast(precise);
        TmfTimestampTransformLinearFast fast2 = new TmfTimestampTransformLinearFast(precise);

        assertEquals(fast, fast2);

        // Initialize the transforms with different first timestamps
        fast.transform(ts);
        fast2.transform(ts + 10);

        long tsTest = ts + (1 << 30) + 1;

        /*
         * With fast, there should be a cache miss, but not with fast2, but
         * results should be the same
         */
        long ts1 = fast.transform(tsTest);
        assertEquals(2, fast.getCacheMisses());
        long ts2 = fast2.transform(tsTest);
        assertEquals(1, fast2.getCacheMisses());
        assertEquals(ts1, ts2);

    }

    /**
     * Test the precision of the fast timestamp transform compared to the
     * original transform.
     */
    @Test
    public void testFastTransformPrecision() {
        TmfTimestampTransformLinear precise = new TmfTimestampTransformLinear(Math.PI, 0);
        TmfTimestampTransformLinearFast fast = new TmfTimestampTransformLinearFast(precise);
        long start = (long) Math.pow(10, 18);

        int samples = 100;
        simulateTime(precise, fast, samples, start, Long.MAX_VALUE / samples);
        assertEquals(samples, fast.getCacheMisses());

        // check that rescale is done only when required
        // assumes tsBitWidth == 30
        // test forward and backward timestamps
        samples = 1000;
        int[] directions = new int[] { 1, -1 };
        for (Integer direction : directions) {
            for (int i = 0; i <= 30; i++) {
                fast.resetScaleStats();
                long step = (1 << i) * direction;
                simulateTime(precise, fast, samples, start, step);
                assertTrue(String.format("samples: %d scale misses: %d",
                        samples, fast.getCacheMisses()), samples > fast.getCacheMisses());
            }
        }

    }

    private static void simulateTime(ITmfTimestampTransform precise, ITmfTimestampTransform fast,
            int samples, long start, long step) {

        for (int i = 0; i < samples; i++) {
            long time = start + i * step;
            long exp = precise.transform(time);
            long act = fast.transform(time);
            long err = act - exp;
            // allow only two ns of error
            assertTrue("[" + err + "]", Math.abs(err) < 3);
        }
    }

}
