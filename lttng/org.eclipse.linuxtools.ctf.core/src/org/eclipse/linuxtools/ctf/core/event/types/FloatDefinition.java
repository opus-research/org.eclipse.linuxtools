/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * A CTF float definition.
 *
 * The definition of a floating point basic data type. It will take the data
 * from a trace and store it (and make it fit) as a double.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class FloatDefinition extends Definition {
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final FloatDeclaration declaration;
    private double value;

    // ------------------------------------------------------------------------
    // Constructors
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
    public FloatDefinition(FloatDeclaration declaration,
            IDefinitionScope definitionScope, String fieldName) {
        super(definitionScope, fieldName);
        this.declaration = declaration;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * The value of a float stored, fit into a double. This should be extended
     * for exotic floats if this is necessary.
     *
     * @return the value of the float field fit into a double.
     */
    public double getValue() {
        return value;
    }

    /**
     * Sets the value of the float
     *
     * @param val
     *            the value of the float
     */
    public void setValue(double val) {
        value = val;
    }

    @Override
    public FloatDeclaration getDeclaration() {
        return declaration;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void read(BitBuffer input) throws CTFReaderException {
        /* Offset the buffer position wrt the current alignment */
        alignRead(input, this.declaration);
        final int exp = declaration.getExponent();
        final int mant = declaration.getMantissa();

        if ((exp + mant) == 32) {
            value = readRawFloat32(input, mant, exp);
        } else if ((exp + mant) == 64) {
            value = readRawFloat64(input, mant, exp);
        } else {
            value = Double.NaN;
        }
    }

    private static double readRawFloat64(BitBuffer input, final int manBits,
            final int expBits) throws CTFReaderException {
        long temp = input.get(64, false);
        return createFloat(temp, manBits - 1, expBits);
    }

    /**
     * @param rawValue
     * @param manBits
     * @param expBits
     */
    private static double createFloat(long rawValue, final int manBits,
            final int expBits) {
        long manShift = 1L << (manBits);
        long manMask = manShift - 1;
        long expMask = (1L << expBits) - 1;

        int exp = (int) ((rawValue >> (manBits)) & expMask) + 1;
        long man = (rawValue & manMask);
        final int offsetExponent = exp - (1 << (expBits - 1));
        double expPow = Math.pow(2.0, offsetExponent);
        double ret = man * 1.0f;
        ret /= manShift;
        ret += 1.0;
        ret *= expPow;
        return ret;
    }

    private static double readRawFloat32(BitBuffer input, final int manBits,
            final int expBits) throws CTFReaderException {
        long temp = input.get(32, false);
        return createFloat(temp, manBits - 1, expBits);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
