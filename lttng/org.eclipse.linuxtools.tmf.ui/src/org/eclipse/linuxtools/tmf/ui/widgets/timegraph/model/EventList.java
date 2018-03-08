/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model;

import java.util.Collections;
import java.util.List;

/**
 * Event list
 *
 * This class is intended to be used to store a zoomed list that can contain
 * non-contiguous events (e.g. empty time range between events). The start and
 * end time in this class represent the range of the query that was used to
 * collect the zoomed list events, and are used by the Event Iterator to
 * properly split the full range 'aggregated' events that intersect with the
 * zoom range.
 *
 * @since 2.1
 */
public class EventList {

    private final List<ITimeEvent> fEventList;
    private final long fStartTime;
    private final long fEndTime;

    /**
     * Constructs an empty list with an initial capacity of ten.
     *
     * @param list
     *            The event list
     * @param startTime
     *            The start time of the query used to collect the list
     * @param endTime
     *            The end time of the query used to collect the list
     */
    public EventList(List<ITimeEvent> list, long startTime, long endTime) {
        if (list != null) {
            fEventList = Collections.unmodifiableList(list);
        } else {
            fEventList = null;
        }
        fStartTime = startTime;
        fEndTime = endTime;
    }

    /**
     * Get the event list
     *
     * @return the event list
     */
    public List<ITimeEvent> getList() {
        return fEventList;
    }

    /**
     * Get the start time of the query used to collect the list
     *
     * @return the start time
     */
    public long getStartTime() {
        return fStartTime;
    }

    /**
     * Get the end time of the query used to collect the list
     *
     * @return the end time
     */
    public long getEndTime() {
        return fEndTime;
    }
}
