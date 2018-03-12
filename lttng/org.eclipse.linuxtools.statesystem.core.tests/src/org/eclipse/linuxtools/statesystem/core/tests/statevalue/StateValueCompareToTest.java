/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made availabComparisonOperator.LE under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is availabComparisonOperator.LE at
 * http://www.eclipse.org/ComparisonOperator.LEgal/epl-v10.html
 *
 * Contributors:
 *   Naser Ezzati - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.statesystem.core.tests.statevalue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.statesystem.core.statevalue.TmfStateValue;
import org.junit.Test;

/**
 * Unit test for the {@link ITmfStateValue#compareTo(ITmfStateValue)} method
 *
 * @author Naser Ezzati
 */
public class StateValueCompareToTest {

    /* State values that will be used */
    private static final ITmfStateValue BASE_INT_VALUE = TmfStateValue.newValueInt(10);
    private static final ITmfStateValue BIGGER_INT_VALUE = TmfStateValue.newValueInt(20);
    private static final ITmfStateValue SMALLER_INT_VALUE = TmfStateValue.newValueInt(6);

    private static final ITmfStateValue BASE_LONG_VALUE = TmfStateValue.newValueLong(10);
    private static final ITmfStateValue BIGGER_LONG_VALUE = TmfStateValue.newValueLong(20);
    private static final ITmfStateValue SMALLER_LONG_VALUE = TmfStateValue.newValueLong(6);
    private static final ITmfStateValue MIN_LONG_VALUE = TmfStateValue.newValueLong(Long.MIN_VALUE);
    private static final ITmfStateValue MAX_LONG_VALUE = TmfStateValue.newValueLong(Long.MAX_VALUE);

    private static final ITmfStateValue BASE_DOUBLE_VALUE = TmfStateValue.newValueDouble(10.00);
    private static final ITmfStateValue BIGGER_DOUBLE_VALUE1 = TmfStateValue.newValueDouble(20.00);
    private static final ITmfStateValue BIGGER_DOUBLE_VALUE2 = TmfStateValue.newValueDouble(10.03);
    private static final ITmfStateValue SMALLER_DOUBLE_VALUE1 = TmfStateValue.newValueDouble(6.00);
    private static final ITmfStateValue SMALLER_DOUBLE_VALUE2 = TmfStateValue.newValueDouble(9.99);
    private static final ITmfStateValue MIN_DOUBLE_VALUE = TmfStateValue.newValueDouble(Double.MIN_VALUE);
    private static final ITmfStateValue MAX_DOUBLE_VALUE = TmfStateValue.newValueDouble(Double.MAX_VALUE);
    private static final ITmfStateValue POSITIVE_INFINITY = TmfStateValue.newValueDouble(Double.POSITIVE_INFINITY);
    private static final ITmfStateValue NEGATIVE_INFINITY = TmfStateValue.newValueDouble(Double.NEGATIVE_INFINITY);

    private static final ITmfStateValue BASE_STRING_VALUE = TmfStateValue.newValueString("D");
    private static final ITmfStateValue BIGGER_STRING_VALUE = TmfStateValue.newValueString("Z");
    private static final ITmfStateValue SMALLER_STRING_VALUE = TmfStateValue.newValueString("A");

    private static final ITmfStateValue NULL_VALUE = TmfStateValue.nullValue();

