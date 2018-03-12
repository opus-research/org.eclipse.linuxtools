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

package org.eclipse.linuxtools.tmf.ui.viewers.events.columns;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;

/**
 * Event table column that will print the value of a given event field, and
 * whose column name is also the same as that field.
 *
 * @author Alexandre Montplaisir
 * @since 3.1
 */
@NonNullByDefault
public class TmfEventTableFieldColumn extends TmfEventTableColumn {

    private final String fFieldName;

    /**
     * Constructor with no tooltip
     *
     * @param headerAndFieldName
     *            The string that is both the title of the column AND the field
     *            name to look for.
     */
    public TmfEventTableFieldColumn(String headerAndFieldName) {
        super(headerAndFieldName);
        fFieldName = headerAndFieldName;
    }

    /**
     * Constructor with a tooltip.
     *
     * @param headerAndFieldName
     *            The string that is both the title of the column AND the field
     *            name to look for.
     * @param headerTooltip
     *            The tooltip text for the column header. Use 'null' for no
     *            tooltip.
     */
    public TmfEventTableFieldColumn(String headerAndFieldName,
            @Nullable String headerTooltip) {
        super(headerAndFieldName, headerTooltip);
        fFieldName = headerAndFieldName;
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

    // ------------------------------------------------------------------------
    // hashCode/equals (so that equivalent columns can be merged together)
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fFieldName.hashCode();
        result = prime * result + getHeaderName().hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfEventTableFieldColumn)) {
            return false;
        }
        TmfEventTableFieldColumn other = (TmfEventTableFieldColumn) obj;
        if (!fFieldName.equals(other.fFieldName)) {
            return false;
        }
        if (!getHeaderName().equals(other.getHeaderName())) {
            return false;
        }
        return true;
    }

}
