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

    public TraceAndOffset(ITmfTrace trace, Map<ITmfTrace, Long> traceMapCache) {
        fTrace = trace;
        fTraceMapCache = traceMapCache;
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

}
