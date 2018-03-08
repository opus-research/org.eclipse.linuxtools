/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.TmfLongLocation;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.StringLocationStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TimestampLocationStub;

/**
 * Test suite for the TmfLocation class.
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfLocationTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    String aString = "some location";
    Long aLong = 12345L;
    TmfTimestamp aTimestamp = new TmfTimestamp();

    StringLocationStub fLocation1;
    StringLocationStub fLocation2;
    TmfLongLocation fLocation3;
    TimestampLocationStub fLocation4;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * @param name
     *            the test name
     */
    public TmfLocationTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fLocation1 = new StringLocationStub((String) null);
        fLocation2 = new StringLocationStub(aString);
        fLocation3 = new TmfLongLocation(aLong);
        fLocation4 = new TimestampLocationStub(aTimestamp);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public void testTmfLocation() {
        assertNull("TmfLocation", fLocation1.getStringValue());
        assertEquals("TmfLocation", aString, fLocation2.getStringValue());
        assertEquals("TmfLocation", aLong, fLocation3.getLongValue());
        assertEquals("TmfLocation", aTimestamp, fLocation4.getTimestamp());
    }

    public void testTmfLocationCopy() {
        StringLocationStub location1 = new StringLocationStub(fLocation1);
        StringLocationStub location2 = new StringLocationStub(fLocation2);
        TmfLongLocation location3 = new TmfLongLocation(fLocation3);
        TimestampLocationStub location4 = new TimestampLocationStub(fLocation4);

        assertNull("TmfLocation", location1.getStringValue());
        assertEquals("TmfLocation", aString, location2.getStringValue());
        assertEquals("TmfLocation", aLong, location3.getLongValue());
        assertEquals("TmfLocation", aTimestamp, location4.getTimestamp());
    }

    // ------------------------------------------------------------------------
    // clone
    // ------------------------------------------------------------------------

    public void testClone() {
        try {
            StringLocationStub location1 = fLocation1.clone();
            StringLocationStub location2 = fLocation2.clone();
            TmfLongLocation location3 = fLocation3.clone();
            TimestampLocationStub location4 = fLocation4.clone();

            assertEquals("clone", fLocation1, location1);
            assertEquals("clone", fLocation2, location2);
            assertEquals("clone", fLocation3, location3);
            assertEquals("clone", fLocation4, location4);

            assertEquals("clone", fLocation1.getLocationData(), location1.getLocationData());
            assertEquals("clone", fLocation2.getLocationData(), location2.getLocationData());
            assertEquals("clone", fLocation3.getLocationData(), location3.getLocationData());
            assertEquals("clone", fLocation4.getLocationData(), location4.getLocationData());

            assertNull("clone", location1.getStringValue());
            assertEquals("clone", aString, location2.getStringValue());
            assertEquals("clone", aLong, location3.getLongValue());
            assertEquals("clone", aTimestamp, location4.getTimestamp());
        } catch (InternalError e) {
            fail("clone()");
        }
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    public void testHashCode() {
        StringLocationStub location1 = new StringLocationStub((String) null);
        StringLocationStub location2 = new StringLocationStub(aString);
        TmfLongLocation location3 = new TmfLongLocation(aLong);

        assertTrue("hashCode", fLocation1.hashCode() == location1.hashCode());
        assertTrue("hashCode", fLocation2.hashCode() == location2.hashCode());
        assertTrue("hashCode", fLocation3.hashCode() == location3.hashCode());

        assertTrue("hashCode", fLocation2.hashCode() != location3.hashCode());
        assertTrue("hashCode", fLocation3.hashCode() != location2.hashCode());
    }

    // ------------------------------------------------------------------------
    // toEquals
    // ------------------------------------------------------------------------

    private static class TmfLocation2 extends StringLocationStub {
        public TmfLocation2(String location) {
            super(location);
        }
    }

    public void testEqualsWrongTypes() {
        StringLocationStub location1 = new StringLocationStub(aString);
        TmfLocation2 location2 = new TmfLocation2(aString);

        assertFalse("equals", location1.equals(location2));
        assertFalse("equals", location2.equals(location1));
    }

    public void testEqualsWithNulls() {
        StringLocationStub location1 = new StringLocationStub(aString);
        StringLocationStub location2 = new StringLocationStub((String) null);

        assertFalse("equals", location1.equals(location2));
        assertFalse("equals", location2.equals(location1));
    }

    public void testEqualsReflexivity() {
        assertTrue("equals", fLocation2.equals(fLocation2));
        assertTrue("equals", fLocation3.equals(fLocation3));

        assertTrue("equals", !fLocation2.equals(fLocation3));
        assertTrue("equals", !fLocation3.equals(fLocation2));
    }

    public void testEqualsSymmetry() {
        StringLocationStub location2 = new StringLocationStub(aString);
        TmfLongLocation location3 = new TmfLongLocation(aLong);

        assertTrue("equals", location2.equals(fLocation2));
        assertTrue("equals", fLocation2.equals(location2));

        assertTrue("equals", location3.equals(fLocation3));
        assertTrue("equals", fLocation3.equals(location3));
    }

    public void testEqualsTransivity() {
        StringLocationStub location1 = new StringLocationStub(aString);
        StringLocationStub location2 = new StringLocationStub(aString);
        StringLocationStub location3 = new StringLocationStub(aString);

        assertTrue("equals", location1.equals(location2));
        assertTrue("equals", location2.equals(location3));
        assertTrue("equals", location3.equals(location1));
    }

    public void testEqualsNull() {
        assertTrue("equals", !fLocation2.equals(null));
        assertTrue("equals", !fLocation2.equals(null));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @SuppressWarnings("hiding")
    public void testToString() {
        String aString = "some location";
        Long aLong = 12345L;
        TmfTimestamp aTimestamp = new TmfTimestamp();

        StringLocationStub location1 = new StringLocationStub(aString);
        TmfLongLocation location2 = new TmfLongLocation(aLong);
        TimestampLocationStub location3 = new TimestampLocationStub(aTimestamp);

        String expected1 = "TmfLocation [fLocation=" + aString + "]";
        String expected2 = "TmfLocation [fLocation=" + aLong + "]";
        String expected3 = "TmfLocation [fLocation=" + aTimestamp + "]";

        assertEquals("toString", expected1, location1.toString());
        assertEquals("toString", expected2, location2.toString());
        assertEquals("toString", expected3, location3.toString());
    }

}
