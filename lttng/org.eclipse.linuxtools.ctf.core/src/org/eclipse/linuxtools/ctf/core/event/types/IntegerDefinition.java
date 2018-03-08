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

import java.math.BigInteger;
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

    // they need to be long so we can invert them
    private final static long mask[] = {
            0x0000000000000000L, // 0
            0x0000000000000001L, // 1
            0x0000000000000003L, // 2
            0x0000000000000007L, // 3

            0x000000000000000FL, // 4
            0x000000000000001FL, // 5
            0x000000000000003FL, // 6
            0x000000000000007FL, // 7

            0x00000000000000FFL, // 8
            0x00000000000001FFL, // 9
            0x00000000000003FFL, // 10
            0x00000000000007FFL, // 11

            0x0000000000000FFFL, // 12
            0x0000000000001FFFL, // 13
            0x0000000000003FFFL, // 14
            0x0000000000007FFFL, // 15

            0x000000000000FFFFL, // 16
            0x000000000001FFFFL, // 17
            0x000000000003FFFFL, // 18
            0x000000000007FFFFL, // 19

            0x00000000000FFFFFL, // 20
            0x00000000001FFFFFL, // 21
            0x00000000003FFFFFL, // 22
            0x00000000007FFFFFL, // 23

            0x0000000000FFFFFFL, // 24
            0x0000000001FFFFFFL, // 25
            0x0000000003FFFFFFL, // 26
            0x0000000007FFFFFFL, // 27

            0x000000000FFFFFFFL, // 28
            0x000000000FFFFFFFL, // 29
            0x000000000FFFFFFFL, // 30
            0x000000000FFFFFFFL, // 31

            0x00000000FFFFFFFFL, // 32
    };

    private final static long longNegBit[] = {
            0L, // 0
            (1L << 0), // 1
            (1L << 1), // 2
            (1L << 2), // 3

            (1L << 3), // 4
            (1L << 4), // 5
            (1L << 5), // 6
            (1L << 6), // 7

            (1L << 7), // 8
            (1L << 8), // 9
            (1L << 9), // 10
            (1L << 10), // 11

            (1L << 11), // 12
            (1L << 12), // 13
            (1L << 13), // 14
            (1L << 14), // 15

            (1L << 15), // 16
            (1L << 16), // 17
            (1L << 17), // 18
            (1L << 18), // 19

            (1L << 19), // 20
            (1L << 20), // 21
            (1L << 21), // 22
            (1L << 22), // 23

            (1L << 23), // 24
            (1L << 24), // 25
            (1L << 25), // 26
            (1L << 26), // 27

            (1L << 27), // 28
            (1L << 28), // 29
            (1L << 29), // 30
            (1L << 30), // 31

            (1L << 31), // 32
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
        } else {
            int pos = input.position() + length;
            bits = readLong(input);
            long mask = (1L << length) - 1L;
            long negBit = (1L << length - 1);
            bits &= mask;
            if ((bits & negBit) != 0 && signed) {
                bits |= ~mask;
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

    private static long readFastInt(BitBuffer input, boolean signed, int length) {
        int pos = input.position() + length;
        long bits;
        bits = input.getInt(length, signed);
        bits = bits & mask[length];
        /*
         * The previous line loses sign information but is necessary, this
         * fixes the sign for 32 bit numbers. Sorry, in java all 64 bit ints
         * are signed.
         */
        if ((longNegBit[length] == (bits & longNegBit[length])) && signed) {
            bits |= ~mask[length];
        }
        input.position(pos);
        return bits;
    }

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
        return formatNumber(value, declaration.getBase(), declaration.isSigned());
    }

    /**
     * Print a numeric value as a string in a given base
     *
     * @param value
     *            The value to print as string
     * @param base
     *            The base for this value
     * @param signed
     *            Is the value signed or not
     * @return formatted number string
     * @since 3.0
     */
    public static final String formatNumber(long value, int base, boolean signed) {
        String s;
        /* Format the number correctly according to the integer's base */
        switch (base) {
        case 2:
            s = "0b" + Long.toBinaryString(value); //$NON-NLS-1$
            break;
        case 8:
            s = "0" + Long.toOctalString(value); //$NON-NLS-1$
            break;
        case 16:
            s = "0x" + Long.toHexString(value); //$NON-NLS-1$
            break;
        case 10:
        default:
            /* For non-standard base, we'll just print it as a decimal number */
            if (!signed && value < 0) {
                /*
                 * Since there are no 'unsigned long', handle this case with
                 * BigInteger
                 */
                BigInteger bigInteger = BigInteger.valueOf(value);
                /*
                 * we add 2^64 to the negative number to get the real unsigned
                 * value
                 */
                bigInteger = bigInteger.add(BigInteger.valueOf(1).shiftLeft(64));
                s = bigInteger.toString();
            } else {
                s = Long.toString(value);
            }
            break;
        }
        return s;
    }
}