    /**
     * Comparing the Integer state values with other types
     *
     */
    @Test
    public void testIntValueComparisons() {

        // with Integer
        assertTrue(BASE_INT_VALUE.compareTo(BASE_INT_VALUE) == 0);
        assertTrue(BASE_INT_VALUE.compareTo(BIGGER_INT_VALUE) < 0);
        assertTrue(BASE_INT_VALUE.compareTo(SMALLER_INT_VALUE) > 0);

        // with Long
        assertTrue(BASE_INT_VALUE.compareTo(BASE_LONG_VALUE) == 0);
        assertTrue(BASE_INT_VALUE.compareTo(BIGGER_LONG_VALUE) < 0);
        assertTrue(BASE_INT_VALUE.compareTo(MAX_LONG_VALUE) < 0);

        assertTrue(BASE_INT_VALUE.compareTo(SMALLER_LONG_VALUE) > 0);
        assertTrue(BASE_INT_VALUE.compareTo(MIN_LONG_VALUE) > 0);

        // with Double
        assertTrue(BASE_INT_VALUE.compareTo(BASE_DOUBLE_VALUE) == 0);
        assertTrue(BASE_INT_VALUE.compareTo(BIGGER_DOUBLE_VALUE1) < 0);
        assertTrue(BASE_INT_VALUE.compareTo(BIGGER_DOUBLE_VALUE2) < 0);
        assertTrue(BASE_INT_VALUE.compareTo(MAX_DOUBLE_VALUE) < 0);
        assertTrue(BASE_INT_VALUE.compareTo(POSITIVE_INFINITY) < 0);
        assertTrue(BASE_INT_VALUE.compareTo(SMALLER_DOUBLE_VALUE1) > 0);
        assertTrue(BASE_INT_VALUE.compareTo(SMALLER_DOUBLE_VALUE2) > 0);
        assertTrue(BASE_INT_VALUE.compareTo(MIN_DOUBLE_VALUE) > 0);
        assertTrue(BASE_INT_VALUE.compareTo(NEGATIVE_INFINITY) > 0);

        // with String
        assertTrue(BASE_INT_VALUE.compareTo(BASE_STRING_VALUE) < 0);
        assertFalse(BASE_INT_VALUE.compareTo(BASE_STRING_VALUE) >= 0);

        // with Null
        assertTrue(BASE_INT_VALUE.compareTo(NULL_VALUE) > 0);
        assertFalse(BASE_INT_VALUE.compareTo(NULL_VALUE) <= 0);

    }

    /**
     * Comparing the Long state values with other types
     *
     */
    @Test
    public void testLongValueComparisons() {

        // with Integer
        assertTrue(BASE_LONG_VALUE.compareTo(BASE_INT_VALUE) == 0);
        assertTrue(BASE_LONG_VALUE.compareTo(BIGGER_INT_VALUE) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(SMALLER_INT_VALUE) > 0);

        // with Long
        assertTrue(BASE_LONG_VALUE.compareTo(BASE_LONG_VALUE) == 0);
        assertTrue(BASE_LONG_VALUE.compareTo(BIGGER_LONG_VALUE) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(MAX_LONG_VALUE) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(SMALLER_LONG_VALUE) > 0);
        assertTrue(BASE_LONG_VALUE.compareTo(MIN_LONG_VALUE) > 0);

        // with Double
        assertTrue(BASE_LONG_VALUE.compareTo(BASE_DOUBLE_VALUE) == 0);
        assertTrue(BASE_LONG_VALUE.compareTo(BASE_DOUBLE_VALUE) == 0);
        assertTrue(BASE_LONG_VALUE.compareTo(BIGGER_DOUBLE_VALUE1) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(BIGGER_DOUBLE_VALUE2) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(MAX_DOUBLE_VALUE) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(POSITIVE_INFINITY) < 0);
        assertTrue(BASE_LONG_VALUE.compareTo(SMALLER_DOUBLE_VALUE1) > 0);
        assertTrue(BASE_LONG_VALUE.compareTo(SMALLER_DOUBLE_VALUE2) > 0);
        assertTrue(BASE_LONG_VALUE.compareTo(MIN_DOUBLE_VALUE) > 0);
        assertTrue(BASE_LONG_VALUE.compareTo(NEGATIVE_INFINITY) > 0);

        // with String
        assertTrue(BASE_LONG_VALUE.compareTo(BASE_STRING_VALUE) < 0);
        assertFalse(BASE_LONG_VALUE.compareTo(BASE_STRING_VALUE) >= 0);

        // with Null
        assertTrue(BASE_LONG_VALUE.compareTo(NULL_VALUE) > 0);
        assertFalse(BASE_LONG_VALUE.compareTo(NULL_VALUE) <= 0);

    }

