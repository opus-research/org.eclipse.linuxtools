/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;

/**
 * A CTF integer definition.
 *
 * The definition of a integer basic data type. It will take the data from a
 * trace and store it (and make it fit) as a long.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class IntegerDefinition extends SimpleDatatypeDefinition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final IntegerDeclaration declaration;
    private long value;

    /*
     * Mask is used in the readFastInt member, in profiling, read() is called a
     * bajillion times while reading a CTF file. The mask is long since it
     * converts the smaller ints (32 bits and less) into a long. The masks
     * before were created during run-time, but there are 32 possible values so
     * why not cache them? They are also longs since they need to be inverted to
     * create negative numbers.
     * A reminder: negative numbers have all 1s for their msbs.
     *
     * they need to be long so we can invert them
     * PS: this looks like a diving flag
     */
    private static final long mask[] = {
            0x0000000000000000L, 0x0000000000000001L, 0x0000000000000003L, 0x0000000000000007L, // 0 - 3
            0x000000000000000FL, 0x000000000000001FL, 0x000000000000003FL, 0x000000000000007FL, // 4 - 7
            0x00000000000000FFL, 0x00000000000001FFL, 0x00000000000003FFL, 0x00000000000007FFL, // 8 - 11
            0x0000000000000FFFL, 0x0000000000001FFFL, 0x0000000000003FFFL, 0x0000000000007FFFL, // 12 - 15
            0x000000000000FFFFL, 0x000000000001FFFFL, 0x000000000003FFFFL, 0x000000000007FFFFL, // 16 - 19
            0x00000000000FFFFFL, 0x00000000001FFFFFL, 0x00000000003FFFFFL, 0x00000000007FFFFFL, // 20 - 23
            0x0000000000FFFFFFL, 0x0000000001FFFFFFL, 0x0000000003FFFFFFL, 0x0000000007FFFFFFL, // 24 - 27
            0x000000000FFFFFFFL, 0x000000001FFFFFFFL, 0x000000003FFFFFFFL, 0x000000007FFFFFFFL, // 28 - 31
            0x00000000FFFFFFFFL, 0x00000001FFFFFFFFL, 0x00000003FFFFFFFFL, 0x00000007FFFFFFFFL, // 32 - 35
            0x0000000FFFFFFFFFL, 0x0000001FFFFFFFFFL, 0x0000003FFFFFFFFFL, 0x0000007FFFFFFFFFL, // 36 - 39
            0x000000FFFFFFFFFFL, 0x000001FFFFFFFFFFL, 0x000003FFFFFFFFFFL, 0x000007FFFFFFFFFFL, // 40 - 43
            0x00000FFFFFFFFFFFL, 0x00001FFFFFFFFFFFL, 0x00003FFFFFFFFFFFL, 0x00007FFFFFFFFFFFL, // 44 - 47
            0x0000FFFFFFFFFFFFL, 0x0001FFFFFFFFFFFFL, 0x0003FFFFFFFFFFFFL, 0x0007FFFFFFFFFFFFL, // 48 - 51
            0x000FFFFFFFFFFFFFL, 0x001FFFFFFFFFFFFFL, 0x003FFFFFFFFFFFFFL, 0x007FFFFFFFFFFFFFL, // 52 - 55
            0x00FFFFFFFFFFFFFFL, 0x01FFFFFFFFFFFFFFL, 0x03FFFFFFFFFFFFFFL, 0x07FFFFFFFFFFFFFFL, // 56 - 59
            0x0FFFFFFFFFFFFFFFL, 0x1FFFFFFFFFFFFFFFL, 0x3FFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL, // 60 - 63
            0xFFFFFFFFFFFFFFFFL // 64
    };

    /*
     * The bit to check if a number is negative wrt it's length.
     * See the explanation above.
     */
    private static final long longNegBit[] = {
            0L        ,  (1L << 0),  (1L << 1),  (1L << 2), // 0 - 3
            (1L << 3) ,  (1L << 4),  (1L << 5),  (1L << 6), // 4 - 7
            (1L << 7) ,  (1L << 8),  (1L << 9), (1L << 10), // 8 - 11
            (1L << 11), (1L << 12), (1L << 13), (1L << 14), // 12 - 15
            (1L << 15), (1L << 16), (1L << 17), (1L << 18), // 16 - 19
            (1L << 19), (1L << 20), (1L << 21), (1L << 22), // 20 - 23
            (1L << 23), (1L << 24), (1L << 25), (1L << 26), // 24 - 27
            (1L << 27), (1L << 28), (1L << 29), (1L << 30), // 28 - 31
            (1L << 31), (1L << 32), (1L << 33), (1L << 34), // 32 - 35
            (1L << 35), (1L << 36), (1L << 37), (1L << 38), // 36 - 39
            (1L << 39), (1L << 40), (1L << 41), (1L << 42), // 40 - 43
            (1L << 43), (1L << 44), (1L << 45), (1L << 46), // 44 - 47
            (1L << 47), (1L << 48), (1L << 49), (1L << 50), // 48 - 51
            (1L << 51), (1L << 52), (1L << 53), (1L << 54), // 52 - 55
            (1L << 55), (1L << 56), (1L << 57), (1L << 58), // 56 - 59
            (1L << 59), (1L << 60), (1L << 61), (1L << 62), // 60 - 63
            (1L << 63) // 64
    };

    // ------------------------------------------------------------------------
    // Contructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param declaration
     *            the parent declaration
     * @param definitionScope
     *            the parent scope
     * @param fieldName
     *            the field name
     */
    public IntegerDefinition(IntegerDeclaration declaration,
            IDefinitionScope definitionScope, String fieldName) {
        super(definitionScope, fieldName);
        this.declaration = declaration;
    }

    // ------------------------------------------------------------------------
    // Gettters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the value of the integer
     *
     * @return the value of the integer (in long)
     */
    public long getValue() {
        return value;
    }

    /**
     * Sets the value of an integer
     *
     * @param val
     *            the value
     */
    public void setValue(long val) {
        value = val;
    }

    @Override
    public IntegerDeclaration getDeclaration() {
        return declaration;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Long getIntegerValue() {
        return getValue();
    }

    @Override
    public String getStringValue() {
        return this.toString();
    }

    @Override
    public void read(BitBuffer input) {
        /* Offset the buffer position wrt the current alignment */
        alignRead(input, this.declaration);

        boolean signed = declaration.isSigned();
        int length = declaration.getLength();
        long bits = 0;

        /*
         * Is the endianness of this field the same as the endianness of the
         * input buffer? If not, then temporarily set the buffer's endianness to
         * this field's just to read the data
         */
        ByteOrder byteOrder = input.getByteOrder();
        if ((this.declaration.getByteOrder() != null) &&
                (this.declaration.getByteOrder() != input.getByteOrder())) {
            input.setByteOrder(this.declaration.getByteOrder());
        }

        // TODO: use the eventual getLong from BitBuffer
        if (length == 64) {
            bits = readLong(input);
        } else if (length <= 32) {
            bits = readFastInt(input, signed, length);
        } else { /* 33-63 bits */
            int pos = input.position() + length;
            bits = readLong(input);
            long bitMask = mask[length];
            long negBit = longNegBit[length];
            bits &= bitMask;
            if ((bits & negBit) != 0 && signed) {
                bits |= ~bitMask;
            }
            input.position(pos);
        }
        /*
         * Put the input buffer's endianness back to original if it was changed
         */
        if (byteOrder != input.getByteOrder()) {
            input.setByteOrder(byteOrder);
        }

        value = bits;
    }

    /*
     * This is a faster read for smaller ints. It is particularly fast for 32 -
     * 16- 8 bit ints.
     */
    private static long readFastInt(BitBuffer input, boolean signed, int length) {
        int pos = input.position() + length;
        long bits;
        bits = input.getInt(length, signed);
        bits = bits & mask[length];
        /*
         * The previous line loses sign information but is necessary, this fixes
         * the sign for 32 bit numbers. Sorry, in java all 64 bit ints are
         * signed.
         */
        if ((longNegBit[length] == (bits & longNegBit[length])) && signed) {
            bits |= ~mask[length];
        }
        input.position(pos);
        return bits;
    }

    /*
     * This is to read a long int.
     */
    private long readLong(BitBuffer input) {
        long bits;
        long low = input.getInt(32, false);
        low = low & 0x00000000FFFFFFFFL;
        long high = input.getInt(32, false);
        high = high & 0x00000000FFFFFFFFL;
        if (this.declaration.getByteOrder() != ByteOrder.BIG_ENDIAN) {
            bits = (high << 32) | low;
        } else {
            bits = (low << 32) | high;
        }
        return bits;
    }

    @Override
    public String toString() {
        if (declaration.isCharacter()) {
            char c = (char) value;
            return Character.toString(c);
        }
        return String.valueOf(value);
    }
}
