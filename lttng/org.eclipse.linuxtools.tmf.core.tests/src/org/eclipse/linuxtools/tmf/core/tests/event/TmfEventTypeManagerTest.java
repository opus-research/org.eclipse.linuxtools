/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.event;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEventTypeManager;

/**
 * Test suite for the TmfEventTypeManager class.
 */
@SuppressWarnings("nls")
public class TmfEventTypeManagerTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private static final TmfEventTypeManager fInstance = TmfEventTypeManager.getInstance();

    private final String fContext1 = "JUnit context 1";
    private final String fContext2 = "JUnit context 2";

    private final String fTypeId1 = "Some type";
    private final String fTypeId2 = "Some other type";
    private final String fTypeId3 = "Yet another type";
    private final String fTypeId4 = "A final type";

    private final String fLabel0 = "label1";
    private final String fLabel1 = "label2";

    private final String[] fLabels0 = new String[] { };
    private final String[] fLabels1 = new String[] { fLabel0, fLabel1 };
    private final String[] fLabels2 = new String[] { fLabel1, fLabel0, fLabel1 };

    private final TmfEventType fType0 = new TmfEventType(fContext1, fTypeId1, TmfEventField.makeRoot(fLabels0));
    private final TmfEventType fType1 = new TmfEventType(fContext1, fTypeId2, TmfEventField.makeRoot(fLabels1));
    private final TmfEventType fType2 = new TmfEventType(fContext2, fTypeId3, TmfEventField.makeRoot(fLabels2));
    private final TmfEventType fType3 = new TmfEventType(fContext2, fTypeId4, TmfEventField.makeRoot(fLabels1));

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
    public TmfEventTypeManagerTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    public void testGetContexts() {
        fInstance.clear();
        fInstance.add(fContext1, fType0);
        fInstance.add(fContext1, fType1);
        fInstance.add(fContext2, fType2);
        fInstance.add(fContext2, fType3);

        final String[] contexts = fInstance.getContexts();
        Arrays.sort(contexts);
        assertEquals("getContexts", 2, contexts.length);
        assertEquals("getContexts", fContext1, contexts[0]);
        assertEquals("getContexts", fContext2, contexts[1]);
    }

    public void testGetTypes() {
        fInstance.clear();
        fInstance.add(fContext1, fType0);
        fInstance.add(fContext1, fType1);
        fInstance.add(fContext2, fType2);
        fInstance.add(fContext2, fType3);

        ITmfEventType[] types = fInstance.getTypes(fContext1);
        assertEquals("getTypes", 2, types.length);
        if (fType0 == types[0])
            assertSame("getTypes", fType1, types[1]);
        else {
            assertSame("getTypes", fType0, types[1]);
            assertSame("getTypes", fType1, types[0]);
        }

        types = fInstance.getTypes(fContext2);
        assertEquals("getTypes", 2, types.length);
        if (fType2 == types[0])
            assertSame("getTypes", fType3, types[1]);
        else {
            assertSame("getTypes", fType2, types[1]);
            assertSame("getTypes", fType3, types[0]);
        }
    }

    public void testGetType() {
        fInstance.clear();
        fInstance.add(fContext1, fType0);
        fInstance.add(fContext1, fType1);
        fInstance.add(fContext2, fType2);
        fInstance.add(fContext2, fType3);

        ITmfEventType type = fInstance.getType(fContext1, fType0.getName());
        assertSame("getType", fType0, type);
        type = fInstance.getType(fContext1, fType1.getName());
        assertSame("getType", fType1, type);
        type = fInstance.getType(fContext1, fType2.getName());
        assertNull("getType", type);
        type = fInstance.getType(fContext1, fType3.getName());
        assertNull("getType", type);

        type = fInstance.getType(fContext2, fType2.getName());
        assertSame("getType", fType2, type);
        type = fInstance.getType(fContext2, fType3.getName());
        assertSame("getType", fType3, type);
        type = fInstance.getType(fContext2, fType0.getName());
        assertNull("getType", type);
        type = fInstance.getType(fContext2, fType1.getName());
        assertNull("getType", type);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    public void testClear() {
        fInstance.clear();
        assertEquals("clear", 0, fInstance.getContexts().length);
        assertEquals("clear", 0, fInstance.getTypes(null).length);
        assertNull("clear", fInstance.getType(null, null));
        assertEquals("clear", "TmfEventTypeManager [fEventTypes={}]", fInstance.toString());
    }

    public void testClearContext() {
        fInstance.clear();
        fInstance.add(fContext1, fType0);
        fInstance.add(fContext1, fType1);
        fInstance.add(fContext2, fType2);
        fInstance.add(fContext2, fType3);

        fInstance.clear(fContext1);

        final String[] contexts = fInstance.getContexts();
        assertEquals("clear context", 1, contexts.length);
        assertEquals("clear context", fContext2, contexts[0]);

        ITmfEventType[] types = fInstance.getTypes(fContext1);
        assertEquals("clear context", 0, types.length);

        ITmfEventType type = fInstance.getType(fContext1, fType0.getName());
        assertNull("clear context", type);
        type = fInstance.getType(fContext1, fType1.getName());
        assertNull("clear context", type);

        types = fInstance.getTypes(fContext2);
        assertEquals("clear context", 2, types.length);
        if (fType2 == types[0])
            assertSame("clear context", fType3, types[1]);
        else {
            assertSame("clear context", fType2, types[1]);
            assertSame("clear context", fType3, types[0]);
        }
    }

    public void testBasicAdd() {
        fInstance.clear();
        fInstance.add(fContext1, fType0);

        final String[] contexts = fInstance.getContexts();
        assertEquals("add", 1, contexts.length);
        assertEquals("add", fContext1, contexts[0]);

        final ITmfEventType[] types = fInstance.getTypes(contexts[0]);
        assertEquals("add", 1, types.length);
        assertSame("add", fType0, types[0]);

        ITmfEventType type = fInstance.getType(contexts[0], fType0.getName());
        assertSame("add", fType0, type);

        type = fInstance.getType(contexts[0], fType1.getName());
        assertNotSame("add", fType0, type);
    }

    public void testAdd() {
        fInstance.clear();
        fInstance.add(fContext1, fType0);
        fInstance.add(fContext1, fType1);
        fInstance.add(fContext2, fType2);
        fInstance.add(fContext2, fType3);

        final String[] contexts = fInstance.getContexts();
        Arrays.sort(contexts);
        assertEquals("add", 2, contexts.length);
        assertEquals("add", fContext1, contexts[0]);
        assertEquals("add", fContext2, contexts[1]);

        ITmfEventType[] types = fInstance.getTypes(fContext1);
        assertEquals("add", 2, types.length);
        if (fType0 == types[0])
            assertSame("add", fType1, types[1]);
        else {
            assertSame("add", fType0, types[1]);
            assertSame("add", fType1, types[0]);
        }

        types = fInstance.getTypes(fContext2);
        assertEquals("add", 2, types.length);
        if (fType2 == types[0])
            assertSame("add", fType3, types[1]);
        else {
            assertSame("add", fType2, types[1]);
            assertSame("add", fType3, types[0]);
        }

        ITmfEventType type = fInstance.getType(fContext1, fType0.getName());
        assertSame("add", fType0, type);
        type = fInstance.getType(fContext1, fType1.getName());
        assertSame("add", fType1, type);
        type = fInstance.getType(fContext2, fType2.getName());
        assertSame("add", fType2, type);
        type = fInstance.getType(fContext2, fType3.getName());
        assertSame("add", fType3, type);

        type = fInstance.getType(fContext1, fType2.getName());
        assertNull("add", type);
        type = fInstance.getType(fContext2, fType0.getName());
        assertNull("add", type);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    public void testToString() {
        fInstance.clear();
        assertEquals("toString", "TmfEventTypeManager [fEventTypes={}]", fInstance.toString());

        fInstance.add(fContext1, fType0);
        assertEquals("toString", "TmfEventTypeManager [fEventTypes={" + fContext1 + "={" + fTypeId1 + "=" + fType0 + "}}]", fInstance.toString());
    }

}
