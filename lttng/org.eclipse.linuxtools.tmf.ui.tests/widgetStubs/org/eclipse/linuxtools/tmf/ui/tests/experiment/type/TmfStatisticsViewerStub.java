/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.experiment.type;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.TmfStatisticsViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * Statistics viewer for experiment type unit tests
 *
 * @author Geneviève Bastien
 */
public class TmfStatisticsViewerStub extends TmfStatisticsViewer {

    /**
     * The last statistics viewer stub to have been initialized. To be used for
     * unit tests
     */
    public static TmfStatisticsViewerStub LAST_INSTANCE = null;

    /**
     * Initialize the statistics viewer.
     *
     * @param parent
     *            The parent component of the viewer.
     * @param viewerName
     *            The name to give to the viewer.
     * @param trace
     *            The trace that will be displayed by the viewer.
     */
    @Override
    public void init(Composite parent, String viewerName, ITmfTrace trace) {
        super.init(parent, viewerName, trace);
        // Increment a counter to make sure the tree ID is unique.
        LAST_INSTANCE = this;
    }

    /**
     * Return the trace for this viewer
     *
     * @return The trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

}
