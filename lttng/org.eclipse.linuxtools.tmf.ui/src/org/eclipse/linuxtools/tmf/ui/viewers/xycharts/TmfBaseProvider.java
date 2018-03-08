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

import org.swtchart.Chart;

/**
 * Base class for any provider such as tool tip, zoom and selection providers.
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
abstract public class TmfBaseProvider {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** Reference to the chart viewer */
    private final TmfXYChartViewer fChartViewer;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor.
     *
     * @param tmfChartViewer
     *            The parent histogram object
     */
    public TmfBaseProvider(TmfXYChartViewer tmfChartViewer) {
        fChartViewer = tmfChartViewer;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns the chart viewer reference.
     *
     * @return the chart viewer reference
     */
    public TmfXYChartViewer getChartViewer() {
        return fChartViewer;
    }

    /**
     * Returns the SWT chart class
     *
     * @return SWT chart @see reference.
     */
    protected Chart getChart() {
        return (Chart) fChartViewer.getControl();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Method deregisters provider from chart viewer. Subclasses may override
     * this method to dispose any resources.
     */
    public void dispose() {
        deregister();
    }

    /**
     * Method to register provider to chart viewer.
     */
    abstract public void register();

    /**
     * Method to deregister provider from chart viewer.
     */
    abstract public void deregister();
}
