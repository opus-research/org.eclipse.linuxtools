/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Alexandre Montplaisir - Update for TmfEventTableColumn
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomEvent;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventTableColumn;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.swt.widgets.Composite;

/**
 * Events table for custom text parsers.
 *
 * @author Patrick Tassé
 */
public class CustomEventsTable extends TmfEventsTable {

    /**
     * Column for custom events, which uses an integer ID to represent each
     * column.
     */
    private static final class CustomEventTableColumn extends TmfEventTableColumn {

        private final int fId;

        /**
         * Constructor
         *
         * @param name
         *            The name (title) of this column
         * @param id
         *            The ID of this column. The event fields with this ID will
         *            be displayed in this column's cell.
         */
        public CustomEventTableColumn(String name, int id) {
            super(name);
            fId = id;
        }

        @Override
        public String getItemString(ITmfEvent event) {
            if (event instanceof CustomEvent) {
                String ret = ((CustomEvent) event).getEventString(fId);
                return (ret == null ? EMPTY_STRING : ret);
            }
            return EMPTY_STRING;
        }

    }

    /**
     * Constructor.
     *
     * @param definition
     *            Trace definition object
     * @param parent
     *            Parent composite of the view
     * @param cacheSize
     *            How many events to keep in cache
     */
    public CustomEventsTable(CustomTraceDefinition definition, Composite parent, int cacheSize) {
        super(parent, cacheSize, generateColumns(definition));
    }

    private static Collection<CustomEventTableColumn> generateColumns(CustomTraceDefinition definition) {
        List<CustomEventTableColumn> columns = new LinkedList<>();
        List<OutputColumn> outputs = definition.outputs;
        for (int i = 0; i < outputs.size(); i++) {
            columns.add(new CustomEventTableColumn(outputs.get(i).name, i));
        }
        return columns;
    }
}
