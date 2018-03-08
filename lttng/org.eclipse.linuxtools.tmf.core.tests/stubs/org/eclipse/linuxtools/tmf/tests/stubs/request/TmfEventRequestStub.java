/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.request;

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;

/**
 * <b><u>TmfEventRequestStub</u></b>
 * <p>
 * @param <T> The requested event type
 */
public class TmfEventRequestStub<T extends TmfEvent> extends TmfEventRequest<T> {

    /**
     * @param dataType the event type
     */
    public TmfEventRequestStub(final Class<T> dataType) {
        super(dataType);
    }

    /**
     * @param dataType the event type
     * @param range the requested time range
     */
    public TmfEventRequestStub(final Class<T> dataType, final TmfTimeRange range) {
        super(dataType, range);
    }

    /**
     * @param dataType the event type
     * @param range the requested time range
     * @param nbRequested the number of events requested
     */
    public TmfEventRequestStub(final Class<T> dataType, final TmfTimeRange range, final int nbRequested) {
        super(dataType, range, nbRequested);
    }

    /**
     * @param dataType the event type
     * @param range the requested time range
     * @param nbRequested the number of events requested
     * @param blockSize the event block size
     */
    public TmfEventRequestStub(final Class<T> dataType, final TmfTimeRange range, final int nbRequested, final int blockSize) {
        super(dataType, range, nbRequested, blockSize);
    }

    /**
     * @param dataType the event type
     * @param range the requested time range
     * @param index the initial event index
     * @param nbRequested the number of events requested
     * @param blockSize the event block size
     */
    public TmfEventRequestStub(final Class<T> dataType, final TmfTimeRange range, final long index, final int nbRequested, final int blockSize) {
        super(dataType, range, index, nbRequested, blockSize);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfDataRequest#handleData(org.eclipse.linuxtools.tmf.core.event.ITmfEvent)
     */
    @Override
    public void handleData(final T data) {
        super.handleData(data);
    }
}
