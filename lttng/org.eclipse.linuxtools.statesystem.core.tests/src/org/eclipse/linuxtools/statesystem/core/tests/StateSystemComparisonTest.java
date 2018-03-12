/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Naser Ezzati - Initial API and implementation
 ******************************************************************************/
package org.eclipse.linuxtools.statesystem.core.tests;

import static org.junit.Assert.*;

import org.eclipse.linuxtools.statesystem.core.statevalue.TmfStateValue;
import org.junit.Test;

/**
 * Unit test for compare() method in the State System
 *
 * @author Naser Ezzati
 *
 */
public class StateSystemComparisonTest {



    /**
     * Compare two state values based on the given comparison operation.
     */
    @Test
    public void testValueComparisons()
    {
        /*
         * Comparison of two state values with EQ ( = ) operator
         */
        assertEquals(true, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(10), "EQ"));
        assertEquals(true, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(10), "EQ"));
        assertEquals(true, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(10), "EQ"));
        assertEquals(true, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("10"), "EQ"));
        assertEquals(true, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abcd"), "EQ"));

        assertEquals(false, TmfStateValue.newValueInt(20).compare(TmfStateValue.newValueInt(10), "EQ"));
        assertEquals(false, TmfStateValue.newValueLong(20).compare(TmfStateValue.newValueLong(10), "EQ"));
        assertEquals(false, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(20), "EQ"));
        assertEquals(false, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abce"), "EQ"));

        /*
         * Comparison of two state values with NE ( != ) operator
         */
        assertEquals(false, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(10), "NE"));
        assertEquals(false, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(10), "NE"));
        assertEquals(false, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(10), "NE"));
        assertEquals(false, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("10"), "NE"));

        assertEquals(true, TmfStateValue.newValueInt(20).compare(TmfStateValue.newValueInt(10), "NE"));
        assertEquals(true, TmfStateValue.newValueLong(20).compare(TmfStateValue.newValueLong(10), "NE"));
        assertEquals(true, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(20), "NE"));
        assertEquals(true, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("20"), "NE"));
        assertEquals(true, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abce"), "NE"));

        /*
         * Comparison of two state values with GE ( >= ) operator
         */
        assertEquals(true, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(10), "GE"));
        assertEquals(true, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(10), "GE"));
        assertEquals(true, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(10), "GE"));
        assertEquals(true, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("10"), "GE"));

        assertEquals(true, TmfStateValue.newValueInt(20).compare(TmfStateValue.newValueInt(10), "GE"));
        assertEquals(true, TmfStateValue.newValueLong(20).compare(TmfStateValue.newValueLong(10), "GE"));
        assertEquals(true, TmfStateValue.newValueDouble(20).compare(TmfStateValue.newValueDouble(10), "GE"));
        assertEquals(true, TmfStateValue.newValueString("20").compare(TmfStateValue.newValueString("10"), "GE"));
        assertEquals(true, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abcc"), "GE"));

        assertEquals(false, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(20), "GE"));
        assertEquals(false, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(20), "GE"));
        assertEquals(false, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(20), "GE"));
        assertEquals(false, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("20"), "GE"));
        assertEquals(false, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abce"), "GE"));

        /*
         * Comparison of two state values with GT ( > ) operator
         */
        assertEquals(false, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(10), "GT"));
        assertEquals(false, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(10), "GT"));
        assertEquals(false, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(10), "GT"));
        assertEquals(false, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("10"), "GT"));

        assertEquals(true, TmfStateValue.newValueInt(20).compare(TmfStateValue.newValueInt(10), "GT"));
        assertEquals(true, TmfStateValue.newValueLong(20).compare(TmfStateValue.newValueLong(10), "GT"));
        assertEquals(true, TmfStateValue.newValueDouble(20).compare(TmfStateValue.newValueDouble(10), "GT"));
        assertEquals(true, TmfStateValue.newValueString("20").compare(TmfStateValue.newValueString("10"), "GT"));
        assertEquals(true, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abcc"), "GT"));

        assertEquals(false, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(20), "GT"));
        assertEquals(false, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(20), "GT"));
        assertEquals(false, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(20), "GT"));
        assertEquals(false, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("20"), "GT"));
        assertEquals(false, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abce"), "GT"));

        /*
         * Comparison of two state values with LE ( <= ) operator
         */
        assertEquals(true, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(10), "LE"));
        assertEquals(true, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(10), "LE"));
        assertEquals(true, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(10), "LE"));
        assertEquals(true, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("10"), "LE"));

        assertEquals(false, TmfStateValue.newValueInt(20).compare(TmfStateValue.newValueInt(10), "LE"));
        assertEquals(false, TmfStateValue.newValueLong(20).compare(TmfStateValue.newValueLong(10), "LE"));
        assertEquals(false, TmfStateValue.newValueDouble(20).compare(TmfStateValue.newValueDouble(10), "LE"));
        assertEquals(false, TmfStateValue.newValueString("20").compare(TmfStateValue.newValueString("10"), "LE"));
        assertEquals(false, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abcc"), "LE"));

        assertEquals(true, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(20), "LE"));
        assertEquals(true, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(20), "LE"));
        assertEquals(true, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(20), "LE"));
        assertEquals(true, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("20"), "LE"));
        assertEquals(true, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abce"), "LE"));

        /*
         * Comparison of two state values with LT ( < ) operator
         */
        assertEquals(false, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(10), "LT"));
        assertEquals(false, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(10), "LT"));
        assertEquals(false, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(10), "LT"));
        assertEquals(false, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("10"), "LT"));

        assertEquals(false, TmfStateValue.newValueInt(20).compare(TmfStateValue.newValueInt(10), "LT"));
        assertEquals(false, TmfStateValue.newValueLong(20).compare(TmfStateValue.newValueLong(10), "LT"));
        assertEquals(false, TmfStateValue.newValueDouble(20).compare(TmfStateValue.newValueDouble(10), "LT"));
        assertEquals(false, TmfStateValue.newValueString("20").compare(TmfStateValue.newValueString("10"), "LT"));
        assertEquals(false, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abcc"), "abcc"));

        assertEquals(true, TmfStateValue.newValueInt(10).compare(TmfStateValue.newValueInt(20), "LT"));
        assertEquals(true, TmfStateValue.newValueLong(10).compare(TmfStateValue.newValueLong(20), "LT"));
        assertEquals(true, TmfStateValue.newValueDouble(10).compare(TmfStateValue.newValueDouble(20), "LT"));
        assertEquals(true, TmfStateValue.newValueString("10").compare(TmfStateValue.newValueString("20"), "LT"));
        assertEquals(true, TmfStateValue.newValueString("abcd").compare(TmfStateValue.newValueString("abce"), "LT"));

    }
}
