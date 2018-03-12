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
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import org.eclipse.swt.SWT;

/**
 * A column in the {@link TmfEventsTable}. In addition to ones provided by
 * default, trace types can extend this class to create additional columns
 * specific to their events.
 *
 * Those additional columns can then be passed to the constructor
 * {@link TmfEventsTable#TmfEventsTable(org.eclipse.swt.widgets.Composite, int, java.util.Collection)}
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
     * Constructor to which we can specify the column configuration. These
     * values are then passed to {@link ColumnData}'s constructor.
     *
     * @param headerName
     *            The name (title) of this column
     * @param width
     *            The initial width of the column
     * @param alignment
     *            The alignement of the text in this column (like
     *            {@link SWT#LEFT}, {@link SWT#RIGHT} etc.)
     */
    public TmfEventTableColumn(String headerName, int width, int alignment) {
        fColumnData = new ColumnData(headerName, width, alignment);
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

// ------------------------------------------------------------------------
// Default column definitions
// ------------------------------------------------------------------------

/**
 * Column for the timestamps
 */
class TmfTimestampColumn extends TmfEventTableColumn {
    public TmfTimestampColumn() {
        super(Messages.TmfEventsTable_TimestampColumnHeader, 100, SWT.LEFT);
    }
    @Override
    public String getItemString(ITmfEvent event) {
        String ret = event.getTimestamp().toString();
        return (ret == null ? EMPTY_STRING : ret);
    }
}

/**
 * Column for the event source
 */
class TmfSourceColumn extends TmfEventTableColumn {
    public TmfSourceColumn() {
        super(Messages.TmfEventsTable_SourceColumnHeader, 100, SWT.LEFT);
    }
    @Override
    public String getItemString(ITmfEvent event) {
        String ret = event.getSource();
        return (ret == null ? EMPTY_STRING : ret);
    }
}

/**
 * Column for the event type
 */
class TmfTypeColumn extends TmfEventTableColumn {
    public TmfTypeColumn() {
        super(Messages.TmfEventsTable_TypeColumnHeader, 100, SWT.LEFT);
    }
    @Override
    public String getItemString(ITmfEvent event) {
        ITmfEventType type = event.getType();
        if (type == null) {
            return EMPTY_STRING;
        }
        String typeName = type.getName();
        return (typeName == null ? EMPTY_STRING : typeName);
    }
}

/**
 * Column for the event reference
 */
class TmfReferenceColumn extends TmfEventTableColumn {
    public TmfReferenceColumn() {
        super(Messages.TmfEventsTable_ReferenceColumnHeader, 100, SWT.LEFT);
    }
    @Override
    public String getItemString(ITmfEvent event) {
        String ret = event.getReference();
        return (ret == null ? EMPTY_STRING : ret);
    }
}

/**
 * Column for the event contents (fields)
 */
class TmfContentsColumn extends TmfEventTableColumn {
    public TmfContentsColumn() {
        super(Messages.TmfEventsTable_ContentColumnHeader, 100, SWT.LEFT);
    }
    @Override
    public String getItemString(ITmfEvent event) {
        String ret = event.getContent().toString();
        return (ret == null ? EMPTY_STRING : ret);
    }
}
