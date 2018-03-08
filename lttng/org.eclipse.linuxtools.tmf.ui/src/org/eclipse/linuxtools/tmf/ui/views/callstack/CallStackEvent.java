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

package org.eclipse.linuxtools.tmf.ui.views.callstack;

import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Time Event implementation specific to the Call Stack View
 *
 * @author Patrick Tasse
 * @since 2.0
 */
public class CallStackEvent extends TimeEvent {

    private int fValue = -1;

    /**
     * Standard constructor
     *
     * @param entry
     *            The entry that this event affects
     * @param time
     *            The start time of the event
     * @param duration
     *            The duration of the event
     * @param value
     *            The event value (1-256)
     */
    public CallStackEvent(CallStackEntry entry, long time, long duration, int value) {
        super(entry, time, duration);
        fValue = value;
    }

    /**
     * Retrieve the value associated with this event
     *
     * @return The integer value
     */
    @Override
    public int getValue() {
        return fValue;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " start=" + getTime() + " end=" + (getTime() + getDuration()) + " duration=" + getDuration() + " value=" + fValue; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
