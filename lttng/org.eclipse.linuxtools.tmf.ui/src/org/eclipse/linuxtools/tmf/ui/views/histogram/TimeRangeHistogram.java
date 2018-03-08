/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Changed to updated histogram data model
 *   Francois Chouinard - Moved from LTTng to TMF
 *   Patrick Tasse - Update for mouse wheel zoom
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.swt.widgets.Composite;

/**
 * A basic histogram widget that displays the event distribution of a specific time range of a trace.
 *
 * @version 1.1
 * @author Francois Chouinard
 */
public class TimeRangeHistogram extends Histogram {

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param view The parent histogram view
     * @param parent The parent composite
     */
    public TimeRangeHistogram(HistogramView view, Composite parent) {
        super(view, parent);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void updateTimeRange(long startTime, long endTime) {
        ((HistogramView) fParentView).updateTimeRange(startTime, endTime);
    }

    /**
     * Sets the time range of the histogram
     * @param startTime The start time
     * @param duration The duration of the time range
     */
    public synchronized void setTimeRange(long startTime, long duration) {
        if (getDataModel().getNbEvents() == 0) {
            getDataModel().setTimeRange(startTime, startTime + duration);
        }
    }

}
