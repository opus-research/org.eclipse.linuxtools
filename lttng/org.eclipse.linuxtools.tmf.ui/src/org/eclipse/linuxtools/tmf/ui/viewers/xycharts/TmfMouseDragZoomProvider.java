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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.swtchart.IAxis;
import org.swtchart.ICustomPaintListener;
import org.swtchart.IPlotArea;

/**
 * Class for providing zooming based on mouse drag with left mouse button. It
 * also notifies the viewer about a change of range.
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
public class TmfMouseDragZoomProvider extends TmfBaseProvider implements MouseListener, MouseMoveListener, ICustomPaintListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** Cached start position of mouse drag */
    private int fStartX = 0;
    /** Cached end position of mouse drag */
    private int fEndX = 0;
    /** Cached start time */
    private double fStartTime;
    /** Cached end time */
    private double fEndTime;
    /** Flag indicating that an update is ongoing */
    private boolean fIsUpdate;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     *
     * @param tmfChartViewer
     *            the chart viewer reference.
     */
    public TmfMouseDragZoomProvider(TmfXYChartViewer tmfChartViewer) {
        super(tmfChartViewer);
        register();
    }

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
        getChart().getPlotArea().addMouseListener(this);
        getChart().getPlotArea().addMouseMoveListener(this);
        ((IPlotArea) getChart().getPlotArea()).addCustomPaintListener(this);
    }

    @Override
    public void deregister() {
        if ((getChartViewer().getControl() != null) && !getChartViewer().getControl().isDisposed()) {
            getChart().getPlotArea().removeMouseListener(this);
            getChart().getPlotArea().removeMouseMoveListener(this);
            ((IPlotArea) getChart().getPlotArea()).removeCustomPaintListener(this);
        }
    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
    }

    @Override
    public void mouseDown(MouseEvent e) {
        if (e.button == 1) {
            fStartX = e.x;
            fEndX = fStartX;
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);
            fStartTime = xAxis.getDataCoordinate(fStartX);
            fEndTime = fStartTime;
            fIsUpdate = true;
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if ((fIsUpdate) && (fStartX != fEndX)) {
            if (fStartX > fEndX) {
                double tmp = fStartTime;
                fStartTime = fEndTime;
                fEndTime = tmp;
            }
            getChartViewer().updateWindow((long) fStartTime, (long) fEndTime);
        }

        if (fIsUpdate) {
            getChart().redraw();
        }
        fIsUpdate = false;
    }

    @Override
    public void mouseMove(MouseEvent e) {
        if (fIsUpdate) {
            fEndX = e.x;
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);
            fEndTime = xAxis.getDataCoordinate(fEndX);
            getChart().redraw();
        }
    }

    @Override
    public void paintControl(PaintEvent e) {
        if (fIsUpdate && (fStartX != fEndX)) {
            if (fStartX < fEndX) {
                e.gc.setBackground(TmfXYChartViewer.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
                e.gc.fillRectangle(fStartX, 0, fEndX - fStartX, e.height);
            } else {
                e.gc.setBackground(TmfXYChartViewer.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
                e.gc.fillRectangle(fEndX, 0, fStartX - fEndX, e.height);
            }
            e.gc.drawLine(fStartX, 0, fStartX, e.height);
            e.gc.drawLine(fEndX, 0, fEndX, e.height);
        }
    }

    @Override
    public boolean drawBehindSeries() {
        return true;
    }
}
