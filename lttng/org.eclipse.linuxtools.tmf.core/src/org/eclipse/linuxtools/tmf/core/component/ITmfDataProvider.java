/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.component;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;

/**
 * This is the interface of the data providers in TMF. Data providers have the
 * capability of handling data requests.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see TmfDataProvider
 * @see TmfEventProvider
 */
public interface ITmfDataProvider extends ITmfComponent {

    /**
     * Queue the request for processing.
     *
     * @param request The request to process
     */
    void sendRequest(ITmfDataRequest request);

    /**
     * Queue the coalesced requests.
     */
    void fireRequest();

    /**
     * Increments/decrements the pending requests counters and fires the request
     * if necessary (counter == 0). Used for coalescing requests across multiple
     * TmfDataProvider's.
     *
     * @param isIncrement
     *            Should we increment (true) or decrement (false) the pending
     *            counter
     */
    void notifyPendingRequest(boolean isIncrement);

    /**
     * Return the next event based on the context supplied. The context
     * will be updated for the subsequent read.
     *
     * @param context the trace read context (updated)
     * @return the event referred to by context
     */
    ITmfEvent getNext(ITmfContext context);
}
