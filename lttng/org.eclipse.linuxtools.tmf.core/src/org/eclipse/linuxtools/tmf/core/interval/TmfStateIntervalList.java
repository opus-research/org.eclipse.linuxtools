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

/**
 * The StateIntervalList represents a list of intervals. It is the main object
 * being returned from 2D queries to the state system.
 *
 * @version 1.0
 * @author Florian Wininger
 */
public final class TmfStateIntervalList implements ITmfStateIntervalList {

    /**
     * The maximum of the Start Time of interval in the list.
     */
    private long start;

    /**
     * The minimum of the End Time of interval in the list.
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
        this.start = Long.MIN_VALUE;
        this.end = Long.MAX_VALUE;
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
    public void add(ITmfStateInterval interval) {
        list.add(interval);
        start = Math.max(start, interval.getStartTime());
        end = Math.min(end, interval.getStartTime());
    }

    @Override
    public void addAll(List<ITmfStateInterval> l) {
        for (ITmfStateInterval interval : l) {
            add(interval);
        }
    }

    @Override
    public ITmfStateInterval get(long time, int attribute) {
        for (ITmfStateInterval interval : list) {
            if (interval.getAttribute() == attribute && interval.intersects(time)) {
                return interval;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        this.start = Long.MIN_VALUE;
        this.end = Long.MAX_VALUE;
        this.list.clear();
    }

}
