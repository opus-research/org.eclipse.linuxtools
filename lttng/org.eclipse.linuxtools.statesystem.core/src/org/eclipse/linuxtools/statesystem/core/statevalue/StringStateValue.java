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

/**
 * A state value containing a variable-sized string
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
final class StringStateValue extends TmfStateValue {

    private final String value;

    public StringStateValue(String valueAsString) {
        this.value = valueAsString;
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
    public boolean equals(@Nullable Object object) {
        if (!(object instanceof StringStateValue)) {
            return false;
        }
        StringStateValue other = (StringStateValue) object;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    // ------------------------------------------------------------------------
    // Unboxing methods
    // ------------------------------------------------------------------------

    @Override
    public String unboxStr() {
        return value;
    }

    @Override
    public boolean compare(Object object, ComparisonOperator comparisonOperator) {
        if (!(object instanceof StringStateValue)) {
            return false;
        }
        StringStateValue other = (StringStateValue) object;

        boolean result = false;
        switch (comparisonOperator) {
        case EQ:
            result = (value.compareTo(other.value) == 0);
            break;
        case GE:
            result = (value.compareTo(other.value) >= 0);
            break;
        case GT:
            result = (value.compareTo(other.value) > 0);
            break;
        case LE:
            result = (value.compareTo(other.value) <= 0);
            break;
        case LT:
            result = (value.compareTo(other.value) < 0);
            break;
        case NE:
            result = (value.compareTo(other.value) != 0);
            break;
        case None:
            throw new IllegalArgumentException();
        default:
            result = (value.compareTo(other.value) == 0);
            break;
        }
        return result;
    }
}
