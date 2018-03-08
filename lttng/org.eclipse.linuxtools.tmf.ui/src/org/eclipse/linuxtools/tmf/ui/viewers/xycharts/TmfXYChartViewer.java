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

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalThrottler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.viewers.TmfViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;

/**
 * Base class for a XY-Chart based on SWT chart. It provides a methods to define
 * zoom, selection and tool tip providers. It also provides call backs to be
 * notified by any changes caused by selection and zoom.
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
public abstract class TmfXYChartViewer extends TmfViewer implements ITmfChartTimeProvider {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /** Constant indicating to not use any of the default providers */
    public final static int NONE = 0;
    /** Constant to use for default mouse wheel zoom provider */
    public final static int DEFAULT_WHEEL_ZOOM = 1 << 1;
    /** Constant to use for default range zoom provider */
    public final static int DEFAULT_RANGE_ZOOM = 1 << 2;
    /** Constant to use for default mouse selection provider */
    public final static int DEFAULT_MOUSE_SELECTION = 1 << 3;
    /** Constant to use for default tool tip provider */
    public final static int DEFAULT_TOOLTIP = 1 << 4;

    /** Constant to use for all default providers */
    public final static int DEFAULT_PROVIDERS =
            TmfXYChartViewer.DEFAULT_WHEEL_ZOOM |
                    TmfXYChartViewer.DEFAULT_RANGE_ZOOM |
                    TmfXYChartViewer.DEFAULT_MOUSE_SELECTION |
                    TmfXYChartViewer.DEFAULT_TOOLTIP;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The offset to apply to any x position. This offset ensures better
     * precision when converting long to double and back.
     */
    protected long fXOffset;
    /** Start time of trace */
    protected long fStartTime;
    /** End time of trace */
    protected long fEndTime;
    /** Start time of current time range */
    protected long fWindowStartTime;
    /** End time of current time range */
    protected long fWindowEndTime;
    /** Duration of current time range */
    protected long fWindowDuration;
    /** Current selected time */
    protected long fSelectedTime;

    /**
     * The offset to apply to any y position. This offset ensures better
     * precision when converting long to double and back.
     */
    protected long fYOffset;

    private TmfBaseProvider fMouseWheelZoomProvider;
    private TmfBaseProvider fMouseDragZoomProvider;
    private TmfBaseProvider fMouseSelectionProvider;
    private TmfBaseProvider fToolTipProvider;

    /**
     * The trace that is displayed by this viewer
     */
    protected ITmfTrace fTrace;

    /** The SWT Chart reference */
    protected Chart fSwtChart;

    /** A signal throttler for range updates */
    private final TmfSignalThrottler fTimeRangeSyncThrottle = new TmfSignalThrottler(this, 200);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a TmfXYChartViewer.
     *
     * @param parent
     *            The parent composite
     * @param title
     *            The title of the viewer
     * @param xLabel
     *            The label of the xAxis
     * @param yLabel
     *            The label of the yAXIS
     * @param flags
     *            Bit mask to choose default providers
     */
    public TmfXYChartViewer(Composite parent, String title, String xLabel, String yLabel, int flags) {
        super(parent, title);
        fSwtChart = new Chart(parent, SWT.NONE);

        IAxis xAxis = fSwtChart.getAxisSet().getXAxis(0);
        IAxis yAxis = fSwtChart.getAxisSet().getYAxis(0);

        /* Set the title/labels, or hide them if they are not provided */
        if (title == null) {
            fSwtChart.getTitle().setVisible(false);
        } else {
            fSwtChart.getTitle().setText(title);
        }
        if (xLabel == null) {
            xAxis.getTitle().setVisible(false);
        } else {
            xAxis.getTitle().setText(xLabel);
        }
        if (yLabel == null) {
            yAxis.getTitle().setVisible(false);
        } else {
            yAxis.getTitle().setText(yLabel);
        }

        if ((flags & DEFAULT_WHEEL_ZOOM) == DEFAULT_WHEEL_ZOOM) {
            fMouseWheelZoomProvider = new TmfMouseWheelZoomProvider(this);
        }
        if ((flags & DEFAULT_RANGE_ZOOM) == DEFAULT_RANGE_ZOOM) {
            fMouseDragZoomProvider = new TmfMouseDragZoomProvider(this);
        }
        if ((flags & DEFAULT_MOUSE_SELECTION) == DEFAULT_MOUSE_SELECTION) {
            fMouseSelectionProvider = new TmfMouseSelectionProvider(this);
        }
        if ((flags & DEFAULT_TOOLTIP) == DEFAULT_TOOLTIP) {
            fToolTipProvider = new TmfSimpleTooltipProvider(this);
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public Control getControl() {
        return fSwtChart;
    }

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    @Override
    public long getEndTime() {
        return fEndTime;
    }

    @Override
    public long getWindowStartTime() {
        return fWindowStartTime;
    }

    @Override
    public long getWindowEndTime() {
        return fWindowEndTime;
    }

    @Override
    public long getWindowDuration() {
        return fWindowDuration;
    }

    @Override
    public long getSelectedTime() {
        return fSelectedTime;
    }

    /**
     * Returns the offset in x direction used for adjusting values in x
     * direction to avoid loss of precision when converting long <-> double
     *
     * @return the offset in x-direction
     */
    public long getXOffset() {
        return fXOffset;
    }

    /**
     * Returns the offset in y direction used for adjusting values in y
     * direction to avoid loss of precision when converting long <-> double
     *
     * @return the offset in y-direction
     */
    public long getYOffset() {
        return fYOffset;
    }

    /**
     * Sets a the mouse wheel zoom provider. An existing provider will be
     * disposed.
     *
     * @param provider
     *            The mouse wheel zoom provider to set
     */
    public void setMouseWheelZoomProvider(TmfBaseProvider provider) {
        if (fMouseWheelZoomProvider != null) {
            fMouseWheelZoomProvider.dispose();
        }
        fMouseWheelZoomProvider = provider;
    }

    /**
     * Sets a the mouse drag zoom provider. An existing provider will be
     * disposed.
     *
     * @param provider
     *            The mouse drag zoom provider to set
     */
    public void setMouseDragZoomProvider(TmfBaseProvider provider) {
        if (fMouseDragZoomProvider != null) {
            fMouseDragZoomProvider.dispose();
        }
        fMouseDragZoomProvider = provider;
    }

    /**
     * Sets a the mouse selection provider. An existing provider will be
     * disposed.
     *
     * @param provider
     *            The selection provider to set
     */
    public void setSelectionProvider(TmfBaseProvider provider) {
        if (fMouseSelectionProvider != null) {
            fMouseSelectionProvider.dispose();
        }
        fMouseSelectionProvider = provider;
    }

    /**
     * Sets a the tooltip provider. An existing provider will be disposed.
     *
     * @param provider
     *            The tooltip provider to set
     */
    public void setTooltipProvider(TmfBaseProvider provider) {
        if (fToolTipProvider != null) {
            fToolTipProvider.dispose();
        }
        fToolTipProvider = provider;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * A Method to initialize the viewer with a trace reference.
     *
     * @param trace
     *            A trace to apply in the viewer
     */
    public void initialize(ITmfTrace trace) {
        fTrace = trace;

        long timestamp = TmfTraceManager.getInstance().getSelectionBeginTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long startTime = TmfTraceManager.getInstance().getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        fSelectedTime = timestamp;
        fStartTime = fTrace.getStartTime().getValue();
        fWindowStartTime = fStartTime;
        fWindowDuration = fTrace.getInitialRangeOffset().getValue();
        fEndTime = startTime + getWindowDuration();
        fWindowEndTime = fEndTime;
        clearView();
    }

    @Override
    public void refresh() {
        fSwtChart.redraw();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fMouseWheelZoomProvider != null) {
            fMouseWheelZoomProvider.dispose();
        }

        if (fMouseSelectionProvider != null) {
            fMouseSelectionProvider.dispose();
        }

        if (fMouseDragZoomProvider != null) {
            fMouseDragZoomProvider.dispose();
        }

        if (fToolTipProvider != null) {
            fToolTipProvider.dispose();
        }

        fSwtChart.dispose();
    }

    /**
     * Clears the view content.
     */
    public void clearView() {
        if (!fSwtChart.isDisposed()) {
            ISeriesSet set = fSwtChart.getSeriesSet();
            ISeries[] series = set.getSeries();
            for (int i = 0; i < series.length; i++) {
                set.deleteSeries(series[i].getId());
            }
            fSwtChart.redraw();
        }
    }

    /**
     * Updates the time range.
     *
     * @param startTime
     *            A start time
     * @param endTime
     *            A end time.
     */
    public void updateWindow(long startTime, long endTime) {

        fWindowStartTime = startTime + fXOffset;
        fWindowEndTime = endTime + fXOffset;
        fWindowDuration = endTime - startTime;

        // Build the new time range; keep the current time
        TmfTimeRange timeRange = new TmfTimeRange(
                new TmfTimestamp(fWindowStartTime, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(fWindowEndTime, ITmfTimestamp.NANOSECOND_SCALE));

        // Send the signal
        TmfRangeSynchSignal signal = new TmfRangeSynchSignal(this, timeRange);
        fTimeRangeSyncThrottle.queue(signal);
    }

    /**
     * Method to notify about a change of the current selected time.
     *
     * @param currentTime
     *            The current selected time
     */
    public void updateCurrentTime(final long currentTime) {
        if (fTrace != null) {
            fSelectedTime = currentTime + fXOffset;
            // Use a request to get the nearest exact time stamp
            TmfTimeRange timeRange = new TmfTimeRange(new TmfTimestamp(fSelectedTime, ITmfTimestamp.NANOSECOND_SCALE), TmfTimestamp.BIG_CRUNCH);
            TmfEventRequest request = new TmfEventRequest(ITmfEvent.class, timeRange, 0, 1, ExecutionType.FOREGROUND) {
                @Override
                public void handleData(ITmfEvent event) {
                    if (event != null) {
                        TmfTimeSynchSignal signal = new TmfTimeSynchSignal(TmfXYChartViewer.this, event.getTimestamp());
                        TmfSignalManager.dispatchSignal(signal);
                    }
                }
            };
            fTrace.sendRequest(request);
        }
    }

    /**
     * Method to implement to create the chart content.
     */
    abstract protected void updateContent();

    // ------------------------------------------------------------------------
    // Signal Handler
    // ------------------------------------------------------------------------
    /**
     * Signal handler for handling of the time synch signal.
     *
     * @param signal
     *            The time synch signal {@link TmfTimeSynchSignal}
     */
    @TmfSignalHandler
    public void currentTimeUpdated(TmfTimeSynchSignal signal) {
        if ((signal.getSource() != this) && (fTrace != null)) {
            ITmfTimestamp selectedTime = signal.getBeginTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE);
            fSelectedTime = selectedTime.getValue();
            if (fMouseSelectionProvider instanceof TmfMouseSelectionProvider) {
                ((TmfMouseSelectionProvider) fMouseSelectionProvider).setSelectedTime(selectedTime.getValue() - fXOffset);
            }
        }
    }

    /**
     * Signal handler for handling of the time range synch signal.
     *
     * @param signal
     *            The time range synch signal {@link TmfRangeSynchSignal}
     */
    @TmfSignalHandler
    public void timeRangeUpdated(TmfRangeSynchSignal signal) {

        if (fTrace != null) {
            // Validate the time range
            TmfTimeRange range = signal.getCurrentRange().getIntersection(fTrace.getTimeRange());
            if (range == null) {
                return;
            }

            if (signal.getSource() != this) {
                // Update the time range
                long windowStartTime = range.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long windowEndTime = range.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long windowSpan = windowEndTime - windowStartTime;

                fWindowStartTime = windowStartTime;
                fWindowEndTime = windowEndTime;
                fWindowDuration = windowSpan;
            }
        }
        updateContent();
    }

    /**
     * Signal handler for handling of the trace closed signal.
     *
     * @param signal
     *            The trace closed signal {@link TmfTraceClosedSignal}
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {

        if (signal.getTrace() != fTrace) {
            return;
        }

        // Initialize the internal data
        fTrace = null;
        fStartTime = 0;
        fEndTime = 0;
        fWindowStartTime = 0;
        fWindowEndTime = 0;
        fWindowDuration = 0;
        fSelectedTime = 0;

        clearView();
    }

    /**
     * Signal handler for handling of the trace range updated signal.
     *
     * @param signal
     *            The trace range signal {@link TmfTraceRangeUpdatedSignal}
     */
    @TmfSignalHandler
    public void traceRangeUpdated(TmfTraceRangeUpdatedSignal signal) {

        if (signal.getTrace() != fTrace) {
            return;
        }

        TmfTimeRange fullRange = signal.getRange();

        long traceStartTime = fullRange.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long traceEndTime = fullRange.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        fStartTime = traceStartTime;
        fEndTime = traceEndTime;
    }

    /**
     * Signal handler for handling of the trace updated signal.
     *
     * @param signal
     *            The trace updated signal {@link TmfTraceUpdatedSignal}
     */
    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
        if (signal.getTrace() != fTrace) {
            return;
        }
        TmfTimeRange fullRange = signal.getTrace().getTimeRange();
        long traceStartTime = fullRange.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long traceEndTime = fullRange.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        fStartTime = traceStartTime;
        fEndTime = traceEndTime;
        fWindowStartTime = getStartTime();
        fWindowEndTime = getStartTime() + getWindowDuration();
    }

    /**
     * Signal handler for handling of the trace updated signal.
     *
     * @param signal
     *            The trace updated signal
     *            {@link TmfTimestampFormatUpdateSignal}
     */
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        fSwtChart.getAxisSet().adjustRange();
        fSwtChart.redraw();
    }

    // ------------------------------------------------------------------------
    // Helper Methods
    // ------------------------------------------------------------------------
    /**
     * Returns the current or default display.
     *
     * @return the current or default display
     */
    protected static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }
}
