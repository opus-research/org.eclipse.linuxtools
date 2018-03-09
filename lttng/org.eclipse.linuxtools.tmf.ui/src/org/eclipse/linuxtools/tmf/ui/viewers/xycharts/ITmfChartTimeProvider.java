/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.viewers.xycharts;

/**
 * Interface defining methods a chart viewer has to implement for displaying
 * data for ranges of a trace.
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
public interface ITmfChartTimeProvider {
    /**
     * Gets the start time of trace
     *
     * @return start time of trace
     */
    long getStartTime();

    /**
     * Gets the end time of trace
     *
     * @return End time of trace
     */
    long getEndTime();

    /**
     * Gets the start time of current time range displayed
     *
     * @return start time of current time range
     */
    long getWindowStartTime();

    /**
     * Gets the end time of current time
     *
     * @return End time of current time
     */
    long getWindowEndTime();

    /**
     * Gets the duration of the current time range displayed
     *
     * @return duration of current time range
     */
    long getWindowDuration();

    /**
     * Get the current selected time
     *
     * @return Current selected time
     */
    long getSelectedTime();

}
