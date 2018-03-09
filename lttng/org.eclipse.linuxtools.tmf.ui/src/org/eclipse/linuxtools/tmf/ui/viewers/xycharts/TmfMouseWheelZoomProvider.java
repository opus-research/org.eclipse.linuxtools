/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *                   (inspired by HistogramZoom implementation)
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.xycharts;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;

/**
 * Class for providing zooming based on mouse wheel. It also notifies the viewer
 * about a change of range.
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
public class TmfMouseWheelZoomProvider extends TmfBaseProvider implements MouseWheelListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private final static double ZOOM_FACTOR = 0.8;

    /** Minimum window size */
    private long fMinWindowSize = 0;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor.
     *
     * @param tmfChartViewer
     *            The parent histogram object
     */
    public TmfMouseWheelZoomProvider(TmfXYChartViewer tmfChartViewer) {
        super(tmfChartViewer);
        register();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public synchronized void dispose() {
        if ((getChartViewer().getControl() != null) && !getChartViewer().getControl().isDisposed()) {
            deregister();
        }
    }

    @Override
    public void register() {
        getChart().getPlotArea().addMouseWheelListener(this);
    }

    @Override
    public void deregister() {
        getChart().getPlotArea().removeMouseWheelListener(this);
    }

    // ------------------------------------------------------------------------
    // MouseWheelListener
    // ------------------------------------------------------------------------

    @Override
    public synchronized void mouseScrolled(MouseEvent event) {

        long fRangeStartTime = getChartViewer().getWindowStartTime() - getChartViewer().getXOffset();
        long fRangeDuration = getChartViewer().getWindowDuration();

        // Compute the new time range
        long requestedRange = (event.count > 0) ? Math.round(ZOOM_FACTOR * fRangeDuration) : (long) Math.ceil(fRangeDuration * (1.0 / ZOOM_FACTOR));

        // Distribute delta and adjust for boundaries
        long requestedStart = validateStart(fRangeStartTime + (fRangeDuration - requestedRange) / 2);
        long requestedEnd = validateEnd(requestedStart, requestedStart + requestedRange);

        getChartViewer().updateWindow(requestedStart, requestedEnd);
    }

    private long validateStart(long start) {
        long realStart = start;
        long fAbsoluteStartTime = getChartViewer().getStartTime() - getChartViewer().getXOffset();
        long fAbsoluteEndTime = getChartViewer().getEndTime() - getChartViewer().getXOffset();

        if (realStart < fAbsoluteStartTime) {
            realStart = fAbsoluteStartTime;
        }
        if (realStart > fAbsoluteEndTime) {
            realStart = fAbsoluteEndTime - fMinWindowSize;
        }
        return realStart;
    }

    private long validateEnd(long start, long end) {
        long realEnd = end;

        long fAbsoluteEndTime = getChartViewer().getEndTime() - getChartViewer().getXOffset();

        if (realEnd > fAbsoluteEndTime) {
            realEnd = fAbsoluteEndTime;
        }
        if (realEnd < start + fMinWindowSize) {
            realEnd = start + fMinWindowSize;
        }
        return realEnd;
    }
}
