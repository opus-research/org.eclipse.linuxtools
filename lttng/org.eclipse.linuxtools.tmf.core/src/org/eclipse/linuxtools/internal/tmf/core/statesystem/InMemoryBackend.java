/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.interval.TmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;

/**
 * State history back-end that stores its intervals in RAM only. It cannot be
 * saved to disk, which means we need to rebuild it every time we re-open a
 * trace. But it's relatively quick to build, so this shouldn't be a problem in
 * most cases.
 *
 * This should only be used with very small state histories (and/or, very small
 * traces). Since it's stored in standard Collections, it's limited to 2^31
 * intervals.
 *
 * @author Alexandre Montplaisir
 */
public class InMemoryBackend implements IStateHistoryBackend {

    private final List<ITmfStateInterval> intervals;
    private final long startTime;
    private long latestTime;

    /**
     * Constructor
     *
     * @param startTime
     *            The start time of this interval store
     */
    public InMemoryBackend(long startTime) {
        this.startTime = startTime;
        this.latestTime = startTime;
        this.intervals = new ArrayList<ITmfStateInterval>();
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getEndTime() {
        return latestTime;
    }

    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, ITmfStateValue value) throws TimeRangeException {
        /* Make sure the passed start/end times make sense */
        if (stateStartTime > stateEndTime || stateStartTime < startTime) {
            throw new TimeRangeException();
        }

        ITmfStateInterval interval = new TmfStateInterval(stateStartTime, stateEndTime, quark, value);

        /* Update the "latest seen time" */
        if (stateEndTime > latestTime) {
            latestTime = stateEndTime;
        }

        /* Add the interval into the-array */
        intervals.add(interval);
    }


    @Override
    public void doQuery(List<ITmfStateInterval> currentStateInfo, long t)
            throws TimeRangeException {
        if (!checkValidTime(t)) {
            throw new TimeRangeException();
        }

        for (ITmfStateInterval entry : intervals) {
            if (entry.intersects(t)) {
                currentStateInfo.set(entry.getAttribute(), entry);
            }
        }
    }

    @Override
    public ITmfStateInterval doSingularQuery(long t, int attributeQuark)
            throws TimeRangeException, AttributeNotFoundException {
        if (!checkValidTime(t)) {
            throw new TimeRangeException();
        }

        for (ITmfStateInterval entry : intervals) {
            if (entry.intersects(t) && entry.getAttribute() == attributeQuark) {
                return entry;
            }
        }
        throw new AttributeNotFoundException();
    }

    @Override
    public boolean checkValidTime(long t) {
        if (t >= startTime && t <= latestTime) {
            return true;
        }
        return false;
    }

    @Override
    public void finishedBuilding(long endTime) throws TimeRangeException {
        /* Nothing to do */
    }

    @Override
    public FileInputStream supplyAttributeTreeReader() {
        /* Saving to disk not supported */
        return null;
    }

    @Override
    public File supplyAttributeTreeWriterFile() {
        /* Saving to disk not supported */
        return null;
    }

    @Override
    public long supplyAttributeTreeWriterFilePosition() {
        /* Saving to disk not supported */
        return -1;
    }

    @Override
    public void removeFiles() {
        /* Nothing to do */
    }

    @Override
    public void dispose() {
        /* Nothing to do */
    }

    @Override
    public void debugPrint(PrintWriter writer) {
        writer.println(intervals.toString());
    }

}
