package org.eclipse.linuxtools.tmf.ui.tests.statistics;

import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsValues;

import junit.framework.TestCase;

/**
 * TmfStatistics Test Cases.
 */
public class TmfStatisticsTest extends TestCase {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    TmfStatisticsValues stats = new TmfStatisticsValues();

    // ------------------------------------------------------------------------
    // Checks initial state
    // ------------------------------------------------------------------------

    /**
     * Test the initial state of the counters
     */
    public void testInitialState() {
        assertEquals(0, stats.getTotal());
        assertEquals(0, stats.getPartial());
    }

    // ------------------------------------------------------------------------
    // Increment Total no parameter
    // ------------------------------------------------------------------------

    /**
     * Test incrementing the total counter by an amount
     */
    public void testSetTotal() {
        int i = 100;
        stats.setTotal(i);
        assertEquals(i, stats.getTotal());

        // Try to assign a negative number. Should do nothing.
        stats.setTotal(-10);
        assertEquals(i, stats.getTotal());

        // Checks if the partial counter was affected
        assertEquals(0, stats.getPartial());
    }

    /**
     * Test incrementing the partial counter by a certain amount
     */
    public void testSetPartial() {
        int i = 100;
        stats.setPartial(i);
        assertEquals(i, stats.getPartial());

        // Try to assign a negative number. Should do nothing.
        stats.setPartial(-10);
        assertEquals(i, stats.getPartial());

        // Checks if the total counter was affected
        assertEquals(0, stats.getTotal());
    }

    /**
     * Test of the reset for the total counter
     */
    public void testResetTotal() {
        stats.setTotal(123);
        assertEquals(123, stats.getTotal());

        stats.resetTotalCount();
        assertEquals(0, stats.getTotal());

        // test when already at 0
        stats.resetTotalCount();
        assertEquals(0, stats.getTotal());
    }

    /**
     * Test of the reset for the partial counter
     */
    public void testResetPartial() {
        stats.setPartial(456);
        assertEquals(456, stats.getPartial());

        stats.resetPartialCount();
        assertEquals(0, stats.getPartial());

        // test when already at 0
        stats.resetPartialCount();
        assertEquals(0, stats.getPartial());
    }
}
