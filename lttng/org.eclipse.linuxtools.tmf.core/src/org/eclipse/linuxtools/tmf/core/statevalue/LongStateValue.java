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
        if (value.getType() == Type.NULL) {
            return 0;
        }
        try {
            return Long.valueOf(this.unboxLong()).compareTo(Long.valueOf(value.unboxLong()));
        } catch (StateValueTypeException e) {
            return 0;
        }
    }
}
