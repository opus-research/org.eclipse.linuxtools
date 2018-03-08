/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.event;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Base implementation of ITmfEvent for use in tmf.core unit tests.
 *
 * @author Alexandre Montplaisir
 */
public class TmfEventStub extends TmfEvent {

    /**
     * Default constructor
     */
    public TmfEventStub() {
        super();
    }

    /**
     * Full constructor without rank.
     *
     * @param trace
     *            The parent trace
     * @param timestamp
     *            The event timestamp
     * @param source
     *            The event source
     * @param type
     *            The event type
     * @param content
     *            The event content (payload)
     * @param reference
     *            The event reference
     */
    public TmfEventStub(ITmfTrace trace, ITmfTimestamp timestamp, String source,
            ITmfEventType type, ITmfEventField content, String reference) {
        super(trace, timestamp, source, type, content, reference);
    }

    /**
     * Full constructor
     *
     * @param trace
     *            The parent trace
     * @param rank
     *            The event rank (index in the trace)
     * @param timestamp
     *            The event timestamp
     * @param source
     *            The event source
     * @param type
     *            The event type
     * @param content
     *            The event content (payload)
     * @param reference
     *            The event reference
     */
    public TmfEventStub(ITmfTrace trace, long rank, ITmfTimestamp timestamp, String source,
            ITmfEventType type, ITmfEventField content, String reference) {
        super(trace, rank, timestamp, source, type, content, reference);
    }

    /**
     * Copy constructor
     *
     * @param other
     *            Other event to copy
     */
    public TmfEventStub(ITmfEvent other) {
        super(other);
    }

    @Override
    public String toString() {
        /* Replace the first word (the name of the base class) with our own */
        String origString = super.toString();
        StringBuilder sb = new StringBuilder();
        sb.append("TmfEventStub"); //$NON-NLS-1$
        sb.append(origString.substring(origString.indexOf(' ')));
        return sb.toString();
    }
}
