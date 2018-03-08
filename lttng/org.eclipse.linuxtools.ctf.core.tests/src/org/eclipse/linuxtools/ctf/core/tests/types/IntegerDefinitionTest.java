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

package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>IntegerDefinitionTest</code> contains tests for the class
 * <code>{@link IntegerDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class IntegerDefinitionTest {

    private IntegerDefinition fixture;
    String name = "testInt";
    String clockName = "clock";

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        IntegerDeclaration id = new IntegerDeclaration( 1, true, 1, ByteOrder.BIG_ENDIAN, Encoding.NONE, clockName, 8);
        fixture = id.createDefinition(null, name);
    }

    /**
     * Run the IntegerDefinition(IntegerDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testIntegerDefinition() {
        IntegerDeclaration declaration = new IntegerDeclaration(1, true, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, null, 8);
        IDefinitionScope definitionScope = null;
        String fieldName = "";

        IntegerDefinition result = new IntegerDefinition(declaration,
                definitionScope, fieldName);
        assertNotNull(result);
    }

    /**
     * Run the IntegerDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        fixture.setValue(1L);

        IntegerDeclaration result = fixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the long getValue() method test.
     */
    @Test
    public void testGetValue() {
        fixture.setValue(1L);

        long result = fixture.getValue();
        assertEquals(1L, result);
    }

    /**
     * Run the void read(BitBuffer) method test.
     */
    @Test
    public void testRead() {
        fixture.setValue(1L);
        BitBuffer input = new BitBuffer(java.nio.ByteBuffer.allocateDirect(128));

        fixture.read(input);
    }

    /**
     * Test the read endianness in a big endian bit buffer
     */
    @Test
    public void testReadEndianness() {

      ByteBuffer bb = java.nio.ByteBuffer.allocateDirect(8);
      bb.put((byte) 0xab);
      bb.put((byte) 0xcd);
      bb.put((byte) 0xef);
      bb.put((byte) 0x12);
      bb.put((byte) 0x34);
      bb.put((byte) 0x56);
      bb.put((byte) 0x78);
      bb.put((byte) 0x9a);
      BitBuffer input = new BitBuffer(bb);

      /* Read 32-bits BE */
      IntegerDeclaration be = new IntegerDeclaration( 32, true, 1, ByteOrder.BIG_ENDIAN, Encoding.NONE, clockName, 8);
      IntegerDefinition fixture_be = be.createDefinition(null, name);
      fixture_be.read(input);
      assertEquals(0xabcdef12, fixture_be.getValue());

      /* Read 64-bits BE */
      be = new IntegerDeclaration( 64, true, 1, ByteOrder.BIG_ENDIAN, Encoding.NONE, clockName, 8);
      fixture_be = be.createDefinition(null, name);
      bb.position(0);
      input.position(0);
      fixture_be.read(input);
      assertEquals(0xabcdef123456789aL, fixture_be.getValue());

      /* Read 32-bits LE */
      IntegerDeclaration le = new IntegerDeclaration( 32, true, 1, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, clockName, 8);
      IntegerDefinition fixture_le = le.createDefinition(null, name);
      bb.position(0);
      input.position(0);
      fixture_le.read(input);
      assertEquals(0x12efcdab, fixture_le.getValue());

      /* Read 64-bits LE */
      le = new IntegerDeclaration( 64, true, 1, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, clockName, 8);
      fixture_le = le.createDefinition(null, name);
      bb.position(0);
      input.position(0);
      fixture_le.read(input);
      assertEquals(0x9a78563412efcdabL, fixture_le.getValue());

    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        fixture.setValue(1L);

        String result = fixture.toString();
        assertNotNull(result);
    }
}
