/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.event;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfSimpleTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 * Test suite for the TmfSimpleTimestampTest class.
 */
@SuppressWarnings("nls")
public class TmfSimpleTimestampTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final ITmfTimestamp ts0 = new TmfSimpleTimestamp();
    private final ITmfTimestamp ts1 = new TmfSimpleTimestamp(12345);
    private final ITmfTimestamp ts2 = new TmfSimpleTimestamp(-1234);

    // ------------------------------------------------------------------------
    // Housekeping
    // ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
    public TmfSimpleTimestampTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     *
     */
    public void testDefaultConstructor() {
        assertEquals("getValue", 0, ts0.getValue());
        assertEquals("getscale", 0, ts0.getScale());
        assertEquals("getPrecision", 0, ts0.getPrecision());
    }

    /**
     *
     */
    public void testFullConstructor() {
        assertEquals("getValue", 12345, ts1.getValue());
        assertEquals("getscale", 0, ts1.getScale());
        assertEquals("getPrecision", 0, ts1.getPrecision());
    }

    /**
     *
     */
    public void testCopyConstructor() {
        final ITmfTimestamp copy = new TmfSimpleTimestamp(ts1);

        assertEquals("getValue", ts1.getValue(), copy.getValue());
        assertEquals("getscale", ts1.getScale(), copy.getScale());
        assertEquals("getPrecision", ts1.getPrecision(), copy.getPrecision());

        assertEquals("getValue", 12345, copy.getValue());
        assertEquals("getscale", 0, copy.getScale());
        assertEquals("getPrecision", 0, copy.getPrecision());
    }

    /**
     *
     */
    public void testCopyBadTimestamp() {
        final ITmfTimestamp ts0a = new TmfTimestamp(0, 100, 0);

        try {
            new TmfSimpleTimestamp(null);
            fail("TmfSimpleTimestamp: null argument");
        } catch (final IllegalArgumentException e) {
        }

        try {
            new TmfSimpleTimestamp(ts0a);
            fail("TmfSimpleTimestamp: bad scale");
        } catch (final ArithmeticException e) {
        }
    }

    // ------------------------------------------------------------------------
    // clone
    // ------------------------------------------------------------------------

    private static class MyTimestamp extends TmfSimpleTimestamp {

        @Override
        public boolean equals(final Object other) {
            return super.equals(other);
        }

        @Override
        public MyTimestamp clone() {
            return (MyTimestamp) super.clone();
        }
    }

    /**
     *
     */
    public void testClone() {
        final ITmfTimestamp clone = ts0.clone();

        assertTrue("clone", ts0.clone().equals(ts0));
        assertTrue("clone", clone.clone().equals(clone));

        assertEquals("clone", clone, ts0);
        assertEquals("clone", ts0, clone);
    }

    /**
     *
     */
    public void testClone2() {
        final MyTimestamp timestamp = new MyTimestamp();
        final MyTimestamp clone = timestamp.clone();

        assertTrue("clone", timestamp.clone().equals(timestamp));
        assertTrue("clone", clone.clone().equals(clone));

        assertEquals("clone", clone, timestamp);
        assertEquals("clone", timestamp, clone);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    /**
     *
     */
    public void testEqualsReflexivity() {
        assertTrue("equals", ts0.equals(ts0));
        assertTrue("equals", ts1.equals(ts1));
        assertTrue("equals", ts2.equals(ts2));

        assertTrue("equals", !ts0.equals(ts1));
        assertTrue("equals", !ts0.equals(ts2));

        assertTrue("equals", !ts1.equals(ts0));
        assertTrue("equals", !ts1.equals(ts2));

        assertTrue("equals", !ts2.equals(ts0));
        assertTrue("equals", !ts2.equals(ts1));
    }

    /**
     *
     */
    public void testEqualsSymmetry() {
        final ITmfTimestamp ts0copy = new TmfSimpleTimestamp(ts0);
        assertTrue("equals", ts0.equals(ts0copy));
        assertTrue("equals", ts0copy.equals(ts0));

        final ITmfTimestamp ts1copy = new TmfSimpleTimestamp(ts1);
        assertTrue("equals", ts1.equals(ts1copy));
        assertTrue("equals", ts1copy.equals(ts1));
    }

    /**
     *
     */
    public void testEqualsTransivity() {
        final ITmfTimestamp ts0copy1 = new TmfSimpleTimestamp(ts0);
        final ITmfTimestamp ts0copy2 = new TmfSimpleTimestamp(ts0copy1);
        assertTrue("equals", ts0.equals(ts0copy1));
        assertTrue("equals", ts0copy1.equals(ts0copy2));
        assertTrue("equals", ts0.equals(ts0copy2));

        final ITmfTimestamp ts1copy1 = new TmfSimpleTimestamp(ts1);
        final ITmfTimestamp ts1copy2 = new TmfSimpleTimestamp(ts1copy1);
        assertTrue("equals", ts1.equals(ts1copy1));
        assertTrue("equals", ts1copy1.equals(ts1copy2));
        assertTrue("equals", ts1.equals(ts1copy2));
    }

    /**
     *
     */
    public void testEqualsNull() {
        assertTrue("equals", !ts0.equals(null));
        assertTrue("equals", !ts1.equals(null));
        assertTrue("equals", !ts2.equals(null));
    }

    /**
     *
     */
    public void testEqualsNonTimestamp() {
        assertFalse("equals", ts0.equals(ts0.toString()));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    /**
     *
     */
    public void testToString() {
        assertEquals("toString", "00:00:00.000_000_000", ts0.toString());
        assertEquals("toString", "03:25:45.000_000_000", ts1.toString());
        assertEquals("toString", "23:39:26.000_000_000", ts2.toString());
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    /**
     *
     */
    public void testHashCode() {
        final ITmfTimestamp ts0copy = new TmfTimestamp(ts0);
        final ITmfTimestamp ts1copy = new TmfTimestamp(ts1);
        final ITmfTimestamp ts2copy = new TmfTimestamp(ts2);

        assertTrue("hashCode", ts0.hashCode() == ts0copy.hashCode());
        assertTrue("hashCode", ts1.hashCode() == ts1copy.hashCode());
        assertTrue("hashCode", ts2.hashCode() == ts2copy.hashCode());

        assertTrue("hashCode", ts0.hashCode() != ts1.hashCode());
    }

    // ------------------------------------------------------------------------
    // normalize
    // ------------------------------------------------------------------------

    /**
     *
     */
    public void testNormalizeScale0() {
        ITmfTimestamp ts = ts0.normalize(0, 0);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(12345, 0);
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(10, 0);
        assertEquals("getValue", 10, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(-10, 0);
        assertEquals("getValue", -10, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    /**
     *
     */
    public void testNormalizeScaleNot0() {
        ITmfTimestamp ts = ts0.normalize(0, 1);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 1, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(12345, 1);
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale", 1, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(10, 1);
        assertEquals("getValue", 10, ts.getValue());
        assertEquals("getscale", 1, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(-10, 1);
        assertEquals("getValue", -10, ts.getValue());
        assertEquals("getscale", 1, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    // ------------------------------------------------------------------------
    // compareTo
    // ------------------------------------------------------------------------

    /**
     *
     */
    public void testBasicCompareTo() {
        final ITmfTimestamp tstamp1 = new TmfSimpleTimestamp(900);
        final ITmfTimestamp tstamp2 = new TmfSimpleTimestamp(1000);
        final ITmfTimestamp tstamp3 = new TmfSimpleTimestamp(1100);

        assertTrue(tstamp1.compareTo(tstamp1) == 0);

        assertTrue("CompareTo", tstamp1.compareTo(tstamp2) < 0);
        assertTrue("CompareTo", tstamp1.compareTo(tstamp3) < 0);

        assertTrue("CompareTo", tstamp2.compareTo(tstamp1) > 0);
        assertTrue("CompareTo", tstamp2.compareTo(tstamp3) < 0);

        assertTrue("CompareTo", tstamp3.compareTo(tstamp1) > 0);
        assertTrue("CompareTo", tstamp3.compareTo(tstamp2) > 0);
    }

    /**
     *
     */
    public void testCompareTo() {
        final ITmfTimestamp ts0a = new TmfTimestamp(0, 2, 0);
        final ITmfTimestamp ts1a = new TmfTimestamp(123450, -1);
        final ITmfTimestamp ts2a = new TmfTimestamp(-12340, -1);

        assertTrue(ts1.compareTo(ts1) == 0);

        assertTrue("CompareTo", ts0.compareTo(ts0a) == 0);
        assertTrue("CompareTo", ts1.compareTo(ts1a) == 0);
        assertTrue("CompareTo", ts2.compareTo(ts2a) == 0);
    }

    // ------------------------------------------------------------------------
    // getDelta
    // ------------------------------------------------------------------------

    /**
     *
     */
    public void testDelta() {
        // Delta for same scale and precision (delta > 0)
        TmfTimestamp tstamp0 = new TmfSimpleTimestamp(10);
        TmfTimestamp tstamp1 = new TmfSimpleTimestamp(5);
        TmfTimestamp expectd = new TmfSimpleTimestamp(5);

        ITmfTimestamp delta = tstamp0.getDelta(tstamp1);
        assertEquals("getDelta", 0, delta.compareTo(expectd, false));

        // Delta for same scale and precision (delta < 0)
        tstamp0 = new TmfTimestamp(5);
        tstamp1 = new TmfTimestamp(10);
        expectd = new TmfTimestamp(-5);

        delta = tstamp0.getDelta(tstamp1);
        assertEquals("getDelta", 0, delta.compareTo(expectd, false));
    }

    /**
     *
     */
    public void testDelta2() {
        // Delta for different scale and same precision (delta > 0)
        final TmfTimestamp tstamp0 = new TmfSimpleTimestamp(10);
        final TmfTimestamp tstamp1 = new TmfTimestamp(1, 1);
        final TmfTimestamp expectd = new TmfTimestamp(0, 0);

        final ITmfTimestamp delta = tstamp0.getDelta(tstamp1);
        assertEquals("getDelta", 0, delta.compareTo(expectd, false));
    }

}
