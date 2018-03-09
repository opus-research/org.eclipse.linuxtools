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
package org.eclipse.linuxtools.tmf.ui.views;

import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * Base class to be used with chart viewer {@link TmfXYChartViewer}. It provides
 * base signal handling for trace open, trace selected and trace closed.
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
abstract public class TmfChartView extends TmfView {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Standard Constructor
     *
     * @param viewName
     *            The view name
     */
    public TmfChartView(String viewName) {
        super(viewName);
    }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private TmfXYChartViewer fChartViewer;
    private ITmfTrace fTrace;

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns the TMF XY chart viewer implementation.
     *
     * @return the TMF XY chart viewer {@link TmfXYChartViewer}
     */
    public TmfXYChartViewer getChartViewer() {
        return fChartViewer;
    }

    /**
     * Sets the TMF XY chart viewer implementation.
     *
     * @param chartViewer
     *            the TMF XY chart viewer {@link TmfXYChartViewer}
     */
    protected void setChartViewer(TmfXYChartViewer chartViewer) {
        fChartViewer = chartViewer;
    }

    /**
     * Returns the ITmfTrace implementation
     *
     * @return the ITmfTrace implementation {@link ITmfTrace}
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {
        ITmfTrace trace = getActiveTrace();
        if (trace != null) {
            fTrace = trace;
            initializeViewer();
        }
    }

    @Override
    public void dispose() {
        if (fChartViewer != null) {
            fChartViewer.dispose();
        }
    }

    /**
     * Signal handler for handling of the trace opened signal.
     *
     * @param signal
     *            The trace opened signal {@link TmfTraceOpenedSignal}
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        fTrace = signal.getTrace();
        initializeViewer();
    }

    /**
     * Signal handler for handling of the trace selected signal.
     *
     * @param signal
     *            The trace selected signal {@link TmfTraceSelectedSignal}
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        if (fTrace != signal.getTrace()) {
            fTrace = signal.getTrace();
            initializeViewer();
        }
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
        fChartViewer.clearView();
    }

    /**
     * Initializes the chart viewer
     */
    protected void initializeViewer() {
        fChartViewer.initialize(fTrace);
    }
}
