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
 *                   (inspired by HistogramZoom implementations)
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.viewers.xycharts;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.swtchart.IAxis;

/**
 * Class for providing selection of current time. It draws a vertical line at
 * the current selected time. The current time can be changed by clicking into
 * the chart or can be set externally. It also notify is viewer about the
 * selection.
 *
 * @since 3.0
 */
public class TmfMouseSelectionProvider extends TmfBaseProvider implements MouseListener, PaintListener {
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** Current X position */
    private int fCurrentX = 0;

    /** Value of current time corresponding to the current x position. */
    private double fCurrentTime;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor.
     *
     * @param tmfChartViewer
     *            The parent histogram object
     */
    public TmfMouseSelectionProvider(TmfXYChartViewer tmfChartViewer) {
        super(tmfChartViewer);
        register();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Method sets the current time received from other views or means. It will
     * draw a vertical line on the current position.
     *
     * @param currentTime
     *            the current time to select
     */
    public void setSelectedTime(long currentTime) {
        fCurrentTime = currentTime;
        if (!TmfXYChartViewer.getDisplay().isDisposed()) {
            TmfXYChartViewer.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    getChart().redraw();
                }
            });
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    public void dispose() {
        if ((getChartViewer().getControl() != null) && !getChartViewer().getControl().isDisposed()) {
            deregister();
        }
    }

    @Override
    public void register() {
        getChart().getPlotArea().addMouseListener(this);
        getChart().getPlotArea().addPaintListener(this);
    }

    @Override
    public void deregister() {
        getChart().getPlotArea().removeMouseListener(this);
        getChart().getPlotArea().removePaintListener(this);
    }

    // ------------------------------------------------------------------------
    // Listeners
    // ------------------------------------------------------------------------
    @Override
    public void paintControl(PaintEvent e) {
        IAxis xAxis = getChart().getAxisSet().getXAxis(0);
        int current = xAxis.getPixelCoordinate(fCurrentTime);
        e.gc.drawLine(current, 0, current, e.height);
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if ((e.button == 1) && (fCurrentX == e.x)) {
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);
            fCurrentTime = xAxis.getDataCoordinate(fCurrentX);
            if (fCurrentTime < 0.0) {
                fCurrentTime = 0;
            }
            long time = (long) fCurrentTime;
            getChartViewer().updateCurrentTime(time);
            getChart().redraw();
        }
    }

    @Override
    public void mouseDown(MouseEvent e) {
        if (e.button == 1) {
            fCurrentX = e.x;
        }
    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
    }
}
