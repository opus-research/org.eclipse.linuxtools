/*******************************************************************************
 * Copyright (c) 2014 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Naser Ezzati - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue;

/**
 * This class provides a comparison mechanism to compare the different state
 * values.
 *
 * @author Naser Ezzati
 * @since 3.1
 */
public class TmfStateValuesComparison {

    /**
     * The supported comparison operations to compare the different state
     * values.
     *
     * @since 3.1
     */
    public enum ComparisonOperator {
        /** Equal (=) */
        EQ,
        /** Not Equal (!=) */
        NE,
        /** Greater Equal (>=) */
        GE,
        /** Greater Than (>) */
        GT,
        /** Less Equal (<=) */
        LE,
        /** Less Than (<) */
        LT,
        /** None! */
        None
    }

    /**
     * Compare two state values with respect to the given comparisonOperator
     *
     * @param source
     *            the current state value to be compared with the second object
     * @param dest
     *            the state value object to be compared with the current state
     *            value
     * @param comparisonOperator
     *            the operator to compare the state values
     * @return The comparison result
     * @since 3.1
     */
    public static boolean compare(@NonNull ITmfStateValue source, @NonNull ITmfStateValue dest, ComparisonOperator comparisonOperator) {
        boolean result = false;
        switch (comparisonOperator) {
        case EQ:
            result = (source.compareTo(dest) == 0);
            break;
        case NE:
            result = (source.compareTo(dest) != 0);
            break;
        case GE:
            result = (source.compareTo(dest) >= 0);
            break;
        case GT:
            result = (source.compareTo(dest) > 0);
            break;
        case LE:
            result = (source.compareTo(dest) <= 0);
            break;
        case LT:
            result = (source.compareTo(dest) < 0);
            break;
        case None:
            throw new IllegalArgumentException();
        default:
            result = (source.compareTo(dest) == 0);
            break;
        }
        return result;
    }

}
