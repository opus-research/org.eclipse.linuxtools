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

import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;

/**
 * This is the basic interface for accessing a list of state intervals. See
 * TmfStateIntervalList.java for a basic implementation.
 *
 * A StateIntervalList is meant to be immutable. All implementing (non-abstract)
 * classes should ideally be marked as 'final'.
 *
 * @since 3.0
 * @author Florian Wininger
 */
public interface ITmfStateIntervalList {

    /**
     * Retrieve the start time of the intervals list.
     *
     * @return the start time of the intervals list.
     */
    long getStartTime();

    /**
     * Retrieve the end time of the intervals list.
     *
     * @return the end time of the intervals list.
     */
    long getEndTime();

    /**
     * Set a new start time for the intervals list.
     *
     * @param t
     *            the start time for the intervals list.
     */
    void setStartTime(long t);

    /**
     * Set a new end time for the intervals list.
     *
     * @param t
     *            the end time for the intervals list.
     */
    void setEndTime(long t);

    /**
     * Add the interval in the list.
     *
     * @param interval
     *            interval added in the intervals list.
     */
    void add(ITmfStateInterval interval);

    /**
     * Add all the intervals list in the list.
     *
     * @param list
     *            intervals list added in the intervals list.
     */
    void addAll(List<? extends ITmfStateInterval> list);

    /**
     * Retrieve the interval intersect the time and the attribute. The timestamp
     * must be between getStartTime and getEndTime.
     *
     * @param time
     *            A time that intersects the interval to retrieve.
     * @param attribute
     *            quark of the attribute
     * @return the state interval for time and attribute.
     *
     * @throws TimeRangeException
     *             if the timestamp was invalid.
     */
    ITmfStateInterval get(long time, int attribute) throws TimeRangeException;

    /**
     * Retrieve the complete interval list.
     *
     * @return the complete interval list.
     */
    List<ITmfStateInterval> getList();

    /**
     * Remove all intervals from this intervals list.
     */
    void clear();

}
