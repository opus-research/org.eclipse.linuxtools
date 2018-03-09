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
import java.util.List;

import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;

/**
 * The StateIntervalList represents a list of intervals with a start time and a
 * end time. Each attribute can be query between start time and end time. This
 * object is returned by a 2D query to the state system.
 *
 * @version 1.0
 * @author Florian Wininger
 */
public final class TmfStateIntervalList implements ITmfStateIntervalList {

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
    private ArrayList<ITmfStateInterval> list;

    /**
     * Construct an interval list.
     */
    public TmfStateIntervalList() {
        this.start = Long.MAX_VALUE;
        this.end = Long.MIN_VALUE;
        this.list = new ArrayList<ITmfStateInterval>();
    }

    @Override
    public long getStartTime() {
        return start;
    }

    @Override
    public long getEndTime() {
        return end;
    }

    @Override
    public void setStartTime(long t) {
        start = t;
    }

    @Override
    public void setEndTime(long t) {
        end = t;
    }

    @Override
    public void add(ITmfStateInterval interval) {
        list.add(interval);
        start = Math.min(start, interval.getStartTime());
        end = Math.max(end, interval.getEndTime());
    }

    @Override
    public void addAll(List<ITmfStateInterval> l) {
        for (ITmfStateInterval interval : l) {
            add(interval);
        }
    }

    @Override
    public ITmfStateInterval get(long time, int attribute) throws TimeRangeException {
        // check the time range
        if (time <= start || time >= end) {
            throw new TimeRangeException();
        }
        for (ITmfStateInterval interval : list) {
            if (interval.getAttribute() == attribute && interval.intersects(time)) {
                return interval;
            }
        }
        return null;
    }

    @Override
    public List<ITmfStateInterval> getList() {
        return list;
    }

    @Override
    public void clear() {
        this.start = Long.MAX_VALUE;
        this.end = Long.MIN_VALUE;
        this.list.clear();
    }

}
