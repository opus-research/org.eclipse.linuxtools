/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.event;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfIntervalTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 * Test suite for the TmfIntervalTimestamp class.
 */
@SuppressWarnings("nls")
public class TmfIntervalTimestampTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final ITmfTimestamp ts0 = new TmfIntervalTimestamp();
    private final ITmfTimestamp ts1 = new TmfIntervalTimestamp(12345,  0);
    private final ITmfTimestamp ts2 = new TmfIntervalTimestamp(12345, -1);
    private final ITmfTimestamp ts3 = new TmfIntervalTimestamp(12345,  2, 5);
    private final ITmfTimestamp ts4 = new TmfIntervalTimestamp(-12345,  -5);

    // ------------------------------------------------------------------------
    // Housekeping
    // ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
    public TmfIntervalTimestampTest(final String name) {
        super(name);
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
    public void testValueConstructor() {
        assertEquals("getValue", 12345, ts1.getValue());
        assertEquals("getscale", 0, ts1.getScale());
        assertEquals("getPrecision", 0, ts1.getPrecision());
    }

    /**
     *
     */
    public void testValueScaleConstructor() {
        assertEquals("getValue", 12345, ts2.getValue());
        assertEquals("getscale", -1, ts2.getScale());
        assertEquals("getPrecision", 0, ts2.getPrecision());
    }

    /**
     *
     */
    public void testFullConstructor() {
        assertEquals("getValue", 12345, ts3.getValue());
        assertEquals("getscale", 2, ts3.getScale());
        assertEquals("getPrecision", 5, ts3.getPrecision());

        assertEquals("getValue", -12345, ts4.getValue());
        assertEquals("getscale", -5, ts4.getScale());
        assertEquals("getPrecision", 0, ts4.getPrecision());
    }

    /**
     *
     */
    public void testCopyConstructor() {
        final ITmfTimestamp ts = new TmfTimestamp(12345, 2, 5);
        final ITmfTimestamp copy = new TmfTimestamp(ts);

        assertEquals("getValue", ts.getValue(), copy.getValue());
        assertEquals("getscale", ts.getScale(), copy.getScale());
        assertEquals("getPrecision", ts.getPrecision(), copy.getPrecision());

        assertEquals("getValue", 12345, copy.getValue());
        assertEquals("getscale", 2, copy.getScale());
        assertEquals("getPrecision", 5, copy.getPrecision());
    }

    /**
     *
     */
    public void testCopyNullConstructor() {
        try {
            new TmfTimestamp(null);
            fail("TmfIntervalTimestamp: null argument");
        } catch (final IllegalArgumentException e) {
        }
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    /**
     *
     */
    public void testToStringDefault() {
        assertEquals("toString", "000.000 000 000", ts0.toString());
        assertEquals("toString", "12345.000 000 000", ts1.toString());
        assertEquals("toString", "1234.500 000 000", ts2.toString());
        assertEquals("toString", "1234500.000 000 000", ts3.toString());
        assertEquals("toString", "-000.123 450 000", ts4.toString());
    }
}
