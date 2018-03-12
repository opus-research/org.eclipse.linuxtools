/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.offset;

import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Pair of trace and object not using generics
 *
 * @author Matthew Khouzam
 *
 */
class TraceAndOffset {

    private final ITmfTrace fTrace;
    private final Map<ITmfTrace, Long> fTraceMapCache;
    private final String fStartTime;

    public TraceAndOffset(ITmfTrace trace, Map<ITmfTrace, Long> traceMapCache) {
        fTrace = trace;
        fTraceMapCache = traceMapCache;
        fStartTime = calculateStartTime();
    }

    public long getOffset() {
        return fTraceMapCache.get(fTrace);
    }

    public ITmfTrace getTrace() {
        return fTrace;
    }

    public void setOffset(long value) {
        fTraceMapCache.put(fTrace, value);
    }

    public String getTraceName(){
        return fTrace.getName();
    }

    public String getStartTime() {
        return fStartTime;
    }

    protected String calculateStartTime() {
        try {
            fTrace.initTrace(fTrace.getResource(), fTrace.getPath(), ITmfEvent.class);
            ITmfContext ctx = fTrace.seekEvent(0);
            ITmfEvent evt = fTrace.getNext(ctx);
            return Long.toString(evt.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue());
        } catch (TmfTraceException e) {
        }
        return Messages.OffsetDialog_NA;
    }

}
