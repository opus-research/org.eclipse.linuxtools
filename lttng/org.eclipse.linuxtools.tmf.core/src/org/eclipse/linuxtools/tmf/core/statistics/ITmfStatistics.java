/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statistics;

import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.signal.TmfStatsUpdatedSignal;

/**
 * Provider for statistics, which is assigned to a trace. This can be used to
 * populate views like the Statistics View or the Histogram.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public interface ITmfStatistics {

    /**
     * This method provides a centralized and asynchronous way of querying
     * statistics information. It is an alternative to the other get* methods,
     * and should not block the caller for too long.
     *
     * Implementors can usually call their own getEventTotal(),
     * getEventsInRange(), etc. but should do so in a separate thread, and
     * should send a {@link TmfStatsUpdatedSignal} whenever they are done (that
     * signal will carry the results).
     *
     * @param isGlobal
     *            Is this for a global query (whole time range of a trace), or
     *            just for a specific time range.
     * @param start
     *            The start time of the query range. Has no effect if isGlobal
     *            is true.
     * @param end
     *            The end time of the query range. Has no effect if isGlobal is
     *            true.
     */
    public void updateStats(final boolean isGlobal, ITmfTimestamp start,
            ITmfTimestamp end);

    /**
     * Return the total number of events in the trace.
     *
     * @return The total number of events
     */
    public long getEventsTotal();

    /**
     * Return a Map of the total events in the trace, per event type. The event
     * type should come from ITmfEvent.getType().getName().
     *
     * @return The map of <event_type, count>, for the whole trace
     */
    public Map<String, Long> getEventTypesTotal();

    /**
     * Retrieve the number of events in the trace in a given time interval.
     *
     * @param start
     *            Start time of the time range
     * @param end
     *            End time of the time range
     * @return The number of events found
     */
    public long getEventsInRange(ITmfTimestamp start, ITmfTimestamp end);

    /**
     * Retrieve the number of events in the trace, per event type, in a given
     * time interval.
     *
     * @param start
     *            Start time of the time range
     * @param end
     *            End time of the time range
     * @return The map of <event_type, count>, for the given time range
     */
    public Map<String, Long> getEventTypesInRange(ITmfTimestamp start,
            ITmfTimestamp end);

    /**
     * Notify the statistics back-end that the trace is being closed, so it
     * should dispose itself as appropriate (release file descriptors, etc.)
     */
    public void dispose();
}
