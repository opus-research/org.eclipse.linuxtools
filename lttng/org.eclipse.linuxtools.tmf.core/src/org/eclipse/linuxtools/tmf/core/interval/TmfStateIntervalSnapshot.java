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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;

/**
 * The StateIntervalList represents a collection of intervals with a start time
 * and a end time. Each attribute can be query between start time and end time.
 * This object is returned by a 2D query to the state system.
 *
 * @since 3.0
 * @author Florian Wininger
 */
public final class TmfStateIntervalSnapshot {

    /**
     * The start timestamp of the intervals list.
     */
    private long start;

    /**
     * The end timestamp of the intervals list.
     */
    private long end;

    /**
     * The list to store the intervals.
     */
    private final ArrayList<ITmfStateInterval> fList;

    /**
     * Construct an interval list.
     */
    public TmfStateIntervalCollection() {
        start = Long.MAX_VALUE;
        end = Long.MIN_VALUE;
        fList = new ArrayList<ITmfStateInterval>();
    }

    /**
     * Retrieve the start time of the intervals list.
     *
     * @return the start time of the intervals list.
     */
    public long getStartTime() {
        return start;
    }

    /**
     * Retrieve the end time of the intervals list.
     *
     * @return the end time of the intervals list.
     */
    public long getEndTime() {
        return end;
    }

    /**
     * Set a new start time for the intervals list.
     *
     * @param t
     *            the start time for the intervals list.
     */
    public void setStartTime(long t) {
        start = t;
    }

    /**
     * Set a new end time for the intervals list.
     *
     * @param t
     *            the end time for the intervals list.
     */
    public void setEndTime(long t) {
        end = t;
    }

    /**
     * Add the interval in the list.
     *
     * @param interval
     *            interval added in the intervals list.
     */
    public void add(ITmfStateInterval interval) {
        fList.add(interval);
        start = Math.min(start, interval.getStartTime());
        end = Math.max(end, interval.getEndTime());
    }

    /**
     * Add all the intervals list in the list.
     *
     * @param list
     *            intervals list added in the intervals list.
     */
    public void addAll(List<? extends ITmfStateInterval> list) {
        for (ITmfStateInterval interval : list) {
            add(interval);
        }
    }

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
    public ITmfStateInterval get(long time, int attribute) throws TimeRangeException {
        // Validate the requested time to be within the time range of this list.
        if (time <= start || time >= end) {
            throw new TimeRangeException();
        }
        for (ITmfStateInterval interval : fList) {
            if (interval.getAttribute() == attribute && interval.intersects(time)) {
                return interval;
            }
        }
        return null;
    }

    /**
     * Retrieve the complete interval list.
     *
     * @return the complete interval list.
     */
    public List<ITmfStateInterval> getList() {
        return Collections.unmodifiableList(fList);
    }

    /**
     * Remove all intervals from this intervals list.
     */
    public void clear() {
        start = Long.MAX_VALUE;
        end = Long.MIN_VALUE;
        fList.clear();
    }

}