    /**
     * Comparing the Double state values with other types
     *
     */
    @Test
    public void testDoubleValueComparisons() {

        // with Integer
        assertTrue(BASE_DOUBLE_VALUE.compareTo(BASE_INT_VALUE) == 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(BIGGER_INT_VALUE) < 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(SMALLER_INT_VALUE) > 0);

        // with Long
        assertTrue(BASE_DOUBLE_VALUE.compareTo(BASE_LONG_VALUE) == 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(BIGGER_LONG_VALUE) < 0);
        assertTrue(SMALLER_DOUBLE_VALUE2.compareTo(BASE_LONG_VALUE) < 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(MAX_LONG_VALUE) < 0);
        assertTrue(BIGGER_DOUBLE_VALUE1.compareTo(SMALLER_LONG_VALUE) > 0);
        assertTrue(BIGGER_DOUBLE_VALUE2.compareTo(BASE_LONG_VALUE) > 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(MIN_LONG_VALUE) > 0);

        // with Double
        assertTrue(BASE_DOUBLE_VALUE.compareTo(BASE_DOUBLE_VALUE) == 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(BIGGER_DOUBLE_VALUE2) < 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(MAX_DOUBLE_VALUE) < 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(SMALLER_DOUBLE_VALUE2) > 0);
        assertTrue(BASE_DOUBLE_VALUE.compareTo(MIN_DOUBLE_VALUE) > 0);

        // with String
        assertTrue(BASE_DOUBLE_VALUE.compareTo(BASE_STRING_VALUE) < 0);
        assertFalse(BASE_DOUBLE_VALUE.compareTo(BASE_STRING_VALUE) >= 0);

        // with Null
        assertTrue(BASE_DOUBLE_VALUE.compareTo(NULL_VALUE) > 0);
        assertFalse(BASE_DOUBLE_VALUE.compareTo(NULL_VALUE) <= 0);

    }

    /**
     * Comparing the String state values with other types
     *
     */
    @Test
    public void testStringValueComparisons() {

        // with Integer
        assertTrue(BASE_STRING_VALUE.compareTo(BASE_INT_VALUE) > 0);
        assertFalse(BASE_STRING_VALUE.compareTo(BASE_INT_VALUE) <= 0);

        // with Long
        assertTrue(BASE_STRING_VALUE.compareTo(BASE_LONG_VALUE) > 0);
        assertFalse(BASE_STRING_VALUE.compareTo(BASE_LONG_VALUE) <= 0);

        // with Double
        assertTrue(BASE_STRING_VALUE.compareTo(BASE_DOUBLE_VALUE) > 0);
        assertFalse(BASE_STRING_VALUE.compareTo(BASE_DOUBLE_VALUE) <= 0);

        // with String
        assertTrue(BASE_STRING_VALUE.compareTo(BASE_STRING_VALUE) == 0);
        assertTrue(BASE_STRING_VALUE.compareTo(SMALLER_STRING_VALUE) > 0);
        assertTrue(BASE_STRING_VALUE.compareTo(BIGGER_STRING_VALUE) < 0);

        // with Null
        assertTrue(BASE_STRING_VALUE.compareTo(NULL_VALUE) > 0);
        assertFalse(BASE_STRING_VALUE.compareTo(NULL_VALUE) <= 0);

    }

    /**
     * Comparing the Null state value with other types
     *
     */
    @Test
    public void testNullValueComparisons() {

        // with Integer
        assertTrue(NULL_VALUE.compareTo(BASE_INT_VALUE) < 0);
        assertFalse(NULL_VALUE.compareTo(BASE_INT_VALUE) >= 0);

        // with Long
        assertTrue(NULL_VALUE.compareTo(BASE_LONG_VALUE) < 0);
        assertFalse(NULL_VALUE.compareTo(BASE_LONG_VALUE) >= 0);

        // with Double
        assertTrue(NULL_VALUE.compareTo(BASE_DOUBLE_VALUE) < 0);
        assertFalse(NULL_VALUE.compareTo(BASE_DOUBLE_VALUE) >= 0);

        // with String
        assertTrue(NULL_VALUE.compareTo(BASE_STRING_VALUE) < 0);
        assertFalse(NULL_VALUE.compareTo(BASE_STRING_VALUE) >= 0);

        // with null
        assertTrue(NULL_VALUE.compareTo(NULL_VALUE) == 0);
        assertFalse(NULL_VALUE.compareTo(NULL_VALUE) > 0);
        assertFalse(NULL_VALUE.compareTo(NULL_VALUE) < 0);

    }

}
