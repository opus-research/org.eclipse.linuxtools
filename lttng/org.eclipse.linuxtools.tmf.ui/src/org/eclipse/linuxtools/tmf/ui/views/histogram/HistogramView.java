/*******************************************************************************
 * Copyright (c) 2009, 2010, 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   William Bourque - Initial API and implementation
 *   Yuriy Vashchuk - GUI reorganisation, simplification and some related code improvements.
 *   Yuriy Vashchuk - Histograms optimisation.
 *   Yuriy Vashchuk - Histogram Canvas Heritage correction
 *   Francois Chouinard - Cleanup and refactoring
 *   Francois Chouinard - Moved from LTTng to TMF
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.editors.ITmfTraceEditor;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

/**
 * The purpose of this view is to provide graphical time distribution statistics about the trace events.
 * <p>
 * The view is composed of two histograms and two controls:
 * <ul>
 * <li>an event distribution histogram for the whole trace;
 * <li>an event distribution histogram for current time window (window span);
 * <li>the timestamp of the currently selected event;
 * <li>the window span (size of the time window of the smaller histogram).
 * </ul>
 * The histograms x-axis show their respective time range.
 *
 * @version 2.0
 * @author Francois Chouinard
 */
public class HistogramView extends TmfView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     *  The view ID as defined in plugin.xml
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.histogram"; //$NON-NLS-1$

    /**
     *  The initial window span (in nanoseconds)
     */
    public static final long INITIAL_WINDOW_SPAN = (1L * 100 * 1000 * 1000); // .1sec

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Parent widget
    private Composite fParent;

    // The current trace
    private ITmfTrace fTrace;

    // Current timestamp/time window - everything in the TIME_SCALE
    private long fTraceStartTime;
    private long fTraceEndTime;
    private long fWindowStartTime;
    private long fWindowEndTime;
    private long fWindowSpan = INITIAL_WINDOW_SPAN;
    private long fCurrentTimestamp;

    // Time controls
    private HistogramTextControl fCurrentEventTimeControl;
    private HistogramTextControl fTimeSpanControl;

    // Histogram/request for the full trace range
    private static FullTraceHistogram fFullTraceHistogram;
    private HistogramRequest fFullTraceRequest;

    // Histogram/request for the selected time range
    private static TimeRangeHistogram fTimeRangeHistogram;
    private HistogramRequest fTimeRangeRequest;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public HistogramView() {
        super(ID);
    }

    @Override
    public void dispose() {
        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }
        fFullTraceHistogram.dispose();
        fTimeRangeHistogram.dispose();
        fCurrentEventTimeControl.dispose();
        fTimeSpanControl.dispose();
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // TmfView
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {

        fParent = parent;

        // Control labels
        final String currentEventLabel = Messages.HistogramView_currentEventLabel;
        final String windowSpanLabel = Messages.HistogramView_windowSpanLabel;

        // --------------------------------------------------------------------
        // Set the HistogramView layout
        // --------------------------------------------------------------------

        Composite viewComposite = new Composite(fParent, SWT.FILL);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.horizontalSpacing = 5;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        viewComposite.setLayout(gridLayout);

        // Use all available space
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        viewComposite.setLayoutData(gridData);

        // --------------------------------------------------------------------
        // Time controls
        // --------------------------------------------------------------------

        Composite controlsComposite = new Composite(viewComposite, SWT.FILL);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 5;
        gridLayout.verticalSpacing = 0;
        gridLayout.makeColumnsEqualWidth = false;
        gridLayout.marginLeft = 5;
        gridLayout.marginRight = 5;
        controlsComposite.setLayout(gridLayout);

        // Current event time control
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.CENTER;
        gridData.verticalAlignment = SWT.CENTER;
        fCurrentEventTimeControl = new HistogramCurrentTimeControl(this, controlsComposite, currentEventLabel, 0L);
        fCurrentEventTimeControl.setLayoutData(gridData);
        fCurrentEventTimeControl.setValue(0L);

        // Window span time control
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.CENTER;
        gridData.verticalAlignment = SWT.CENTER;
        fTimeSpanControl = new HistogramTimeRangeControl(this, controlsComposite, windowSpanLabel, 0L);
        fTimeSpanControl.setLayoutData(gridData);
        fTimeSpanControl.setValue(0L);

        // --------------------------------------------------------------------
        // Time range histogram
        // --------------------------------------------------------------------

        Composite timeRangeComposite = new Composite(viewComposite, SWT.FILL);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginTop = 5;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginLeft = 5;
        gridLayout.marginRight = 5;
        timeRangeComposite.setLayout(gridLayout);

        // Use remaining horizontal space
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        timeRangeComposite.setLayoutData(gridData);

        // Histogram
        fTimeRangeHistogram = new TimeRangeHistogram(this, timeRangeComposite);

        // --------------------------------------------------------------------
        // Full range histogram
        // --------------------------------------------------------------------

        Composite fullRangeComposite = new Composite(viewComposite, SWT.FILL);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginTop = 5;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginLeft = 5;
        gridLayout.marginRight = 5;
        fullRangeComposite.setLayout(gridLayout);

        // Use remaining horizontal space
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        fullRangeComposite.setLayoutData(gridData);

        // Histogram
        fFullTraceHistogram = new FullTraceHistogram(this, fullRangeComposite);

        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editor instanceof ITmfTraceEditor) {
            ITmfTrace trace = ((ITmfTraceEditor) editor).getTrace();
            if (trace != null) {
                traceSelected(new TmfTraceSelectedSignal(this, trace));
            }
        }
    }

    @Override
    public void setFocus() {
        fFullTraceHistogram.fCanvas.setFocus();
    }

    void refresh() {
        fParent.layout(true);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the current trace handled by the view
     *
     * @return the current trace
     * @since 2.0
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Returns the time range of the current selected window (base on default time scale).
     *
     * @return the time range of current selected window.
     */
    public TmfTimeRange getTimeRange() {
        return new TmfTimeRange(
                new TmfTimestamp(fWindowStartTime, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(fWindowEndTime, ITmfTimestamp.NANOSECOND_SCALE));
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Broadcast TmfSignal about new current time value.
     * @param newTime the new current time.
     */
    void updateCurrentEventTime(long newTime) {
        if (fTrace != null) {
            TmfTimeRange timeRange = new TmfTimeRange(new TmfTimestamp(newTime, ITmfTimestamp.NANOSECOND_SCALE), TmfTimestamp.BIG_CRUNCH);
            HistogramRequest request = new HistogramRequest(fTimeRangeHistogram.getDataModel(), timeRange, 0, 1, 0, ExecutionType.FOREGROUND) {
                @Override
                public void handleData(ITmfEvent event) {
                    if (event != null) {
                        TmfTimeSynchSignal signal = new TmfTimeSynchSignal(this, event.getTimestamp());
                        TmfSignalManager.dispatchSignal(signal);
                    }
                }
            };
            fTrace.sendRequest(request);
        }
    }

    /**
     * Broadcast TmfSignal about new selected time range.
     * @param startTime the new start time
     * @param endTime the new end time
     */
    void updateTimeRange(long startTime, long endTime) {
        if (fTrace != null) {
            // Build the new time range; keep the current time
            TmfTimeRange timeRange = new TmfTimeRange(
                    new TmfTimestamp(startTime, ITmfTimestamp.NANOSECOND_SCALE),
                    new TmfTimestamp(endTime, ITmfTimestamp.NANOSECOND_SCALE));
            ITmfTimestamp currentTime = new TmfTimestamp(fCurrentTimestamp, ITmfTimestamp.NANOSECOND_SCALE);
            fTimeSpanControl.setValue(endTime - startTime);

            // Send the FW signal
            TmfRangeSynchSignal signal = new TmfRangeSynchSignal(this, timeRange, currentTime);
            TmfSignalManager.dispatchSignal(signal);
        }
    }

    /**
     * Broadcast TmfSignal about new selected time range.
     * @param newDuration new duration (relative to current start time)
     */
    public synchronized void updateTimeRange(long newDuration) {
        if (fTrace != null) {
            long delta = newDuration - fWindowSpan;
            long newStartTime = fWindowStartTime + (delta / 2);
            setNewRange(newStartTime, newDuration);
        }
    }

    private void setNewRange(long startTime, long duration) {
        if (startTime < fTraceStartTime) {
            startTime = fTraceStartTime;
        }

        long endTime = startTime + duration;
        if (endTime > fTraceEndTime) {
            endTime = fTraceEndTime;
            if ((endTime - duration) > fTraceEndTime) {
                startTime = endTime - duration;
            } else {
                startTime = fTraceStartTime;
            }
        }
        updateTimeRange(startTime, endTime);
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handles trace opened signal. Loads histogram if new trace time range is not
     * equal <code>TmfTimeRange.NULL_RANGE</code>
     * @param signal the trace selected signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        assert (signal != null);
        fTrace = signal.getTrace();
        loadTrace();
    }

    /**
     * Handles trace selected signal. Loads histogram if new trace time range is not
     * equal <code>TmfTimeRange.NULL_RANGE</code>
     * @param signal the trace selected signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        assert (signal != null);
        if (fTrace != signal.getTrace()) {
            fTrace = signal.getTrace();
            loadTrace();
        }
    }

    private void loadTrace() {
        initializeHistograms();
        fParent.redraw();
    }

    /**
     * Handles trace closed signal. Clears the view and data model and cancels requests.
     * @param signal the trace closed signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {

        if (signal.getTrace() != fTrace) {
            return;
        }

        // Kill any running request
        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }

        // Initialize the internal data
        fTrace = null;
        fTraceStartTime = 0L;
        fTraceEndTime = 0L;
        fWindowStartTime = 0L;
        fWindowEndTime = 0L;
        fWindowSpan = INITIAL_WINDOW_SPAN;
        fCurrentTimestamp = 0L;

        // Clear the UI widgets
        fFullTraceHistogram.clear();
        fTimeRangeHistogram.clear();
        fCurrentEventTimeControl.setValue(0L);

        fTimeSpanControl.setValue(0);
    }

    /**
     * Handles trace range updated signal. Extends histogram according to the new time range. If a
     * HistogramRequest is already ongoing, it will be cancelled and a new request with the new range
     * will be issued.
     *
     * @param signal the trace range updated signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceRangeUpdated(TmfTraceRangeUpdatedSignal signal) {

        if (signal.getTrace() != fTrace) {
            return;
        }

        boolean drawTimeRangeHistogram = fTraceStartTime == 0;
        TmfTimeRange fullRange = signal.getRange();

        fTraceStartTime = fullRange.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        fTraceEndTime = fullRange.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        fFullTraceHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fTimeRangeHistogram.setFullRange(fTraceStartTime, fTraceEndTime);

        if (drawTimeRangeHistogram) {
            fCurrentTimestamp = fTraceStartTime;
            fCurrentEventTimeControl.setValue(fCurrentTimestamp);
            fFullTraceHistogram.setTimeRange(fTraceStartTime, INITIAL_WINDOW_SPAN);
            fTimeRangeHistogram.setTimeRange(fTraceStartTime, INITIAL_WINDOW_SPAN);
            sendTimeRangeRequest(fTraceStartTime, fTraceStartTime + INITIAL_WINDOW_SPAN);
        }

        sendFullRangeRequest(fullRange);
    }

    /**
     * Handles the trace updated signal. Used to update time limits (start and end time)
     * @param signal the trace updated signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
        if (signal.getTrace() != fTrace) {
            return;
        }
        TmfTimeRange fullRange = signal.getTrace().getTimeRange();
        fTraceStartTime = fullRange.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        fTraceEndTime = fullRange.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        fFullTraceHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fTimeRangeHistogram.setFullRange(fTraceStartTime, fTraceEndTime);

        if ((fFullTraceRequest != null) && fFullTraceRequest.getRange().getEndTime().compareTo(signal.getRange().getEndTime()) < 0) {
            sendFullRangeRequest(fullRange);
        }
}

    /**
     * Handles the current time updated signal. Sets the current time in the time range
     * histogram as well as the full histogram.
     *
     * @param signal the signal to process
     */
    @TmfSignalHandler
    public void currentTimeUpdated(TmfTimeSynchSignal signal) {
        // Because this can't happen :-)
        assert (signal != null);

        // Update the selected event time
        ITmfTimestamp currentTime = signal.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE);
        fCurrentTimestamp = currentTime.getValue();

        // Notify the relevant widgets
        fFullTraceHistogram.setCurrentEvent(fCurrentTimestamp);
        fTimeRangeHistogram.setCurrentEvent(fCurrentTimestamp);
        fCurrentEventTimeControl.setValue(fCurrentTimestamp);
    }

    /**
     * Updates the current time range in the time range histogram and full range histogram.
     * @param signal the signal to process
     */
    @TmfSignalHandler
    public void timeRangeUpdated(TmfRangeSynchSignal signal) {
        // Because this can't happen :-)
        assert (signal != null);

        if (fTrace != null) {
            // Update the time range
            fWindowStartTime = signal.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
            fWindowEndTime = signal.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
            fWindowSpan = fWindowEndTime - fWindowStartTime;

            // Notify the relevant widgets
            sendTimeRangeRequest(fWindowStartTime, fWindowEndTime);
            fFullTraceHistogram.setTimeRange(fWindowStartTime, fWindowSpan);

            fTimeSpanControl.setValue(fWindowSpan);
        }
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private void initializeHistograms() {
        TmfTimeRange fullRange = updateTraceTimeRange();

        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        fTimeRangeHistogram.clear();
        fTimeRangeHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fTimeRangeHistogram.setTimeRange(fTraceStartTime, INITIAL_WINDOW_SPAN);
        fTimeRangeHistogram.setCurrentEvent(fTraceStartTime);

        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }
        fFullTraceHistogram.clear();
        fFullTraceHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fFullTraceHistogram.setTimeRange(fTraceStartTime, INITIAL_WINDOW_SPAN);
        fFullTraceHistogram.setCurrentEvent(fTraceStartTime);

        fWindowStartTime = fTraceStartTime;
        fWindowSpan = INITIAL_WINDOW_SPAN;
        fWindowEndTime = fWindowStartTime + fWindowSpan;

        fCurrentEventTimeControl.setValue(fTraceStartTime);

        fTimeSpanControl.setValue(fWindowSpan);

        if (!fullRange.equals(TmfTimeRange.NULL_RANGE)) {
            sendTimeRangeRequest(fTraceStartTime, fTraceStartTime + fWindowSpan);
            sendFullRangeRequest(fullRange);
        }
    }

    private TmfTimeRange updateTraceTimeRange() {
        fTraceStartTime = 0L;
        fTraceEndTime = 0L;
        fCurrentTimestamp = 0L;

        TmfTimeRange timeRange = fTrace.getTimeRange();
        if (!timeRange.equals(TmfTimeRange.NULL_RANGE)) {
            fTraceStartTime = timeRange.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
            fTraceEndTime = timeRange.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
            fCurrentTimestamp = fTraceStartTime;
        }
        return timeRange;
    }

    private void sendTimeRangeRequest(long startTime, long endTime) {
        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        TmfTimestamp startTS = new TmfTimestamp(startTime, ITmfTimestamp.NANOSECOND_SCALE);
        TmfTimestamp endTS = new TmfTimestamp(endTime, ITmfTimestamp.NANOSECOND_SCALE);
        TmfTimeRange timeRange = new TmfTimeRange(startTS, endTS);

        fTimeRangeHistogram.clear();
        fTimeRangeHistogram.setTimeRange(startTime, endTime - startTime);

        int cacheSize = fTrace.getCacheSize();
        fTimeRangeRequest = new HistogramRequest(fTimeRangeHistogram.getDataModel(), timeRange, 0, TmfDataRequest.ALL_DATA, cacheSize, ExecutionType.FOREGROUND);
        fTrace.sendRequest(fTimeRangeRequest);
    }

    private void sendFullRangeRequest(TmfTimeRange fullRange) {
        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }
        int cacheSize = fTrace.getCacheSize();
        fFullTraceRequest = new HistogramRequest(fFullTraceHistogram.getDataModel(), fullRange, (int) fFullTraceHistogram.fDataModel.getNbEvents(),
                TmfDataRequest.ALL_DATA, cacheSize, ExecutionType.BACKGROUND);
        fTrace.sendRequest(fFullTraceRequest);
    }

}
