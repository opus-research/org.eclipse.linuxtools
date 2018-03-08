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

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.swtchart.IAxis;

/**
 * Tool tip provider for TMF chart viewer. It displays the x and y value of the
 * mouse position.
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
public class TmfSimpleTooltipProvider extends TmfBaseProvider implements MouseTrackListener {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor for a tool tip provider.
     *
     * @param tmfChartViewer
     *            The parent chart viewer
     */
    public TmfSimpleTooltipProvider(TmfXYChartViewer tmfChartViewer) {
        super(tmfChartViewer);
        register();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        deregister();
    }

    @Override
    public void register() {
        getChart().getPlotArea().addMouseTrackListener(this);
    }

    @Override
    public void deregister() {
        if ((getChartViewer().getControl() != null) && !getChartViewer().getControl().isDisposed()) {
            getChart().getPlotArea().removeMouseTrackListener(this);
        }
    }

    @Override
    public void mouseEnter(MouseEvent e) {
    }

    @Override
    public void mouseExit(MouseEvent e) {
    }

    @Override
    public void mouseHover(MouseEvent e) {
        IAxis xAxis = getChart().getAxisSet().getXAxis(0);
        IAxis yAxis = getChart().getAxisSet().getYAxis(0);

        double xCoordinate = xAxis.getDataCoordinate(e.x);
        double yCoordinate = yAxis.getDataCoordinate(e.y);
        /* set tooltip of closest data point */
        StringBuffer buffer = new StringBuffer();
        buffer.append("x="); //$NON-NLS-1$
        buffer.append(new TmfTimestamp((long) xCoordinate + getChartViewer().getXOffset(), ITmfTimestamp.NANOSECOND_SCALE).toString());
        buffer.append("\n"); //$NON-NLS-1$
        buffer.append("y="); //$NON-NLS-1$
        buffer.append((long) yCoordinate + getChartViewer().getYOffset());
        getChart().getPlotArea().setToolTipText(buffer.toString());
    }
}
