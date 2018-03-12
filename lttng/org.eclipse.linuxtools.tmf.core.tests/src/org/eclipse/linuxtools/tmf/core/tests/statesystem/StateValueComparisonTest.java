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
package org.eclipse.linuxtools.tmf.core.tests.statesystem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateValuesComparison;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateValuesComparison.ComparisonOperator;
import org.junit.Test;

/**
 * Unit tests for the {@link TmfStateValuesComparison} class
 *
 * @author Naser Ezzati
 *
 */
public class StateValueComparisonTest {

    /* State values that will be used */
    private static final @NonNull ITmfStateValue baseIntValue = TmfStateValue.newValueInt(10);
    private static final @NonNull ITmfStateValue biggerIntValue = TmfStateValue.newValueInt(20);

    private static final @NonNull ITmfStateValue baseLongValue = TmfStateValue.newValueLong(10);
    private static final @NonNull ITmfStateValue biggerLongValue = TmfStateValue.newValueLong(20);

    private static final @NonNull ITmfStateValue baseDoubleValue = TmfStateValue.newValueDouble(10.00);
    private static final @NonNull ITmfStateValue biggerDoubleValue1 = TmfStateValue.newValueDouble(20.00);

    private static final @NonNull ITmfStateValue baseStringValue = TmfStateValue.newValueString("D");
    private static final @NonNull ITmfStateValue biggerStringValue = TmfStateValue.newValueString("Z");
    private static final @NonNull ITmfStateValue smallerStringValue = TmfStateValue.newValueString("A");

    private static final @NonNull ITmfStateValue nullValue = TmfStateValue.nullValue();

