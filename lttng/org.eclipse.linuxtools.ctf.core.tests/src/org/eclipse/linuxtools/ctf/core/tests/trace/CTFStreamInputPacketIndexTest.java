/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ListIterator;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndex;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndexEntry;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputPacketIndexTest</code> contains tests for the
 * class <code>{@link StreamInputPacketIndex}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class CTFStreamInputPacketIndexTest {

    private StreamInputPacketIndex fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        fixture = new StreamInputPacketIndex();
        fixture.add(new StreamInputPacketIndexEntry(1L, 0L));
    }

    /**
     * Run the StreamInputPacketIndex() constructor test.
     */
    @Test
    public void testStreamInputPacketIndex() {
        assertNotNull(fixture);
    }

    /**
     * Run the ListIterator<StreamInputPacketIndexEntry> listIterator() method
     * test, with no parameter to listIterator().
     */
    @Test
    public void testListIterator_noparam() {
        ListIterator<StreamInputPacketIndexEntry> result = fixture.listIterator();

        assertNotNull(result);
        assertEquals(true, result.hasNext());
        assertEquals(-1, result.previousIndex());
        assertEquals(false, result.hasPrevious());
        assertEquals(0, result.nextIndex());
    }

    /**
     * Run the ListIterator<StreamInputPacketIndexEntry> listIterator(n) method
     * test, with n = 1.
     */
    @Test
    public void testListIterator_withparam() {
        ListIterator<StreamInputPacketIndexEntry> result = fixture.listIterator(1);

        assertNotNull(result);
        assertEquals(false, result.hasNext());
        assertEquals(0, result.previousIndex());
        assertEquals(true, result.hasPrevious());
        assertEquals(1, result.nextIndex());
        assertEquals(false, result.hasNext());
    }

    /**
     * Run the ListIterator<StreamInputPacketIndexEntry> search(long) method
     * test with a valid timestamp.
     */
    @Test
    public void testSearch_valid() {
        ListIterator<StreamInputPacketIndexEntry> result = fixture.search(1L);

        assertNotNull(result);
        assertEquals(true, result.hasNext());
        assertEquals(-1, result.previousIndex());
        assertEquals(false, result.hasPrevious());
        assertEquals(0, result.nextIndex());
    }

    /**
     * Run the ListIterator<StreamInputPacketIndexEntry> search(long) method
     * test with an invalid timestamp.
     */
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testSearch_invalid() {
        ListIterator<StreamInputPacketIndexEntry> result = fixture.search(-1L);

        assertNotNull(result);
    }
}