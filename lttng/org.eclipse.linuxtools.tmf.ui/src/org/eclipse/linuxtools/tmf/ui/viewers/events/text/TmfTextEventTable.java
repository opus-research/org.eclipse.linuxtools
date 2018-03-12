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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events.text;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.trace.text.TextTraceEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventTableColumn;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import org.eclipse.swt.widgets.Composite;

/**
 * Event table for text traces, which has one column for every event field.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
 */
public class TmfTextEventTable extends TmfEventsTable {

    /**
     * A column for text trace types. When creating the column you specify its
     * name (which will show in the header) and the ID. The column will look at
     * an event's field whose ID matches the column's ID when asked to filled
     * its corresponding cell.
     *
     * @since 3.1
     */
    protected static class TmfTextEventTableColumn extends TmfEventTableColumn {

        private final int fColId;

        /**
         * Create a new column for a text trace.
         *
         * @param colName
         *            The name (title) this column will have
         * @param colId
         *            The ID of this column. To populate its cells, it will look
         *            at the event's field with this ID.
         */
        public TmfTextEventTableColumn(String colName, int colId) {
            super(colName);
            fColId = colId;
        }

        @Override
        public String getItemString(ITmfEvent event) {
            if (event instanceof TextTraceEvent) {
                /*
                 * We will print the value of the field whose name matches this
                 * column's ID, if it exists.
                 */
                ITmfEventField field = ((TextTraceEvent) event).getContent().getFields().get(fColId);
                if (field != null) {
                    Object value = field.getValue();
                    if (value != null) {
                        String ret = value.toString();
                        return (ret == null ? EMPTY_STRING : ret);
                    }
                }
            }
            return EMPTY_STRING;
        }
    }

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite UI object
     * @param cacheSize
     *            The size of the event table cache
     * @param extraColumns
     *            The tracetype-specific columns to use for this table
     * @since 3.1
     */
    public TmfTextEventTable(Composite parent, int cacheSize,
            Collection<? extends TmfEventTableColumn> extraColumns) {
        super(parent, cacheSize, extraColumns);
    }

    /**
     * Deprecated constructor
     *
     * @param parent
     *            The parent composite UI object
     * @param cacheSize
     *            The size of the event table cache
     * @param columnData
     *            Unused
     * @deprecated Use
     *             {@link TmfTextEventTable#TmfTextEventTable(Composite, int, Collection)}
     *             instead.
     */
    @Deprecated
    public TmfTextEventTable(Composite parent, int cacheSize, ColumnData[] columnData) {
        this(parent, cacheSize, Collections.EMPTY_SET);
    }
}
