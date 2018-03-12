/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/


package org.eclipse.linuxtools.tmf.core.event;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * Time shifted event, can copy itself with an new time
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
interface ITimeshiftedTmfEvent extends ITmfEvent {

    /**
     * Copy an event but add a new timestamp
     *
     * @param newTimestamp
     *            the new timestamp
     * @return the new event
     */
    ITmfEvent copy(ITmfTimestamp newTimestamp);
}
