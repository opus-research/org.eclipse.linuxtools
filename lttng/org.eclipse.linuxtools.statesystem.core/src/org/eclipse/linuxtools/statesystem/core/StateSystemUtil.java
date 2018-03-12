/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.statesystem.core;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.statesystem.core.interval.ITmfStateInterval;

/**
 * Provide utility methods for the state system
 *
 * @author - Geneviève Bastien
 * @since 3.2
 */
public final class StateSystemUtil {

    private StateSystemUtil() {

    }

    /**
     * Functional interface containing a method to determine if an interval
     * passes a given condition
     */
    public static interface IStateSystemIntervalCondition {

        /**
         * Return whether or not an interval passes a condition
         *
         * @param interval
         *            The interval on which to check the condition
         * @return <code>true</code> if the condition passes, <code>false</code>
         *         otherwise
         */
        public boolean pass(ITmfStateInterval interval);

    }

    /**
     * Queries intervals in the state system for an attribute until a certain
     * condition passes
     *
     * @param ss
     *            The state system on which to query intervals
     * @param attributeQuark
     *            The attribute quark to query
     * @param t1
     *            Start time of the query
     * @param t2
     *            End time of the query
     * @param condition
     *            The condition to meet for the query to finish
     * @return The first interval from t1 for which the condition is true, or
     *         <code>null</code> if no interval found
     */
    public static @Nullable ITmfStateInterval queryIntervalsUntil(ITmfStateSystem ss, int attributeQuark,
            long t1, long t2, IStateSystemIntervalCondition condition) {

        long current = t1;
        /* Make sure the range is ok */
        if (t1 < ss.getStartTime()) {
            current = ss.getStartTime();
        }
        long end = t2;
        if (end < ss.getCurrentEndTime()) {
            end = ss.getCurrentEndTime();
        }
        /* Make sure the time range makes sense */
        if (end < current) {
            return null;
        }

        try {
            while (current < t2) {
                ITmfStateInterval currentInterval = ss.querySingleState(current, attributeQuark);

                if (condition.pass(currentInterval)) {
                    return currentInterval;
                }
                current = currentInterval.getEndTime() + 1;
            }
        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
            /* Nothing to do */
        }
        return null;
    }

}
