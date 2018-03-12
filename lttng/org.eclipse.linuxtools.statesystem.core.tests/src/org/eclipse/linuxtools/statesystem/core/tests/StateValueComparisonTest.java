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

import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue.ComparisonOperator;
import org.eclipse.linuxtools.statesystem.core.statevalue.TmfStateValue;
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
        assertEquals(true, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(10), ComparisonOperator.EQ));
        assertEquals(true, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(10), ComparisonOperator.EQ));
        assertEquals(true, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(10), ComparisonOperator.EQ));
        assertEquals(true, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("10"), ComparisonOperator.EQ));
        assertEquals(true, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abcd"), ComparisonOperator.EQ));

        assertEquals(false, TmfStateValue.newValueInt(20).compare(TmfStateValue.newValueInt(10), ComparisonOperator.EQ));
        assertEquals(false, TmfStateValue.newValueLong(20).compare(TmfStateValue.newValueLong(10), ComparisonOperator.EQ));
        assertEquals(false, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(20), ComparisonOperator.EQ));
        assertEquals(false, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abce"), ComparisonOperator.EQ));

        /*
         * Comparison of two state values with ComparisonOperator.NE ( != ) operator
         */
        assertEquals(false, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(10), ComparisonOperator.NE));
        assertEquals(false, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(10), ComparisonOperator.NE));
        assertEquals(false, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(10), ComparisonOperator.NE));
        assertEquals(false, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("10"), ComparisonOperator.NE));

        assertEquals(true, TmfStateValue.newValueInt(20).compare(TmfStateValue.newValueInt(10), ComparisonOperator.NE));
        assertEquals(true, TmfStateValue.newValueLong(20).compare(TmfStateValue.newValueLong(10), ComparisonOperator.NE));
        assertEquals(true, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(20), ComparisonOperator.NE));
        assertEquals(true, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("20"), ComparisonOperator.NE));
        assertEquals(true, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abce"), ComparisonOperator.NE));

        /*
         * Comparison of two state values with ComparisonOperator.GE ( >= ) operator
         */
        assertEquals(true, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(10), ComparisonOperator.GE));
        assertEquals(true, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(10), ComparisonOperator.GE));
        assertEquals(true, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(10), ComparisonOperator.GE));
        assertEquals(true, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("10"), ComparisonOperator.GE));

        assertEquals(true, TmfStateValue.newValueInt(20).compare(TmfStateValue.newValueInt(10), ComparisonOperator.GE));
        assertEquals(true, TmfStateValue.newValueLong(20).compare(TmfStateValue.newValueLong(10), ComparisonOperator.GE));
        assertEquals(true, TmfStateValue.newValueDouble(20).compare(TmfStateValue.newValueDouble(10), ComparisonOperator.GE));
        assertEquals(true, TmfStateValue.newValueString("20").compare(TmfStateValue.newValueString("10"), ComparisonOperator.GE));
        assertEquals(true, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abcc"), ComparisonOperator.GE));

        assertEquals(false, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(20), ComparisonOperator.GE));
        assertEquals(false, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(20), ComparisonOperator.GE));
        assertEquals(false, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(20), ComparisonOperator.GE));
        assertEquals(false, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("20"), ComparisonOperator.GE));
        assertEquals(false, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abce"), ComparisonOperator.GE));

        /*
         * Comparison of two state values with ComparisonOperator.GT ( > ) operator
         */
        assertEquals(false, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(10), ComparisonOperator.GT));
        assertEquals(false, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(10), ComparisonOperator.GT));
        assertEquals(false, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(10), ComparisonOperator.GT));
        assertEquals(false, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("10"), ComparisonOperator.GT));

        assertEquals(true, TmfStateValue.newValueInt(20).compare(TmfStateValue.newValueInt(10), ComparisonOperator.GT));
        assertEquals(true, TmfStateValue.newValueLong(20).compare(TmfStateValue.newValueLong(10), ComparisonOperator.GT));
        assertEquals(true, TmfStateValue.newValueDouble(20).compare(TmfStateValue.newValueDouble(10), ComparisonOperator.GT));
        assertEquals(true, TmfStateValue.newValueString("20").compare(TmfStateValue.newValueString("10"), ComparisonOperator.GT));
        assertEquals(true, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abcc"), ComparisonOperator.GT));

        assertEquals(false, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(20), ComparisonOperator.GT));
        assertEquals(false, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(20), ComparisonOperator.GT));
        assertEquals(false, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(20), ComparisonOperator.GT));
        assertEquals(false, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("20"), ComparisonOperator.GT));
        assertEquals(false, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abce"), ComparisonOperator.GT));

        /*
         * Comparison of two state values with ComparisonOperator.LE ( <= ) operator
         */
        assertEquals(true, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(10), ComparisonOperator.LE));
        assertEquals(true, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(10), ComparisonOperator.LE));
        assertEquals(true, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(10), ComparisonOperator.LE));
        assertEquals(true, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("10"), ComparisonOperator.LE));

        assertEquals(false, TmfStateValue.newValueInt(20).compare(TmfStateValue.newValueInt(10), ComparisonOperator.LE));
        assertEquals(false, TmfStateValue.newValueLong(20).compare(TmfStateValue.newValueLong(10), ComparisonOperator.LE));
        assertEquals(false, TmfStateValue.newValueDouble(20).compare(TmfStateValue.newValueDouble(10), ComparisonOperator.LE));
        assertEquals(false, TmfStateValue.newValueString("20").compare(TmfStateValue.newValueString("10"), ComparisonOperator.LE));
        assertEquals(false, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abcc"), ComparisonOperator.LE));

        assertEquals(true, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(20), ComparisonOperator.LE));
        assertEquals(true, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(20), ComparisonOperator.LE));
        assertEquals(true, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(20), ComparisonOperator.LE));
        assertEquals(true, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("20"), ComparisonOperator.LE));
        assertEquals(true, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abce"), ComparisonOperator.LE));

        /*
         * Comparison of two state values with ComparisonOperator.LT ( < ) operator
         */
        assertEquals(false, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(10), ComparisonOperator.LT));
        assertEquals(false, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(10), ComparisonOperator.LT));
        assertEquals(false, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(10), ComparisonOperator.LT));
        assertEquals(false, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("10"), ComparisonOperator.LT));

        assertEquals(false, TmfStateValue.newValueInt(20).compare(TmfStateValue.newValueInt(10), ComparisonOperator.LT));
        assertEquals(false, TmfStateValue.newValueLong(20).compare(TmfStateValue.newValueLong(10), ComparisonOperator.LT));
        assertEquals(false, TmfStateValue.newValueDouble(20).compare(TmfStateValue.newValueDouble(10), ComparisonOperator.LT));
        assertEquals(false, TmfStateValue.newValueString("20").compare(TmfStateValue.newValueString("10"), ComparisonOperator.LT));
        assertEquals(false, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abcc"), ComparisonOperator.LT));

        assertEquals(true, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(20), ComparisonOperator.LT));
        assertEquals(true, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(20), ComparisonOperator.LT));
        assertEquals(true, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(20), ComparisonOperator.LT));
        assertEquals(true, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("20"), ComparisonOperator.LT));
        assertEquals(true, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abce"), ComparisonOperator.LT));

    }
}
