/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *     Matthew Khouzam - additional tests
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.io;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Before;
import org.junit.Test;

/**
 * Part of the BitBuffer tests which test the methods to read/write integers.
 * These are separated from the main file because the fixture is different.
 *
 * @author Alexandre Montplaisir
 */
public class BitBufferIntTest {

    private BitBuffer fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new BitBuffer(java.nio.ByteBuffer.allocateDirect(128));
        fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
        createBuffer(fixture);
    }

    private static void createBuffer(final BitBuffer fixture) {
        createBuffer(fixture, 16);
    }

    private static void createBuffer(final BitBuffer fixture, final int j) {
        final byte[] bytes = new byte[j];
        for (int i = 0; i < j; i++) {
            bytes[i] = (byte) (i % 0xff);
        }
        fixture.setByteBuffer(ByteBuffer.wrap(bytes));
        fixture.position(1);
    }

    /**
     * Run the int getInt() method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetInt_base() throws CTFReaderException {
        final int result = fixture.getInt();
        assertEquals(0x020406, result);
    }

    /**
     * Run the int getInt(int) method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetInt_pos0() throws CTFReaderException {
        fixture.position(0);
        final int result = fixture.getInt();
        assertEquals(0x010203, result);
    }

    /**
     * Run the int getInt(int,boolean) method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetInt_pos1() throws CTFReaderException {
        fixture.position(1);
        final int length = 1;
        final boolean signed = true;

        final long result = fixture.get(length, signed);
        assertEquals(0, result);
    }

    /**
     * Run the int getInt(int,boolean) method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetInt_pos2() throws CTFReaderException {
        fixture.position(2);
        final int length = 0;
        final boolean signed = true;

        final long result = fixture.get(length, signed);
        assertEquals(0, result);
    }

    /**
     * Run the get method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetInt_signed() throws CTFReaderException {
        fixture.position(1);
        final int length = 0;
        final boolean signed = true;

        final long result = fixture.get(length, signed);
        assertEquals(0, result);
    }

    /**
     * Run the get method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetInt_signed_length1() throws CTFReaderException {
        fixture.position(1);
        final int length = 1;
        final boolean signed = true;

        final long result = fixture.get(length, signed);
        assertEquals(0, result);
    }

    /**
     * Run the get method test with a little-endian
     * BitBuffer.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetInt_le1() throws CTFReaderException {
        final BitBuffer le_fixture = new BitBuffer(
                java.nio.ByteBuffer.allocateDirect(128));
        le_fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        createBuffer(le_fixture);
        le_fixture.position(1);
        final int length = 24;
        final long result = le_fixture.get(length, false);

        /* 0x020100 downshifted */
        assertEquals(0x810080, result);
    }

    /**
     * Run the get method test with a little-endian
     * BitBuffer.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetInt_le2() throws CTFReaderException {
        final BitBuffer le_fixture = new BitBuffer(
                java.nio.ByteBuffer.allocateDirect(128));
        le_fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        createBuffer(le_fixture);
        le_fixture.position(0);
        final int length = 24;
        final long result = le_fixture.get(length, false);
        assertEquals(0x020100, result);
    }

    /**
     * Run the int getInt(int,boolean) method test and expect an overflow.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test(expected = CTFReaderException.class)
    public void testGetInt_invalid() throws CTFReaderException {
        final BitBuffer small_fixture = new BitBuffer(
                java.nio.ByteBuffer.allocateDirect(128));
        small_fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
        createBuffer(small_fixture, 2);
        small_fixture.position(10);
        final int length = 32;
        final boolean signed = true;
        small_fixture.get(length, signed);

    }

    /**
     * Run the get method test and expect an overflow.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test(expected = CTFReaderException.class)
    public void testGetInt_invalid2() throws CTFReaderException {
        final BitBuffer small_fixture = new BitBuffer(
                java.nio.ByteBuffer.allocateDirect(128));
        small_fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
        createBuffer(small_fixture, 2);
        small_fixture.position(1);
        final int length = 64;
        final boolean signed = true;

        small_fixture.get(length, signed);
    }

    /**
     * Run the getLong method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong_pos0() throws CTFReaderException {
        fixture.position(0);
        final long result = fixture.getLong();
        assertEquals(0x01020304050607L, result);
    }

    /**
     * Run the getLong method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong_pos7() throws CTFReaderException {
        fixture.position(7);
        final long result = fixture.getLong();
        assertEquals(0x81018202830384L, result);
    }

    /**
     * Run the getLong method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong_pos8() throws CTFReaderException {
        fixture.position(8);
        final long result = fixture.getLong();
        assertEquals(result, 0x0102030405060708L);
    }

    /**
     * Run the getLong method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong_pos0LE() throws CTFReaderException {
        fixture.position(0);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        final long result = fixture.getLong();
        assertEquals(result, 0x0706050403020100L);
    }

    /**
     * Run the getLong method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong_pos7LE() throws CTFReaderException {
        fixture.position(7);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        final long result = fixture.getLong();
        assertEquals(result, 0x100e0c0a08060402L);
    }

    /**
     * Run the getLong method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong_pos8LE() throws CTFReaderException {
        fixture.position(8);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        final long result = fixture.getLong();
        assertEquals(result, 0x0807060504030201L);
    }

    /**
     * Run the get method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGet35_pos0BE() throws CTFReaderException {
        fixture.position(0);
        final long result = fixture.get(35, false);
        assertEquals(result, 0x081018L);
    }

    /**
     * Run the get method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGet35_pos8BE() throws CTFReaderException {
        fixture.position(8);
        final long result = fixture.get(35, false);
        assertEquals(result, 0x08101820L);
    }

    /**
     * Run the get method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGet35_pos0LE() throws CTFReaderException {
        fixture.position(0);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        final long result = fixture.get(35, false);

        assertEquals(result, 0x0403020100L);
    }

    /**
     * Run the get method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong35_pos7LE() throws CTFReaderException {
        fixture.position(7);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        final long result = fixture.get(35, false);
        assertEquals(result, 0x0208060402L);
    }

    /**
     * Run the get method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong35_pos8LE() throws CTFReaderException {
        fixture.position(8);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        final long result = fixture.get(35, false);
        assertEquals(result, 0x0504030201L);
    }

    /**
     * Run the get method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong35s_pos0LE() throws CTFReaderException {
        fixture.position(0);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        final long result = fixture.get(35, true);
        assertEquals(result, 0xfffffffc03020100L);
    }

    /**
     * Run the get method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong35s_pos7LE() throws CTFReaderException {
        fixture.position(7);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        final long result = fixture.get(35, true);
        assertEquals(result, 0x0208060402L);
    }

    /**
     * Run the get method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGetLong35s_pos8LE() throws CTFReaderException {
        fixture.position(8);
        fixture.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        final long result = fixture.get(35, true);
        assertEquals(result, 0xfffffffd04030201L);
    }

    /**
     * Run the void putInt(int) method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testPutInt() throws CTFReaderException {
        final int value = 1;
        fixture.position(1);
        fixture.putInt(value);
    }

    /**
     * Run the void putInt(int,int,boolean) method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testPutInt_signed() throws CTFReaderException {
        final int length = 1;
        final int value = 1;

        fixture.position(1);
        fixture.putInt(length, value);
    }

    /**
     * Run the void putInt(int,int,int,boolean) method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testPutInt_length0() throws CTFReaderException {
        final int length = 0;
        final int value = 1;

        fixture.position(1);
        fixture.putInt(length, value);
    }

    /**
     * Run the void putInt(int,int,int,boolean) method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testPutInt_length1() throws CTFReaderException {
        final int length = 1;
        final int value = 1;

        fixture.position(1);
        fixture.putInt(length, value);
    }

    /**
     * Run the void putInt(int) method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testPutInt_hex() throws CTFReaderException {
        final int value = 0x010203;
        int read;

        for (int i = 0; i <= 32; i++) {
            fixture.position(i);
            fixture.putInt(value);

            fixture.position(i);
            read = fixture.getInt();

            assertEquals(value, read);
        }
    }

    /**
     * Run the void putInt(int,int,int,boolean) method test.
     *
     * @throws CTFReaderException
     *             desired
     */
    @Test(expected = CTFReaderException.class)
    public void testPutInt_invalid() throws CTFReaderException {
        BitBuffer fixture2;
        fixture2 = new BitBuffer(java.nio.ByteBuffer.allocateDirect(128));
        fixture2.setByteOrder(ByteOrder.BIG_ENDIAN);
        createBuffer(fixture2, 4);
        fixture2.position(1);

        final int length = 32;
        final int value = 1;

        fixture2.putInt(length, value);
        fixture2.get(1, true);

    }
}
