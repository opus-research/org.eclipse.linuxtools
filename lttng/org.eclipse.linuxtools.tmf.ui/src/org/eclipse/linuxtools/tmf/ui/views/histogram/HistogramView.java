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
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentDisposedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * The purpose of this view is to provide graphical time distribution statistics about the experiment/trace events.
 * <p>
 * The view is composed of two histograms and two controls:
 * <ul>
 * <li>an event distribution histogram for the whole experiment;
 * <li>an event distribution histogram for current time window (window span);
 * <li>the timestamp of the currently selected event;
 * <li>the window span (size of the time window of the smaller histogram).
 * </ul>
 * The histograms x-axis show their respective time range.
 *
 * @version 1.0
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

    // Time scale
    private final byte TIME_SCALE = Histogram.TIME_SCALE;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Parent widget
    private Composite fParent;

    // The current experiment
    private TmfExperiment fCurrentExperiment;

    // Current timestamp/time window
    private long fExperimentStartTime;
    private long fExperimentEndTime;
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
        gridLayout.makeColumnsEqualWidth = true;
        gridLayout.marginLeft = 5;
        gridLayout.marginRight = 5;
        controlsComposite.setLayout(gridLayout);

        // Current event time control
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.CENTER;
        gridData.verticalAlignment = SWT.CENTER;
        fCurrentEventTimeControl = new HistogramCurrentTimeControl(this, controlsComposite, SWT.BORDER, SWT.NONE,
                currentEventLabel, HistogramUtils.nanosecondsToString(0L));
        fCurrentEventTimeControl.setLayoutData(gridData);

        // Window span time control
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.CENTER;
        gridData.verticalAlignment = SWT.CENTER;
        fTimeSpanControl = new HistogramTimeRangeControl(this, controlsComposite, SWT.BORDER, SWT.NONE,
                windowSpanLabel, HistogramUtils.nanosecondsToString(0L));
        fTimeSpanControl.setLayoutData(gridData);

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

        // Load the experiment if present
        fCurrentExperiment = TmfExperiment.getCurrentExperiment();
        if (fCurrentExperiment != null) {
            loadExperiment();
        }
    }

    @Override
    public void setFocus() {
        TmfExperiment experiment = TmfExperiment.getCurrentExperiment();
        if ((experiment != null) && (experiment != fCurrentExperiment)) {
            fCurrentExperiment = experiment;
            initializeHistograms();
        }
        fParent.redraw();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the time range of the current selected window (base on default time scale).
     *
     * @return the time range of current selected window.
     */
    public TmfTimeRange getTimeRange() {
        return new TmfTimeRange(new TmfTimestamp(fWindowStartTime, TIME_SCALE), new TmfTimestamp(fWindowEndTime,
                TIME_SCALE));
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Broadcast TmfSignal about new current time value.
     * @param newTime the new current time.
     */
    public void updateCurrentEventTime(long newTime) {
        if (fCurrentExperiment != null) {
            TmfTimeRange timeRange = new TmfTimeRange(new TmfTimestamp(newTime, TIME_SCALE), TmfTimestamp.BIG_CRUNCH);
            HistogramRequest request = new HistogramRequest(fTimeRangeHistogram.getDataModel(), timeRange, 0, 1, 0, ExecutionType.FOREGROUND) {
                @Override
                public void handleData(ITmfEvent event) {
                    if (event != null) {
                        TmfTimeSynchSignal signal = new TmfTimeSynchSignal(this, event.getTimestamp());
                        TmfSignalManager.dispatchSignal(signal);
                    }
                }
            };
            fCurrentExperiment.sendRequest(request);
        }
    }

    /**
     * Broadcast TmfSignal about new selected time range.
     * @param startTime the new start time
     * @param endTime the new end time
     */
    public void updateTimeRange(long startTime, long endTime) {
        if (fCurrentExperiment != null) {
            // Build the new time range; keep the current time
            TmfTimeRange timeRange = new TmfTimeRange(new TmfTimestamp(startTime, TIME_SCALE), new TmfTimestamp(
                    endTime, TIME_SCALE));
            TmfTimestamp currentTime = new TmfTimestamp(fCurrentTimestamp, TIME_SCALE);

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
        if (fCurrentExperiment != null) {
            long delta = newDuration - fWindowSpan;
            long newStartTime = fWindowStartTime + (delta / 2);
            setNewRange(newStartTime, newDuration);
        }
    }

    private void setNewRange(long startTime, long duration) {
        if (startTime < fExperimentStartTime) {
            startTime = fExperimentStartTime;
        }

        long endTime = startTime + duration;
        if( endTime < startTime ) {
            endTime = fExperimentEndTime;
            startTime = fExperimentStartTime;
        }
        if (endTime > fExperimentEndTime) {
            endTime = fExperimentEndTime;
            if ((endTime - duration) > fExperimentStartTime) {
                startTime = endTime - duration;
            } else {
                startTime = fExperimentStartTime;
            }
        }
        updateTimeRange(startTime, endTime);
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handles experiment selected signal. Loads histogram if new experiment time range is not
     * equal <code>TmfTimeRange.NULL_RANGE</code>
     * @param signal the experiment selected signal
     */
    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal signal) {
        assert (signal != null);
        fCurrentExperiment = signal.getExperiment();
        loadExperiment();
    }

    private void loadExperiment() {
        initializeHistograms();
        fParent.redraw();
    }

    /**
     * @param signal the incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void experimentDisposed(TmfExperimentDisposedSignal signal) {

        // Kill any running request
        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }

        // Initialize the internal data
        fCurrentExperiment = null;
        fExperimentStartTime = 0;
        fExperimentEndTime = 0;
        fWindowStartTime = 0;
        fWindowEndTime = 0;
        fWindowSpan = INITIAL_WINDOW_SPAN;
        fCurrentTimestamp = 0;

        // Clear the UI widgets
        fFullTraceHistogram.clear();
        fTimeRangeHistogram.clear();
        fCurrentEventTimeControl.setValue(0);
        fTimeSpanControl.setValue(0);
    }

    /**
     * Handles experiment range updated signal. Extends histogram according to the new time range. If a
     * HistogramRequest is already ongoing, it will be cancelled and a new request with the new range
     * will be issued.
     *
     * @param signal the experiment range updated signal
     */
    @TmfSignalHandler
    public void experimentRangeUpdated(TmfExperimentRangeUpdatedSignal signal) {

        if (signal.getExperiment() != fCurrentExperiment) {
            return;
        }

        boolean drawTimeRangeHistogram = fExperimentStartTime == 0;
        TmfTimeRange fullRange = signal.getRange();

        fExperimentStartTime = fullRange.getStartTime().normalize(0, -9).getValue();
        fExperimentEndTime = fullRange.getEndTime().normalize(0, -9).getValue();

        fFullTraceHistogram.setFullRange(fExperimentStartTime, fExperimentEndTime);
        fTimeRangeHistogram.setFullRange(fExperimentStartTime, fExperimentEndTime);

        if (drawTimeRangeHistogram) {
            fCurrentTimestamp = fExperimentStartTime;
            fCurrentEventTimeControl.setValue(fCurrentTimestamp);
            fFullTraceHistogram.setTimeRange(fExperimentStartTime, INITIAL_WINDOW_SPAN);
            fTimeRangeHistogram.setTimeRange(fExperimentStartTime, INITIAL_WINDOW_SPAN);
            sendTimeRangeRequest(fExperimentStartTime, fExperimentStartTime + INITIAL_WINDOW_SPAN);
        }

        sendFullRangeRequest(fullRange);
    }

    /**
     * Handles the experiment updated signal. Used to update time limits (start and end time)
     * @param signal the experiment updated signal
     */
    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
        if (signal.getExperiment() != fCurrentExperiment) {
            return;
        }
        TmfTimeRange fullRange = signal.getExperiment().getTimeRange();
        fExperimentStartTime = fullRange.getStartTime().normalize(0, -9).getValue();
        fExperimentEndTime = fullRange.getEndTime().normalize(0, -9).getValue();

        fFullTraceHistogram.setFullRange(fExperimentStartTime, fExperimentEndTime);
        fTimeRangeHistogram.setFullRange(fExperimentStartTime, fExperimentEndTime);
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
        ITmfTimestamp currentTime = signal.getCurrentTime();
        fCurrentTimestamp = currentTime.normalize(0, -9).getValue();

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

        if (fCurrentExperiment != null) {
            // Update the time range
            fWindowStartTime = signal.getCurrentRange().getStartTime().normalize(0, -9).getValue();
            fWindowEndTime = signal.getCurrentRange().getEndTime().normalize(0, -9).getValue();
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
        TmfTimeRange fullRange = updateExperimentTimeRange();

        fTimeRangeHistogram.clear();
        fTimeRangeHistogram.setFullRange(fExperimentStartTime, fExperimentEndTime);
        fTimeRangeHistogram.setTimeRange(fExperimentStartTime, INITIAL_WINDOW_SPAN);
        fTimeRangeHistogram.setCurrentEvent(fExperimentStartTime);

        fFullTraceHistogram.clear();
        fFullTraceHistogram.setFullRange(fExperimentStartTime, fExperimentEndTime);
        fFullTraceHistogram.setTimeRange(fExperimentStartTime, INITIAL_WINDOW_SPAN);
        fFullTraceHistogram.setCurrentEvent(fExperimentStartTime);

        fWindowStartTime = fExperimentStartTime;
        fWindowSpan = INITIAL_WINDOW_SPAN;
        fWindowEndTime = fWindowStartTime + fWindowSpan;

        fCurrentEventTimeControl.setValue(fExperimentStartTime);
        fTimeSpanControl.setValue(fWindowSpan);

        if (!fullRange.equals(TmfTimeRange.NULL_RANGE)) {
            sendTimeRangeRequest(fExperimentStartTime, fExperimentStartTime + fWindowSpan);
            sendFullRangeRequest(fullRange);
        }
    }

    private TmfTimeRange updateExperimentTimeRange() {
        fExperimentStartTime = 0;
        fExperimentEndTime = 0;
        fCurrentTimestamp = 0;

        TmfTimeRange timeRange = fCurrentExperiment.getTimeRange();
        if (!timeRange.equals(TmfTimeRange.NULL_RANGE)) {
            fExperimentStartTime = timeRange.getStartTime().normalize(0, -9).getValue();
            fExperimentEndTime = timeRange.getEndTime().normalize(0, -9).getValue();
            fCurrentTimestamp = fExperimentStartTime;
        }
        return timeRange;
    }

    private void sendTimeRangeRequest(long startTime, long endTime) {
        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        TmfTimestamp startTS = new TmfTimestamp(startTime, TIME_SCALE);
        TmfTimestamp endTS = new TmfTimestamp(endTime, TIME_SCALE);
        TmfTimeRange timeRange = new TmfTimeRange(startTS, endTS);

        fTimeRangeHistogram.clear();
        fTimeRangeHistogram.setTimeRange(startTime, endTime - startTime);

        int cacheSize = fCurrentExperiment.getCacheSize();
        fTimeRangeRequest = new HistogramRequest(fTimeRangeHistogram.getDataModel(), timeRange, 0, TmfDataRequest.ALL_DATA, cacheSize, ExecutionType.FOREGROUND);
        fCurrentExperiment.sendRequest(fTimeRangeRequest);
    }

    private void sendFullRangeRequest(TmfTimeRange fullRange) {
        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }
        int cacheSize = fCurrentExperiment.getCacheSize();
        fFullTraceRequest = new HistogramRequest(fFullTraceHistogram.getDataModel(), fullRange, (int) fFullTraceHistogram.fDataModel.getNbEvents(),
                TmfDataRequest.ALL_DATA, cacheSize, ExecutionType.BACKGROUND);
        fCurrentExperiment.sendRequest(fFullTraceRequest);
    }

}
