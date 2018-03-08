/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.viewers.ITmfViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.swt.widgets.Composite;

/**
 * Interface to be implemented by all statistics viewers. It is used by the
 * statistics view.
 *
 * @author Mathieu Denis
 * @version 2.0
 * @since 2.0
 */
public interface ITmfStatisticsViewer extends ITmfViewer {

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
    public void init(Composite parent, String viewerName, ITmfTrace trace);

    /**
     * Get the input of the viewer.
     *
     * @return an object representing the input of the statistics viewer.
     */
    public Object getInput();

    /**
     * @return the quantity of data to retrieve before a refresh of the view is
     *         performed.
     */
    public long getInputChangedRefresh();

    /**
     * @return the trace that is displayed by this viewer.
     */
    public ITmfTrace getTrace();

    /**
     * Returns a unique ID based on name to be associated with the statistics
     * tree for this viewer. For a same name, it will always return the same ID.
     *
     * @param name
     *            The name of the viewer preferably, or any other string.
     * @return a unique statistics tree ID.
     */
    public String getTreeID(String name);

    /**
     * This method can be overridden to implement another way of representing
     * the statistics data and to retrieve the information for display.
     *
     * @return a TmfStatisticsData object.
     */
    public AbsTmfStatisticsTree getStatisticData();

    /**
     * Tells the viewer to refresh its contents.
     */
    public void refresh();

    /**
     * Sets or clears the input for this viewer.
     *
     * @param input
     *            The input of this viewer, or <code>null</code> if none
     */
    public void setInput(TmfStatisticsTreeNode input);

    /**
     * When the experiment is loading the cursor will be different so the user
     * knows that the processing is not finished yet.
     *
     * @param waitInd
     *            Indicates if we need to show the waiting cursor, or the
     *            default one
     */
    public void waitCursor(final boolean waitInd);
}
