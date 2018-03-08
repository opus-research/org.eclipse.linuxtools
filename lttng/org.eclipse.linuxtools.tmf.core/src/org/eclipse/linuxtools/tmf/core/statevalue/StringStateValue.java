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

package org.eclipse.linuxtools.tmf.core.statevalue;

import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;

/**
 * A state value containing a variable-sized string
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
final class StringStateValue extends TmfStateValue {

    private final String valueStr;

    public StringStateValue(String valueAsString) {
        assert (valueAsString != null);
        this.valueStr = valueAsString;
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public String getValue() {
        return valueStr;
    }

    @Override
    public String toString() {
        return valueStr;
    }

    @Override
    public int compareTo(ITmfStateValue value) {
        if (value.getType() == Type.NULL) {
            return 0;
        }
        try {
            return this.unboxStr().compareTo(value.unboxStr());
        } catch (StateValueTypeException e) {
            return 0;
        }
    }

    @Override
    public ITmfStateValue add(ITmfStateValue val) throws StateValueTypeException {
        throw new StateValueTypeException();
    }

    @Override
    public ITmfStateValue increment() throws StateValueTypeException {
        throw new StateValueTypeException();
    }
}
