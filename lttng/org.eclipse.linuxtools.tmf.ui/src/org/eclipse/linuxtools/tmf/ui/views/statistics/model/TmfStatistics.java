/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Intial API and Implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics.model;

/**
 * Primitive container for Statistics data
 *
 * Contains information about statistics that can be retrieved with any type of
 * traces
 *
 * There are two counters : one for the global number of events in the trace and
 * another for the number of events in the selected time range
 *
 * @version 1.0
 * @author Mathieu Denis
 */
public class TmfStatistics {
    /**
     * Number of event.
     */
    public long nbEvents = 0;
    /**
     * Number of event within a time range.
     */
    public long nbEventsInTimeRange = 0;
}
