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
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventTableColumn;

/**
 * Column for the event type
 */
public final class TmfTypeColumn extends TmfEventTableColumn {

    /**
     * Constructor
     */
    public TmfTypeColumn() {
        super(Messages.TmfEventsTable_TypeColumnHeader);
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