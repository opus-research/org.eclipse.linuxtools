/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for bar charts
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.barchart;

import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Time Event specific to the control flow view
 * @since 2.0
 */
public class BarChartEvent extends TimeEvent {

    private final int fValue;
    /**
     * Default value when no other value present
     */
    public static final int NOVALUE = -99;

    /**
     * Constructor
     *
     * @param entry
     *            The entry to which this time event is assigned
     * @param time
     *            The timestamp of this event
     * @param duration
     *            The duration of this event
     * @param value
     *            The status assigned to the event
     */
    public BarChartEvent(ITimeGraphEntry entry, long time, long duration,
            int value) {
        super(entry, time, duration);
        fValue = value;
    }

    /**
     * Constructor with no value
     *
     * @param entry
     *            The entry to which this time event is assigned
     * @param time
     *            The timestamp of this event
     * @param duration
     *            The duration of this event
     */
    public BarChartEvent(BarChartEntry entry, long time, long duration) {
        this(entry, time, duration, NOVALUE);
    }

    /**
     * Get this event's status
     *
     * @return The integer matching this status
     */
    public int getValue() {
        return fValue;
    }

    /**
     * Return whether an event has a value
     *
     * @return true if the event has a value
     */
    public boolean hasValue() {
        return (fValue != NOVALUE);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " start=" + fTime + " end=" + (fTime + fDuration) + " duration=" + fDuration + " value=" + fValue; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
