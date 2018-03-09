/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.interval;

import java.util.List;

/**
 * This is the basic interface for accessing a list of state intervals. See
 * TmfStateIntervalList.java for a basic implementation.
 *
 * A StateIntervalList is meant to be immutable. All implementing (non-abstract)
 * classes should ideally be marked as 'final'.
 *
 * @version 1.0
 * @author Florian Wininger
 */
public interface ITmfStateIntervalList {

    /**
     * Retrieve the start time of the interval intersection.
     *
     * @return the start time of the interval intersection.
     */
    long getStartTime();

    /**
     * Retrieve the end time of the interval intersection.
     *
     * @return the end time of the interval intersection.
     */
    long getEndTime();

    /**
     * add the interval in the list.
     *
     * @param interval
     *            interval added in the list.
     */
    void add(ITmfStateInterval interval);

    /**
     * addAll the intervals list in the list.
     *
     * @param list
     *            intervals list added in the list.
     */
    void addAll(List<ITmfStateInterval> list);

    /**
     * Retrieve the interval intersect the time and the attribute.
     *
     * @param time
     *            a time inside the interval intersection.
     * @param attribute
     *            quark of the attribute
     * @return the state interval for time and attribute.
     */
    ITmfStateInterval get(long time, int attribute);

    /**
     * Remove all intervals from this intervals list.
     */
    void clear();

}
