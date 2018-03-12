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

package org.eclipse.linuxtools.internal.tmf.ui.viewers.events.columns;

import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventTableColumn;

/**
 * Column for the timestamps
 */
public final class TmfTimestampColumn extends TmfEventTableColumn {

    /**
     * Constructor
     */
    public TmfTimestampColumn() {
        super(Messages.TmfEventsTable_TimestampColumnHeader);
    }

    @Override
    public String getItemString(ITmfEvent event) {
        String ret = event.getTimestamp().toString();
        return (ret == null ? EMPTY_STRING : ret);
    }
}