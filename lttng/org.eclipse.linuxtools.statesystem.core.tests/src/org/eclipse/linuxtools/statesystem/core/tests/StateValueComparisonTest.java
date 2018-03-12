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

import static org.junit.Assert.*;

import org.eclipse.linuxtools.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.TmfStateValuesComparison;
import org.eclipse.linuxtools.tmf.core.TmfStateValuesComparison.ComparisonOperator;
import org.junit.Test;

/**
 * Unit test for compare() method in the State System
 *
 * @author Naser Ezzati
 *
 */
public class StateValueComparisonTest {

    /**
     * Compare two state values based on the given comparison operation.
     */
    @Test
    public void testValueComparisons()
    {
        /*
         * Comparison of two state values with ComparisonOperator.EQ ( = ) operator
         */
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(10), TmfStateValue.newValueInt(10), ComparisonOperator.EQ));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(10), TmfStateValue.newValueLong(10), ComparisonOperator.EQ));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(10), TmfStateValue.newValueDouble(10), ComparisonOperator.EQ));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueString("10"), TmfStateValue.newValueString("10"), ComparisonOperator.EQ));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueString("abcd"), TmfStateValue.newValueString("abcd"), ComparisonOperator.EQ));

        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(20), TmfStateValue.newValueInt(10), ComparisonOperator.EQ));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(20), TmfStateValue.newValueLong(10), ComparisonOperator.EQ));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(10), TmfStateValue.newValueDouble(20), ComparisonOperator.EQ));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueString("abcd"), TmfStateValue.newValueString("abce"), ComparisonOperator.EQ));

        /*
         * Comparison of two state values with ComparisonOperator.NE ( != ) operator
         */
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(10), TmfStateValue.newValueInt(10), ComparisonOperator.NE));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(10), TmfStateValue.newValueLong(10), ComparisonOperator.NE));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(10), TmfStateValue.newValueDouble(10), ComparisonOperator.NE));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueString("10"), TmfStateValue.newValueString("10"), ComparisonOperator.NE));


        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(20), TmfStateValue.newValueInt(10), ComparisonOperator.NE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(20), TmfStateValue.newValueLong(10), ComparisonOperator.NE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(10), TmfStateValue.newValueDouble(20), ComparisonOperator.NE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueString("abcd"), TmfStateValue.newValueString("abce"), ComparisonOperator.NE));

        /*
         * Comparison of two state values with ComparisonOperator.GE ( >= ) operator
         */
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(10), TmfStateValue.newValueInt(10), ComparisonOperator.GE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(10), TmfStateValue.newValueLong(10), ComparisonOperator.GE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(10), TmfStateValue.newValueDouble(10), ComparisonOperator.GE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueString("10"), TmfStateValue.newValueString("10"), ComparisonOperator.GE));

        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(20), TmfStateValue.newValueInt(10), ComparisonOperator.GE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(20), TmfStateValue.newValueLong(10), ComparisonOperator.GE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(20), TmfStateValue.newValueDouble(10), ComparisonOperator.GE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueString("abcd"), TmfStateValue.newValueString("abcc"), ComparisonOperator.GE));

        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(10), TmfStateValue.newValueInt(20), ComparisonOperator.GE));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(10), TmfStateValue.newValueLong(20), ComparisonOperator.GE));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(10), TmfStateValue.newValueDouble(20), ComparisonOperator.GE));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueString("10"), TmfStateValue.newValueString("20"), ComparisonOperator.GE));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueString("abcd"), TmfStateValue.newValueString("abce"), ComparisonOperator.GE));

        /*
         * Comparison of two state values with ComparisonOperator.GT ( > ) operator
         */
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(10), TmfStateValue.newValueInt(10), ComparisonOperator.GT));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(10), TmfStateValue.newValueLong(10), ComparisonOperator.GT));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(10), TmfStateValue.newValueDouble(10), ComparisonOperator.GT));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueString("10"), TmfStateValue.newValueString("10"), ComparisonOperator.GT));

        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(20), TmfStateValue.newValueInt(10), ComparisonOperator.GT));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(20), TmfStateValue.newValueLong(10), ComparisonOperator.GT));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(20), TmfStateValue.newValueDouble(10), ComparisonOperator.GT));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueString("abcd"), TmfStateValue.newValueString("abcc"), ComparisonOperator.GT));

        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(10), TmfStateValue.newValueInt(20), ComparisonOperator.GT));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(10), TmfStateValue.newValueLong(20), ComparisonOperator.GT));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(10), TmfStateValue.newValueDouble(20), ComparisonOperator.GT));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueString("10"), TmfStateValue.newValueString("20"), ComparisonOperator.GT));

        /*
         * Comparison of two state values with ComparisonOperator.LE ( <= ) operator
         */
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(10), TmfStateValue.newValueInt(10), ComparisonOperator.LE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(10), TmfStateValue.newValueLong(10), ComparisonOperator.LE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(10), TmfStateValue.newValueDouble(10), ComparisonOperator.LE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueString("10"), TmfStateValue.newValueString("10"), ComparisonOperator.LE));

        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(20), TmfStateValue.newValueInt(10), ComparisonOperator.LE));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(20), TmfStateValue.newValueLong(10), ComparisonOperator.LE));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(20), TmfStateValue.newValueDouble(10), ComparisonOperator.LE));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueString("20"), TmfStateValue.newValueString("10"), ComparisonOperator.LE));

        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(10), TmfStateValue.newValueInt(20), ComparisonOperator.LE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(10), TmfStateValue.newValueLong(20), ComparisonOperator.LE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(10), TmfStateValue.newValueDouble(20), ComparisonOperator.LE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueString("10"), TmfStateValue.newValueString("20"), ComparisonOperator.LE));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueString("abcd"), TmfStateValue.newValueString("abce"), ComparisonOperator.LE));

        /*
         * Comparison of two state values with ComparisonOperator.LT ( < ) operator
         */
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(10), TmfStateValue.newValueInt(10), ComparisonOperator.LT));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(10), TmfStateValue.newValueLong(10), ComparisonOperator.LT));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(10), TmfStateValue.newValueDouble(10), ComparisonOperator.LT));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueString("10"), TmfStateValue.newValueString("10"), ComparisonOperator.LT));

        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(20), TmfStateValue.newValueInt(10), ComparisonOperator.LT));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(20), TmfStateValue.newValueLong(10), ComparisonOperator.LT));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(20), TmfStateValue.newValueDouble(10), ComparisonOperator.LT));
        assertEquals(false, TmfStateValuesComparison.compare(TmfStateValue.newValueString("20"), TmfStateValue.newValueString("10"), ComparisonOperator.LT));

        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueInt(10), TmfStateValue.newValueInt(20), ComparisonOperator.LT));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueLong(10), TmfStateValue.newValueLong(20), ComparisonOperator.LT));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueDouble(10), TmfStateValue.newValueDouble(20), ComparisonOperator.LT));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueString("10"), TmfStateValue.newValueString("20"), ComparisonOperator.LT));
        assertEquals(true, TmfStateValuesComparison.compare(TmfStateValue.newValueString("abcd"), TmfStateValue.newValueString("abce"), ComparisonOperator.LT));

    }
}
