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
 *   Patrick Tasse - Make class extensible
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events.columns;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * Column for the timestamps
 * @since 3.1
 */
public class TmfTimestampColumn extends TmfEventTableColumn {

    @SuppressWarnings("null")
    private static final @NonNull String HEADER = Messages.TmfEventsTable_TimestampColumnHeader;

    /**
     * Constructor
     */
    public TmfTimestampColumn() {
        super(HEADER);
    }

    /**
     * Constructor with header name
     * @param headerName
     *            The name (title) of this column.
     */
    public TmfTimestampColumn(@NonNull String headerName) {
        super(headerName);
    }

    /**
     * Constructor with header name and tool tip
     * @param headerName
     *            The name (title) of this column.
     * @param headerTooltip
     *            The tool tip text for the column header.
     */
    public TmfTimestampColumn(@NonNull String headerName, @NonNull String headerTooltip) {
        super(headerName, headerTooltip);
    }

    @Override
    public String getItemString(ITmfEvent event) {
        String ret = event.getTimestamp().toString();
        return (ret == null ? EMPTY_STRING : ret);
    }

    @Override
    public String getFilterFieldId() {
        return ITmfEvent.EVENT_FIELD_TIMESTAMP;
    }
}