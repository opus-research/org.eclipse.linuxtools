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

package org.eclipse.linuxtools.tmf.core.statevalue;

import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;

/**
 * A state value containing a long integer (8 bytes).
 *
 * @version 1.0
 * @author François Rajotte
 */
final class LongStateValue extends TmfStateValue {

    private final long valueLong;

    public LongStateValue(long valueAsLong) {
        this.valueLong = valueAsLong;
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
    public Long getValue() {
        return valueLong;
    }

    @Override
    public String toString() {
        return String.format("%3d", valueLong); //$NON-NLS-1$
    }

    @Override
    public int compareTo(ITmfStateValue value) {
        long result;
        int returnValue = 0;
        if (value.getType() == Type.NULL) {
            returnValue = 1;
        } else {
            try {
                result = this.unboxLong() - value.unboxLong();
                if (result > 0) {
                    returnValue = 1;
                } else if (result == 0) {
                    returnValue = 0;
                } else {
                    returnValue = -1;
                }
            } catch (StateValueTypeException e) {
                return returnValue;
            }
        }
        return returnValue;
    }

    @Override
    public ITmfStateValue add(ITmfStateValue stateValue) {
        try {
            if (stateValue.getType() == Type.NULL) {
                return null;
            }
            return TmfStateValue.newValueLong(this.unboxLong() + stateValue.unboxLong());
        } catch (StateValueTypeException e) {
            return null;
        }
    }

    @Override
    public ITmfStateValue increment() {
        try {
            return TmfStateValue.newValueLong(this.unboxLong() + 1);
        } catch (StateValueTypeException e) {
        }
        return null;
    }
}
