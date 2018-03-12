package org.eclipse.linuxtools.tmf.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.statesystem.core.statevalue.TmfStateValue;

/**
 * @author Naser Ezzati
 * @since 3.1
 *
 */
public class TmfStateValuesComparison{

    /**
     * The supported comparison operations to compare the different state values.
     * @since 4.0
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
     * Compare two state values with respect to the comparisonOperator
     *
     * @param source the current state value to be compared with the second object
     * @param dest the state value object to be compared with the current state value
     * @param comparisonOperator the operator to compare the state values
     * @return The comparison result
     * @since 4.0
     */

    public static boolean compare(@NonNull TmfStateValue source, @NonNull TmfStateValue dest, ComparisonOperator comparisonOperator) {

            if (source.getType() != dest.getType()) {
                return false;
            }

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
