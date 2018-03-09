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
 * The definition of a integer basic data type. It will take the data
 * from a trace and store it (and make it fit) as a long.
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

    // ------------------------------------------------------------------------
    // Contructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param declaration the parent declaration
     * @param definitionScope the parent scope
     * @param fieldName the field name
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
     * @return the value of the integer (in long)
     */
    public long getValue() {
        return value;
    }

    /**
     * Sets the value of an integer
     * @param val the value
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
        final long longNegBit = 0x0000000080000000L;
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
            long low = input.getInt(32, false);
            low = low & 0x00000000FFFFFFFFL;
            long high = input.getInt(32, false);
            high = high & 0x00000000FFFFFFFFL;
            if (this.declaration.getByteOrder() != ByteOrder.BIG_ENDIAN) {
                bits = (high << 32) | low;
            } else {
                bits = (low << 32) | high;
            }
        } else {
            bits = input.getInt(length, signed);
            bits = bits & 0x00000000FFFFFFFFL;
            /*
             * The previous line loses sign information but is necessary, this
             * fixes the sign for 32 bit numbers. Sorry, in java all 64 bit ints
             * are signed.
             */
            if ((longNegBit == (bits & longNegBit)) && signed) {
                bits |= 0xffffffff00000000L;
            }
        }
        /*
         * Put the input buffer's endianness back to original if it was changed
         */
        if (byteOrder != input.getByteOrder()) {
            input.setByteOrder(byteOrder);
        }

        value = bits;
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
