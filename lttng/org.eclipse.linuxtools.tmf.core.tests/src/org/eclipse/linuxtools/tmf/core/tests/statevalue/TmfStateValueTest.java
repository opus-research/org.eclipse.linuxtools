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

import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.junit.Test;

/**
 * Test for the {@link ITmfStateValue}.
 *
 * @author ekadkou
 *
 */
public class TmfStateValueTest {

    private final static int greater = 1;
    private final static int EqualOrDifferentType = 0;
    private final static int lower = -1;

    /* State values that will be used */
    private final static ITmfStateValue value1 = TmfStateValue.newValueString("guitare");
    private final static ITmfStateValue value2 = TmfStateValue.newValueInt(10);
    private final static ITmfStateValue value3 = TmfStateValue.nullValue();
    private final static ITmfStateValue value4 = TmfStateValue.newValueString("guitaro");
    private final static ITmfStateValue value5 = TmfStateValue.newValueLong(Long.MAX_VALUE);
    private final static ITmfStateValue value6 = TmfStateValue.newValueInt(20);
    private final static ITmfStateValue value7 = TmfStateValue.nullValue();
    private final static ITmfStateValue value8 = TmfStateValue.newValueLong(9978375435753453L);

    /**
     * test comparing stateValues
     */
    @Test
    public void testValue1CompareTo() {
        int returnValue;
        returnValue = value1.compareTo(value1);
        assertEquals(EqualOrDifferentType, returnValue);
        returnValue = value1.compareTo(value2);
        assertEquals(EqualOrDifferentType, returnValue);
        returnValue = value1.compareTo(value3);
        assertEquals(greater, returnValue);
        returnValue = value1.compareTo(value4);
        assertEquals(lower, returnValue);
        returnValue = value1.compareTo(value5);
        assertEquals(EqualOrDifferentType, returnValue);
    }

    /**
     * test comparing stateValues
     */
    @Test
    public void testValue2CompareTo() {
        int returnValue = value2.compareTo(value1);
        assertEquals(EqualOrDifferentType, returnValue);

        returnValue = value2.compareTo(value2);
        assertEquals(EqualOrDifferentType, returnValue);

        returnValue = value2.compareTo(value3);
        assertEquals(greater, returnValue);

        returnValue = value2.compareTo(value6);
        assertEquals(lower, returnValue);

        returnValue = value2.compareTo(value8);
        assertEquals(EqualOrDifferentType, returnValue);
    }

    /**
     * test comparing stateValues
     */
    @Test
    public void testValue3CompareTo() {
        int returnValue = value3.compareTo(value1);
        assertEquals(lower, returnValue);

        returnValue = value3.compareTo(value2);
        assertEquals(lower, returnValue);

        returnValue = value3.compareTo(value7);
        assertEquals(EqualOrDifferentType, returnValue);

        returnValue = value3.compareTo(value4);
        assertEquals(lower, returnValue);
    }

    /**
     * test comparing stateValues
     */
    @Test
    public void testValue5CompareTo() {
        int returnValue = value5.compareTo(value1);
        assertEquals(EqualOrDifferentType, returnValue);

        returnValue = value5.compareTo(value2);
        assertEquals(EqualOrDifferentType, returnValue);

        returnValue = value5.compareTo(value3);
        assertEquals(greater, returnValue);

        returnValue = value5.compareTo(value5);
        assertEquals(EqualOrDifferentType, returnValue);

        returnValue = value5.compareTo(value8);
        assertEquals(greater, returnValue);
    }
}
