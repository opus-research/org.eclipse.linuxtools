/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   jean-Christian Kouamé - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.statevalue;

import static org.junit.Assert.assertEquals;

import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.junit.Test;

/**
 * Test for the {@link ITmfStateValue}.
 *
 * @author Jean-Christian Kouamé
 */
public class TmfStateValueTest {

    private final static int GREATER = 1;
    private final static int EQUAL_OR_DIFFERENT_TYPE = 0;
    private final static int LOWER = -1;

    /* State values that will be used */
    private final static ITmfStateValue VALUE1 = TmfStateValue.newValueString("guitare");
    private final static ITmfStateValue VALUE2 = TmfStateValue.newValueInt(10);
    private final static ITmfStateValue VALUE3 = TmfStateValue.nullValue();
    private final static ITmfStateValue VALUE4 = TmfStateValue.newValueString("guitaro");
    private final static ITmfStateValue VALUE5 = TmfStateValue.newValueLong(Long.MAX_VALUE);
    private final static ITmfStateValue VALUE6 = TmfStateValue.newValueInt(20);
    private final static ITmfStateValue VALUE7 = TmfStateValue.nullValue();
    private final static ITmfStateValue VALUE8 = TmfStateValue.newValueLong(9978375435753453L);

    /**
     * Test comparing a string state value to an integer state value
     */
    @Test
    public void testCompareStringToInt() {
        int returnValue = VALUE1.compareTo(VALUE2);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing a string state value to a long state value
     */
    @Test
    public void testCompareStringToLong() {
        int returnValue = VALUE1.compareTo(VALUE5);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing a string state value to an other string state value
     */
    @Test
    public void testCompareStringToString() {
        int returnValue;
        returnValue = VALUE1.compareTo(VALUE1);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);

        returnValue = VALUE1.compareTo(VALUE4);
        assertEquals(true, returnValue < 0);

        returnValue = VALUE4.compareTo(VALUE1);
        assertEquals(true, returnValue > 0);
    }

    /**
     * Test comparing a string state value to a null state value
     */
    @Test
    public void testCompareStringToNull() {
        int returnValue = VALUE1.compareTo(VALUE3);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing an integer state value to an other integer state value
     */
    @Test
    public void testCompareIntToInt() {
        int returnValue = VALUE2.compareTo(VALUE6);
        assertEquals(LOWER, returnValue);

        returnValue = VALUE6.compareTo(VALUE2);
        assertEquals(GREATER, returnValue);

        returnValue = VALUE2.compareTo(VALUE2);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing an integer state value to a long state value
     */
    @Test
    public void testCompareIntToLong() {
        int returnValue = VALUE2.compareTo(VALUE8);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing an integer state value to a string state value
     */
    @Test
    public void testCompareIntToString() {
        int returnValue = VALUE2.compareTo(VALUE1);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing an integer state value to a null state value
     */
    @Test
    public void testCompareIntToNull() {
        int returnValue = VALUE2.compareTo(VALUE3);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing a null state value to an integer state value
     */
    @Test
    public void testCompareNullToInt() {
        int returnValue = VALUE3.compareTo(VALUE2);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing a null state value to a long state value
     */
    @Test
    public void testCompareNullToLong() {
        int returnValue = VALUE3.compareTo(VALUE8);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing a null state value to a string state value
     */
    @Test
    public void testCompareNullToString() {
        int returnValue = VALUE3.compareTo(VALUE1);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing a null state value to another null state value
     */
    @Test
    public void testCompareNullToNull() {
        int returnValue = VALUE3.compareTo(VALUE7);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing a long state value to an integer state value
     */
    @Test
    public void testCompareLongToInt() {
        int returnValue = VALUE5.compareTo(VALUE2);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing a long state value to another long state value
     */
    @Test
    public void testCompareLongToLong() {
        int returnValue = VALUE5.compareTo(VALUE8);
        assertEquals(GREATER, returnValue);

        returnValue = VALUE8.compareTo(VALUE5);
        assertEquals(LOWER, returnValue);

        returnValue = VALUE8.compareTo(VALUE8);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing a long state value to string state value
     */
    @Test
    public void testCompareLongToString() {
        int returnValue = VALUE5.compareTo(VALUE1);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }

    /**
     * Test comparing a long state value to a null state value
     */
    @Test
    public void testCompareLongToNull() {
        int returnValue = VALUE5.compareTo(VALUE3);
        assertEquals(EQUAL_OR_DIFFERENT_TYPE, returnValue);
    }
}
