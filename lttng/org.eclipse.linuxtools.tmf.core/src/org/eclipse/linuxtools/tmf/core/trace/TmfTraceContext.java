/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Support selection range
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;

/**
 * Context of a trace, which is the representation of the "view" the user
 * currently has on this trace (selected time range, selected time stamp).
 *
 * TODO could be extended to support the notion of current location too.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
final class TmfTraceContext {

    static final TmfTraceContext NULL_CONTEXT =
            new TmfTraceContext(TmfTimestamp.BIG_CRUNCH, TmfTimestamp.BIG_CRUNCH, TmfTimeRange.NULL_RANGE);

    private final ITmfTimestamp fSelectionBegin;
    private final ITmfTimestamp fSelectionEnd;
    private final TmfTimeRange fTimerange;

    public TmfTraceContext(ITmfTimestamp beginTs, ITmfTimestamp endTs, TmfTimeRange tr) {
        fSelectionBegin = beginTs;
        fSelectionEnd = endTs;
        fTimerange = tr;
    }

    public TmfTraceContext(TmfTraceContext prevCtx, ITmfTimestamp beginTs, ITmfTimestamp endTs) {
        fSelectionBegin = beginTs;
        fSelectionEnd = endTs;
        fTimerange = prevCtx.fTimerange;
    }

    public TmfTraceContext(TmfTraceContext prevCtx, TmfTimeRange tr) {
        fSelectionBegin = prevCtx.fSelectionBegin;
        fSelectionEnd = prevCtx.fSelectionEnd;
        fTimerange = tr;
    }

    public ITmfTimestamp getSelectionBegin() {
        return fSelectionBegin;
    }

    public ITmfTimestamp getSelectionEnd() {
        return fSelectionEnd;
    }

    public TmfTimeRange getTimerange() {
        return fTimerange;
    }

    public boolean isValid() {
        if (fSelectionBegin.compareTo(TmfTimestamp.ZERO) <= 0 ||
                fSelectionEnd.compareTo(TmfTimestamp.ZERO) <= 0 ||
                fTimerange.getEndTime().compareTo(fTimerange.getStartTime()) <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[fSelectionBegin=" + fSelectionBegin.toString() + //$NON-NLS-1$
                ", fSelectionEnd=" + fSelectionEnd.toString() + //$NON-NLS-1$
                ", fTimerange=" + fTimerange + ']'; //$NON-NLS-1$
    }
}
