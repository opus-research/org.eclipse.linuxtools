/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   jean-Christian Kouame - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.statevalue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
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

    /**
     * Test adding an integer state value to an integer state value
     */
    @Test
    public void testAddIntToInt() {
        try {
            ITmfStateValue sv = VALUE2.add(VALUE6);
            assertEquals(30, sv.unboxInt());
        } catch (StateValueTypeException e) {
            fail();
        }
    }

    /**
     * Test adding a long state value to an integer state value
     */
    @Test
    public void testAddLongToInt() {
        try {
            VALUE2.add(VALUE8);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test adding a string state value to an integer state value
     */
    @Test
    public void testAddStringToInt() {
        try {
            VALUE2.add(VALUE1);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test adding a null state value to an integer state value
     */
    @Test
    public void testAddNullToInt() {
        try {
            VALUE2.add(VALUE3);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test adding an integer state value to a long state value
     */
    @Test
    public void testAddIntToLong() {
        try {
            VALUE8.add(VALUE2);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test adding a long state value to a long state value
     */
    @Test
    public void testAddLongToLong() {
        try {
            ITmfStateValue long1 = TmfStateValue.newValueLong(1l);
            ITmfStateValue long2 = TmfStateValue.newValueLong(9L);
            ITmfStateValue result = long1.add(long2);
            assertEquals(10L, result.unboxLong());
        } catch (StateValueTypeException e) {
            fail();
        }
    }

    /**
     * Test adding a string state value to a long state value
     */
    @Test
    public void testAddStringToLong() {
        try {
            VALUE8.add(VALUE1);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test adding a null state value to a long state value
     */
    @Test
    public void testAddNullToLong() {
        try {
            VALUE8.add(VALUE7);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test adding an integer state value to a string state value
     */
    @Test
    public void testAddIntToString() {
        try {
            VALUE1.add(VALUE2);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test adding a long state value to a string state value
     */
    @Test
    public void testAddLongToString() {
        try {
            VALUE1.add(VALUE8);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test adding a string state value to a string state value
     */
    @Test
    public void testAddStringToString() {
        try {
            VALUE1.add(VALUE4);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test adding a null state value to a string state value
     */
    @Test
    public void testAddNullToString() {
        try {
            VALUE1.add(VALUE7);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test adding an integer state value to a null state value
     */
    @Test
    public void testAddIntToNull() {
        try {
            VALUE3.add(VALUE2);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test adding a long state value to a null state value
     */
    @Test
    public void testAddLongToNull() {
        try {
            VALUE3.add(VALUE8);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test adding a string state value to a null state value
     */
    @Test
    public void testAddStringToNull() {
        try {
            VALUE3.add(VALUE1);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test adding a null state value to a null state value
     */
    @Test
    public void testAddNullToNull() {
        try {
            VALUE3.add(VALUE7);
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test increment an integer state value
     */
    @Test
    public void testIncrementInt() {
        try {
            ITmfStateValue sv = VALUE2.increment();
            assertEquals(VALUE2.unboxInt() + 1, sv.unboxInt());
        } catch (StateValueTypeException e) {
            fail();
        }
        assertTrue(true);
    }

    /**
     * Test increment a long state value
     */
    @Test
    public void testIncrementLong() {
        try {
            ITmfStateValue sv = VALUE8.increment();
            assertEquals(VALUE8.unboxLong() + 1, sv.unboxLong());
        } catch (StateValueTypeException e) {
            fail();
        }
        assertTrue(true);
    }

    /**
     * Test increment a string state value
     */
    @Test
    public void testIncrementString() {
        try {
            VALUE1.increment();
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }

    /**
     * Test increment a null state value
     */
    @Test
    public void testIncrementNull() {
        try {
            VALUE3.increment();
            fail();
        } catch (StateValueTypeException e) {
            assertTrue(true);
        }
    }
}
