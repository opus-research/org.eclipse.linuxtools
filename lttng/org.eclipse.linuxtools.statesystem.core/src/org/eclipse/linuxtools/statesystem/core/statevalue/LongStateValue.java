/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

 * Contributors:
 *   François Rajotte - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.statesystem.core.statevalue;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A state value containing a long integer (8 bytes).
 *
 * @version 1.0
 * @author François Rajotte
 */
final class LongStateValue extends TmfStateValue {

    private final long value;

    public LongStateValue(long valueAsLong) {
        this.value = valueAsLong;
    }

    @Override
    public Type getType() {
        return Type.LONG;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (!(object instanceof LongStateValue)) {
            return false;
        }
        LongStateValue other = (LongStateValue) object;
        return (this.value == other.value);
    }

    @Override
    public int hashCode() {
        return ((int) value) ^ ((int) (value >>> 32));
    }

    @Override
    public @Nullable
    String toString() {
        return String.format("%3d", value); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Unboxing methods
    // ------------------------------------------------------------------------

    @Override
    public long unboxLong() {
        return value;
    }

    @Override
    public int compareTo(@Nullable ITmfStateValue object) {
        if (object == null) {
            /*
             * We assume that every long state value is greater than any null
             * state value.
             */
            return 1;
        }
        int result = 0;

        switch (object.getType()) {
        case INTEGER:
            long otherLongValue = ((IntegerStateValue) object).unboxInt();
            result = Long.compare(this.value, otherLongValue);
            break;
        case DOUBLE:
            double otherDoubleValue = ((DoubleStateValue) object).unboxDouble();
            result = Double.compare(this.value, otherDoubleValue);
            break;
        case LONG:
            otherLongValue = ((LongStateValue) object).unboxLong();
            result = Long.compare(this.value, otherLongValue);
            break;
        case NULL:
            /*
             * We assume that every integer state value is greater than any null
             * state value.
             */
            result = 1;
            break;
        case STRING:
            /*
             * We assume that every state value is smaller than any string state
             * value.
             */
            result = -1;
            break;
        default:
            break;
        }
        return result;
    }

}
