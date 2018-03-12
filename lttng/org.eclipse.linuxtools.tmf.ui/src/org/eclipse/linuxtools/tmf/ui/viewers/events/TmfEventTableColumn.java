/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import org.eclipse.swt.SWT;

/**
 * A column in the
 * {@link org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable}. In
 * addition to ones provided by default, trace types can extend this class to
 * create additional columns specific to their events.
 *
 * Those additional columns can then be passed to the constructor
 * {@link org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable#TmfEventsTable(org.eclipse.swt.widgets.Composite, int, java.util.Collection)}
 *
 * @author Alexandre Montplaisir
 * @since 3.1
 */
public abstract class TmfEventTableColumn {

    /**
     * Static definition of an empty string. Return this instead of returning
     * 'null'!
     */
    protected static final @NonNull String EMPTY_STRING = ""; //$NON-NLS-1$

    private final ColumnData fColumnData;

    /**
     * Constructor using default column configuration
     *
     * @param headerName
     *            The name (title) of this column
     */
    public TmfEventTableColumn(String headerName) {
        fColumnData = new ColumnData(headerName, 100, SWT.LEFT);
    }

    /**
     * Get the {@link ColumnData} object of this column. Should only be used by
     * the event table itself.
     *
     * @return The ColumnData
     */
    ColumnData getColumnData() {
        return fColumnData;
    }

    /**
     * Get the string that should be displayed in this column's cell for a given
     * trace event. Basically, this defines "what to print in this column for
     * this event".
     * <p>
     * Note to implementers:
     * <p>
     * This method takes an {@link ITmfEvent}, because any type of event could
     * potentially be present in the table at the time. Do not assume that you
     * will only receive events of your trace type. You'd probably want to
     * return an empty string for event that don't match your expected class
     * type here.
     *
     * @param event
     *            The trace event whose element we want to display
     * @return The string to display in the column for this event
     */
    public abstract @NonNull String getItemString(ITmfEvent event);
}

