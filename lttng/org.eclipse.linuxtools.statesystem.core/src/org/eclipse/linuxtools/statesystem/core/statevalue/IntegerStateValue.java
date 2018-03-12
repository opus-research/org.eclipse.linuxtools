/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.statesystem.core.statevalue;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateValueTypeException;

/**
 * A state value containing a simple integer.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
final class IntegerStateValue extends TmfStateValue {

    private final int value;

    public IntegerStateValue(int valueAsInt) {
        this.value = valueAsInt;
    }

    @Override
    public Type getType() {
        return Type.INTEGER;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (!(object instanceof IntegerStateValue)) {
            return false;
        }
        IntegerStateValue other = (IntegerStateValue) object;
        return (this.value == other.value);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public @Nullable String toString() {
        return String.format("%3d", value); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Unboxing methods
    // ------------------------------------------------------------------------

    @Override
    public int unboxInt() {
        return value;
    }

    @Override
    public long unboxLong() {
        /* It's always safe to up-cast an int into a long */
        return value;
    }

    @Override
    public int compareTo(@Nullable ITmfStateValue object) {
        if (object == null) {
            throw new StateValueTypeException("An Integer state value cannot be compared to null."); //$NON-NLS-1$
        }

        switch (object.getType()) {
        case INTEGER:
            IntegerStateValue other = (IntegerStateValue) object;
            return Integer.compare(this.value, other.value);
        case DOUBLE:
            double otherDoubleValue = ((DoubleStateValue) object).unboxDouble();
            return Double.compare(this.value, otherDoubleValue);
        case LONG:
            long otherLongValue = ((LongStateValue) object).unboxLong();
            return Long.compare(this.value, otherLongValue);
        case NULL:
            /*
             * We assume that every integer state value is greater than any null
             * state value.
             */
            return 1;
        case STRING:
            throw new StateValueTypeException("An Integer state value cannot be compared to a String state value."); //$NON-NLS-1$
        default:
            return 0;
        }

    }

}
