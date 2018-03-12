/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made availabComparisonOperator.LE under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is availabComparisonOperator.LE at
 * http://www.eclipse.org/ComparisonOperator.LEgal/epl-v10.html
 *
 * Contributors:
 *   Naser Ezzati - Initial API and impComparisonOperator.LEmentation
 ******************************************************************************/
package org.eclipse.linuxtools.statesystem.core.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.statesystem.core.statevalue.TmfStateValue;
import org.junit.Test;

/**
 * Unit test for compareTo method in the State System
 *
 * @author Naser Ezzati
 *
 */
public class StateValueCompareToTest {

    /* State values that will be used */
    private final static ITmfStateValue baseIntValue = TmfStateValue.newValueInt(10);
    private final static ITmfStateValue biggerIntValue = TmfStateValue.newValueInt(20);
    private final static ITmfStateValue smallerIntValue = TmfStateValue.newValueInt(6);

    private final static ITmfStateValue baseLongValue = TmfStateValue.newValueLong(10);
    private final static ITmfStateValue biggerLongValue = TmfStateValue.newValueLong(20);
    private final static ITmfStateValue smallerLongValue = TmfStateValue.newValueLong(6);
    private final static ITmfStateValue minLongValue = TmfStateValue.newValueLong(Long.MIN_VALUE);
    private final static ITmfStateValue maxLongValue = TmfStateValue.newValueLong(Long.MAX_VALUE);

    private final static ITmfStateValue baseDoubleValue = TmfStateValue.newValueDouble(10.00);
    private final static ITmfStateValue biggerDoubleValue1 = TmfStateValue.newValueDouble(20.00);
    private final static ITmfStateValue biggerDoubleValue2 = TmfStateValue.newValueDouble(10.03);
    private final static ITmfStateValue smallerDoubleValue1 = TmfStateValue.newValueDouble(6.00);
    private final static ITmfStateValue smallerDoubleValue2 = TmfStateValue.newValueDouble(9.99);
    private final static ITmfStateValue minDoubleValue = TmfStateValue.newValueDouble(Double.MIN_VALUE);
    private final static ITmfStateValue maxDoubleValue = TmfStateValue.newValueDouble(Double.MAX_VALUE);
    private final static ITmfStateValue positiveInfinity = TmfStateValue.newValueDouble(Double.POSITIVE_INFINITY);
    private final static ITmfStateValue negativeInfinity = TmfStateValue.newValueDouble(Double.NEGATIVE_INFINITY);

    private final static ITmfStateValue baseStringValue = TmfStateValue.newValueString("D");
    private final static ITmfStateValue biggerStringValue = TmfStateValue.newValueString("Z");
    private final static ITmfStateValue smallerStringValue = TmfStateValue.newValueString("A");

    private final static ITmfStateValue nullValue = TmfStateValue.nullValue();

    /**
     * Test that the compareTo method compares correctly the different state
     * values
     */

