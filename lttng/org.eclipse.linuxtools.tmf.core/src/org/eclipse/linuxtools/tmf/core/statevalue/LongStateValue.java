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

    // ------------------------------------------------------------------------
    // Unboxing methods
    // ------------------------------------------------------------------------

    @Override
    public int unboxInt() throws StateValueTypeException {
        /* We'd rather never allow it, or else people might take bad habits... */
        throw new StateValueTypeException("Trying to unbox Long value as an Int"); //$NON-NLS-1$
    }

    @Override
    public long unboxLong() {
        return valueLong;
    }

    @Override
    public String unboxStr() throws StateValueTypeException {
        throw new StateValueTypeException("Trying to unbox Long value as a String"); //$NON-NLS-1$
    }
}