    /**
     * Test that the {@link TmfStateValuesComparison#compare(ITmfStateValue, ITmfStateValue, ComparisonOperator)}
     * method compares correctly two state values based on the given comparison
     * operator.
     *
     */
    @Test
    public void testValueComparisons()
    {
        /*
         * Comparison of two state values with ComparisonOperator.EQ ( = )
         * operator
         */
        assertTrue(TmfStateValuesComparison.compare(baseIntValue, baseIntValue, ComparisonOperator.EQ));
        assertTrue(TmfStateValuesComparison.compare(baseLongValue, baseLongValue, ComparisonOperator.EQ));
        assertTrue(TmfStateValuesComparison.compare(baseDoubleValue, baseDoubleValue, ComparisonOperator.EQ));
        assertTrue(TmfStateValuesComparison.compare(baseStringValue, baseStringValue, ComparisonOperator.EQ));
        assertTrue(TmfStateValuesComparison.compare(nullValue, nullValue, ComparisonOperator.EQ));

        assertFalse(TmfStateValuesComparison.compare(biggerIntValue, baseIntValue, ComparisonOperator.EQ));
        assertFalse(TmfStateValuesComparison.compare(biggerLongValue, baseLongValue, ComparisonOperator.EQ));
        assertFalse(TmfStateValuesComparison.compare(baseDoubleValue, biggerDoubleValue1, ComparisonOperator.EQ));
        assertFalse(TmfStateValuesComparison.compare(baseStringValue, biggerStringValue, ComparisonOperator.EQ));
        assertFalse(TmfStateValuesComparison.compare(baseIntValue, nullValue, ComparisonOperator.EQ));

        /*
         * Comparison of two state values with ComparisonOperator.NE ( != )
         * operator
         */
        assertFalse(TmfStateValuesComparison.compare(baseIntValue, baseIntValue, ComparisonOperator.NE));
        assertFalse(TmfStateValuesComparison.compare(baseLongValue, baseLongValue, ComparisonOperator.NE));
        assertFalse(TmfStateValuesComparison.compare(baseDoubleValue, baseDoubleValue, ComparisonOperator.NE));
        assertFalse(TmfStateValuesComparison.compare(baseStringValue, baseStringValue, ComparisonOperator.NE));
        assertFalse(TmfStateValuesComparison.compare(nullValue, nullValue, ComparisonOperator.NE));

        assertTrue(TmfStateValuesComparison.compare(biggerIntValue, baseIntValue, ComparisonOperator.NE));
        assertTrue(TmfStateValuesComparison.compare(biggerLongValue, baseLongValue, ComparisonOperator.NE));
        assertTrue(TmfStateValuesComparison.compare(baseDoubleValue, biggerDoubleValue1, ComparisonOperator.NE));
        assertTrue(TmfStateValuesComparison.compare(baseStringValue, biggerStringValue, ComparisonOperator.NE));
        assertTrue(TmfStateValuesComparison.compare(baseIntValue, nullValue, ComparisonOperator.NE));

        /*
         * Comparison of two state values with ComparisonOperator.GE ( >= )
         * operator
         */
        assertTrue(TmfStateValuesComparison.compare(baseIntValue, baseIntValue, ComparisonOperator.GE));
        assertTrue(TmfStateValuesComparison.compare(baseLongValue, baseLongValue, ComparisonOperator.GE));
        assertTrue(TmfStateValuesComparison.compare(baseDoubleValue, baseDoubleValue, ComparisonOperator.GE));
        assertTrue(TmfStateValuesComparison.compare(baseStringValue, baseStringValue, ComparisonOperator.GE));
        assertTrue(TmfStateValuesComparison.compare(baseIntValue, nullValue, ComparisonOperator.GE));

        assertTrue(TmfStateValuesComparison.compare(biggerIntValue, baseIntValue, ComparisonOperator.GE));
        assertTrue(TmfStateValuesComparison.compare(biggerLongValue, baseLongValue, ComparisonOperator.GE));
        assertTrue(TmfStateValuesComparison.compare(biggerDoubleValue1, baseDoubleValue, ComparisonOperator.GE));
        assertTrue(TmfStateValuesComparison.compare(baseStringValue, smallerStringValue, ComparisonOperator.GE));
        assertTrue(TmfStateValuesComparison.compare(baseStringValue, biggerIntValue, ComparisonOperator.GE));

        assertFalse(TmfStateValuesComparison.compare(baseIntValue, biggerIntValue, ComparisonOperator.GE));
        assertFalse(TmfStateValuesComparison.compare(baseLongValue, biggerLongValue, ComparisonOperator.GE));
        assertFalse(TmfStateValuesComparison.compare(baseDoubleValue, biggerDoubleValue1, ComparisonOperator.GE));
        assertFalse(TmfStateValuesComparison.compare(baseStringValue, biggerStringValue, ComparisonOperator.GE));
        assertFalse(TmfStateValuesComparison.compare(biggerIntValue, biggerStringValue, ComparisonOperator.GE));

        /*
         * Comparison of two state values with ComparisonOperator.GT ( > )
         * operator
         */
        assertFalse(TmfStateValuesComparison.compare(baseIntValue, baseIntValue, ComparisonOperator.GT));
        assertFalse(TmfStateValuesComparison.compare(baseLongValue, baseLongValue, ComparisonOperator.GT));
        assertFalse(TmfStateValuesComparison.compare(baseDoubleValue, baseDoubleValue, ComparisonOperator.GT));
        assertFalse(TmfStateValuesComparison.compare(baseStringValue, baseStringValue, ComparisonOperator.GT));
        assertTrue(TmfStateValuesComparison.compare(baseIntValue, nullValue, ComparisonOperator.GT));

        assertTrue(TmfStateValuesComparison.compare(biggerIntValue, baseIntValue, ComparisonOperator.GT));
        assertTrue(TmfStateValuesComparison.compare(biggerLongValue, baseLongValue, ComparisonOperator.GT));
        assertTrue(TmfStateValuesComparison.compare(biggerDoubleValue1, baseDoubleValue, ComparisonOperator.GT));
        assertTrue(TmfStateValuesComparison.compare(baseStringValue, smallerStringValue, ComparisonOperator.GT));
        assertTrue(TmfStateValuesComparison.compare(baseStringValue, nullValue, ComparisonOperator.GT));

        assertFalse(TmfStateValuesComparison.compare(baseIntValue, biggerIntValue, ComparisonOperator.GT));
        assertFalse(TmfStateValuesComparison.compare(baseLongValue, biggerLongValue, ComparisonOperator.GT));
        assertFalse(TmfStateValuesComparison.compare(baseDoubleValue, biggerDoubleValue1, ComparisonOperator.GT));
        assertFalse(TmfStateValuesComparison.compare(baseStringValue, biggerStringValue, ComparisonOperator.GT));
        assertFalse(TmfStateValuesComparison.compare(nullValue, biggerIntValue, ComparisonOperator.GT));

        /*
         * Comparison of two state values with ComparisonOperator.LE ( <= )
         * operator
         */
        assertTrue(TmfStateValuesComparison.compare(baseIntValue, baseIntValue, ComparisonOperator.LE));
        assertTrue(TmfStateValuesComparison.compare(baseLongValue, baseLongValue, ComparisonOperator.LE));
        assertTrue(TmfStateValuesComparison.compare(baseDoubleValue, baseDoubleValue, ComparisonOperator.LE));
        assertTrue(TmfStateValuesComparison.compare(baseStringValue, baseStringValue, ComparisonOperator.LE));
        assertTrue(TmfStateValuesComparison.compare(nullValue, biggerIntValue, ComparisonOperator.LE));
        assertTrue(TmfStateValuesComparison.compare(biggerIntValue, baseStringValue, ComparisonOperator.LE));

        assertFalse(TmfStateValuesComparison.compare(biggerIntValue, baseIntValue, ComparisonOperator.LE));
        assertFalse(TmfStateValuesComparison.compare(biggerLongValue, baseLongValue, ComparisonOperator.LE));
        assertFalse(TmfStateValuesComparison.compare(biggerDoubleValue1, baseDoubleValue, ComparisonOperator.LE));
        assertFalse(TmfStateValuesComparison.compare(biggerStringValue, baseStringValue, ComparisonOperator.LE));
        assertFalse(TmfStateValuesComparison.compare(biggerStringValue, biggerIntValue, ComparisonOperator.LE));

        assertTrue(TmfStateValuesComparison.compare(baseIntValue, biggerIntValue, ComparisonOperator.LE));
        assertTrue(TmfStateValuesComparison.compare(baseLongValue, biggerLongValue, ComparisonOperator.LE));
        assertTrue(TmfStateValuesComparison.compare(baseDoubleValue, biggerDoubleValue1, ComparisonOperator.LE));
        assertTrue(TmfStateValuesComparison.compare(baseStringValue, biggerStringValue, ComparisonOperator.LE));
        assertTrue(TmfStateValuesComparison.compare(baseStringValue, biggerStringValue, ComparisonOperator.LE));

        /*
         * Comparison of two state values with ComparisonOperator.LT ( < )
         * operator
         */
        assertFalse(TmfStateValuesComparison.compare(baseIntValue, baseIntValue, ComparisonOperator.LT));
        assertFalse(TmfStateValuesComparison.compare(baseLongValue, baseLongValue, ComparisonOperator.LT));
        assertFalse(TmfStateValuesComparison.compare(baseDoubleValue, baseDoubleValue, ComparisonOperator.LT));
        assertFalse(TmfStateValuesComparison.compare(baseStringValue, baseStringValue, ComparisonOperator.LT));
        assertFalse(TmfStateValuesComparison.compare(baseIntValue, nullValue, ComparisonOperator.LT));

        assertFalse(TmfStateValuesComparison.compare(biggerIntValue, baseIntValue, ComparisonOperator.LT));
        assertFalse(TmfStateValuesComparison.compare(biggerLongValue, baseLongValue, ComparisonOperator.LT));
        assertFalse(TmfStateValuesComparison.compare(biggerDoubleValue1, baseDoubleValue, ComparisonOperator.LT));
        assertFalse(TmfStateValuesComparison.compare(biggerStringValue, baseStringValue, ComparisonOperator.LT));

        assertTrue(TmfStateValuesComparison.compare(baseIntValue, biggerIntValue, ComparisonOperator.LT));
        assertTrue(TmfStateValuesComparison.compare(baseLongValue, biggerLongValue, ComparisonOperator.LT));
        assertTrue(TmfStateValuesComparison.compare(baseDoubleValue, biggerDoubleValue1, ComparisonOperator.LT));
        assertTrue(TmfStateValuesComparison.compare(baseStringValue, biggerStringValue, ComparisonOperator.LT));
        assertTrue(TmfStateValuesComparison.compare(baseStringValue, biggerStringValue, ComparisonOperator.LT));
        assertTrue(TmfStateValuesComparison.compare(nullValue, baseIntValue, ComparisonOperator.LT));

    }
}
