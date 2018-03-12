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

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;

/**
 * Event table column that will print the value of a given event field.
 *
 * @author Alexandre Montplaisir
 * @since 3.1
 */
public class TmfEventTableFieldColumn extends TmfEventTableColumn {

    private final String fFieldName;

    /**
     * Constructor
     *
     * @param headerName
     *            The name of the column's header, in the table
     * @param fieldName
     *            The event field to look for to populate the column
     */
    public TmfEventTableFieldColumn(String headerName, String fieldName) {
        super(headerName);
        fFieldName = fieldName;
    }

    @Override
    public final String getItemString(ITmfEvent event) {
        ITmfEventField field = event.getContent().getField(fFieldName);
        if (field == null) {
            return EMPTY_STRING;
        }
        String val = field.getFormattedValue();
        return (val == null ? EMPTY_STRING : val);
    }

}