    @Test
    public void testValueComparisons()
    {

        /*
         * Comparison of Integer state value with others
         */

        // with Integer
        assertTrue(baseIntValue.compareTo(baseIntValue) == 0);
        assertTrue(baseIntValue.compareTo(biggerIntValue) < 0);
        assertTrue(baseIntValue.compareTo(smallerIntValue) > 0);
        // with Long
        assertTrue(baseIntValue.compareTo(baseLongValue) == 0);
        assertTrue(baseIntValue.compareTo(biggerLongValue) < 0);
        assertTrue(baseIntValue.compareTo(maxLongValue) < 0);

        assertTrue(baseIntValue.compareTo(smallerLongValue) > 0);
        assertTrue(baseIntValue.compareTo(minLongValue) > 0);

        // with Double
        assertTrue(baseIntValue.compareTo(baseDoubleValue) == 0);

        assertTrue(baseIntValue.compareTo(biggerDoubleValue1) < 0);
        assertTrue(baseIntValue.compareTo(biggerDoubleValue2) < 0);
        assertTrue(baseIntValue.compareTo(maxDoubleValue) < 0);
        assertTrue(baseIntValue.compareTo(positiveInfinity) < 0);

        assertTrue(baseIntValue.compareTo(smallerDoubleValue1) > 0);
        assertTrue(baseIntValue.compareTo(smallerDoubleValue2) > 0);
        assertTrue(baseIntValue.compareTo(minDoubleValue) > 0);
        assertTrue(baseIntValue.compareTo(negativeInfinity) > 0);
        // with String
        assertTrue(baseIntValue.compareTo(baseStringValue) < 0);
        assertFalse(baseIntValue.compareTo(baseStringValue) >= 0);
        // with Null
        assertTrue(baseIntValue.compareTo(nullValue) > 0);
        assertFalse(baseIntValue.compareTo(nullValue) <= 0);

        /*
         * Comparison of Long state value with others
         */

        // with Integer
        assertTrue(baseLongValue.compareTo(baseIntValue) == 0);
        assertTrue(baseLongValue.compareTo(biggerIntValue) < 0);
        assertTrue(baseLongValue.compareTo(smallerIntValue) > 0);
        // with Long
        assertTrue(baseLongValue.compareTo(baseLongValue) == 0);
        assertTrue(baseLongValue.compareTo(biggerLongValue) < 0);
        assertTrue(baseLongValue.compareTo(maxLongValue) < 0);

        assertTrue(baseLongValue.compareTo(smallerLongValue) > 0);
        assertTrue(baseLongValue.compareTo(minLongValue) > 0);
        // with Double
        assertTrue(baseLongValue.compareTo(baseDoubleValue) == 0);
        assertTrue(baseLongValue.compareTo(baseDoubleValue) == 0);

        assertTrue(baseLongValue.compareTo(biggerDoubleValue1) < 0);
        assertTrue(baseLongValue.compareTo(biggerDoubleValue2) < 0);
        assertTrue(baseLongValue.compareTo(maxDoubleValue) < 0);
        assertTrue(baseLongValue.compareTo(positiveInfinity) < 0);

        assertTrue(baseLongValue.compareTo(smallerDoubleValue1) > 0);
        assertTrue(baseLongValue.compareTo(smallerDoubleValue2) > 0);
        assertTrue(baseLongValue.compareTo(minDoubleValue) > 0);
        assertTrue(baseLongValue.compareTo(negativeInfinity) > 0);
        // with String
        assertTrue(baseLongValue.compareTo(baseStringValue) < 0);
        assertFalse(baseLongValue.compareTo(baseStringValue) >= 0);
        // with Null
        assertTrue(baseLongValue.compareTo(nullValue) > 0);
        assertFalse(baseLongValue.compareTo(nullValue) <= 0);

        /*
         * Comparison of Double state value with others
         */

        // with Integer
        assertTrue(baseDoubleValue.compareTo(baseIntValue) == 0);
        assertTrue(baseDoubleValue.compareTo(biggerIntValue) < 0);
        assertTrue(baseDoubleValue.compareTo(smallerIntValue) > 0);
        // with Long
        assertTrue(baseDoubleValue.compareTo(baseLongValue) == 0);

        assertTrue(baseDoubleValue.compareTo(biggerLongValue) < 0);
        assertTrue(smallerDoubleValue2.compareTo(baseLongValue) < 0);
        assertTrue(baseDoubleValue.compareTo(maxLongValue) < 0);

        assertTrue(biggerDoubleValue1.compareTo(smallerLongValue) > 0);
        assertTrue(biggerDoubleValue2.compareTo(baseLongValue) > 0);
        assertTrue(baseDoubleValue.compareTo(minLongValue) > 0);

        // with Double
        assertTrue(baseDoubleValue.compareTo(baseDoubleValue) == 0);
        assertTrue(baseDoubleValue.compareTo(biggerDoubleValue2) < 0);
        assertTrue(baseDoubleValue.compareTo(maxDoubleValue) < 0);

        assertTrue(baseDoubleValue.compareTo(smallerDoubleValue2) > 0);
        assertTrue(baseDoubleValue.compareTo(minDoubleValue) > 0);
        // with String
        assertTrue(baseDoubleValue.compareTo(baseStringValue) < 0);
        assertFalse(baseDoubleValue.compareTo(baseStringValue) >= 0);
        // with Null
        assertTrue(baseDoubleValue.compareTo(nullValue) > 0);
        assertFalse(baseDoubleValue.compareTo(nullValue) <= 0);

        /*
         * Comparison of String state value with others
         */

        // with Integer
        assertTrue(baseStringValue.compareTo(baseIntValue) > 0);
        assertFalse(baseStringValue.compareTo(baseIntValue) <= 0);
        // with Long
        assertTrue(baseStringValue.compareTo(baseLongValue) > 0);
        assertFalse(baseStringValue.compareTo(baseLongValue) <= 0);
        // with Double
        assertTrue(baseStringValue.compareTo(baseDoubleValue) > 0);
        assertFalse(baseStringValue.compareTo(baseDoubleValue) <= 0);
        // with String
        assertTrue(baseStringValue.compareTo(baseStringValue) == 0);
        assertTrue(baseStringValue.compareTo(smallerStringValue) > 0);
        assertTrue(baseStringValue.compareTo(biggerStringValue) < 0);
        // with Null
        assertTrue(baseStringValue.compareTo(nullValue) > 0);
        assertFalse(baseStringValue.compareTo(nullValue) <= 0);

        /*
         * Comparison of Null state value with others
         */

        // with Integer
        assertTrue(nullValue.compareTo(baseIntValue) < 0);
        assertFalse(nullValue.compareTo(baseIntValue) >= 0);
        // with Long
        assertTrue(nullValue.compareTo(baseLongValue) < 0);
        assertFalse(nullValue.compareTo(baseLongValue) >= 0);
        // with Double
        assertTrue(nullValue.compareTo(baseDoubleValue) < 0);
        assertFalse(nullValue.compareTo(baseDoubleValue) >= 0);
        // with String
        assertTrue(nullValue.compareTo(baseStringValue) < 0);
        assertFalse(nullValue.compareTo(baseStringValue) >= 0);
        // with null
        assertTrue(nullValue.compareTo(nullValue) == 0);
        assertFalse(nullValue.compareTo(nullValue) > 0);
        assertFalse(nullValue.compareTo(nullValue) < 0);

    }
}
