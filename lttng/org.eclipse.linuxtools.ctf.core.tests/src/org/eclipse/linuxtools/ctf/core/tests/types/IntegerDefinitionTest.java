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
import org.junit.After;
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
    String name = "testInt"; //$NON-NLS-1$
    String clockName = "clock"; //$NON-NLS-1$
    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(IntegerDefinitionTest.class);
    }

    /**
     * Perform pre-test initialization. We know the structDef won't be null (or
     * else the tests will fail), so we can safely suppress the warning.
     */
    @Before
    public void setUp() {

//        StructDefinition structDef = null;
//        boolean found = false;
        IntegerDeclaration id = new IntegerDeclaration( 1, true, 1, ByteOrder.BIG_ENDIAN, Encoding.NONE, clockName, 8);
        fixture = id.createDefinition(null, name);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
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
        String fieldName = ""; //$NON-NLS-1$

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
      IntegerDeclaration be = new IntegerDeclaration( 16, false, 1, ByteOrder.BIG_ENDIAN, Encoding.NONE, clockName, 8);
      IntegerDefinition fixture_be = be.createDefinition(null, name);

      IntegerDeclaration le = new IntegerDeclaration( 16, false, 1, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, clockName, 8);
      IntegerDefinition fixture_le = le.createDefinition(null, name);

      ByteBuffer bb = java.nio.ByteBuffer.allocateDirect(2);
      bb.put((byte) 0xab);
      bb.put((byte) 0xcd);
      BitBuffer input = new BitBuffer(bb);

      fixture_be.read(input);
      assertEquals(0xabcd, fixture_be.getValue());

      bb.position(0);
      input.position(0);
      fixture_le.read(input);
      assertEquals(0xcdab, fixture_le.getValue());

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
