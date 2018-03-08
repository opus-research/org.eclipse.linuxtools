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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.shared;

import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTraces;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;

/**
 * Available CTF TMF test traces. Kind-of-extends {@link CtfTestTraces}.
 *
 * To run tests using these, you first need to run the "get-traces.[xml|sh]"
 * script located under lttng/org.eclipse.linuxtools.ctf.core.tests/traces/ .
 *
 * @author Alexandre Montplaisir
 */
public enum CtfTmfTestTraces {
    /** Example kernel trace */
    KERNEL,
    /** Another kernel trace */
    TRACE2,
    /** Kernel trace with event contexts */
    KERNEL_VM,
    /** UST trace with lots of lost events */
    HELLO_LOST;


    private final String fPath;
    private CtfTmfTrace fTrace = null;

    private CtfTmfTestTraces() {
        /* This makes my head spin */
        fPath = CtfTestTraces.valueOf(this.name()).getPath();
    }

    /**
     * @return The path of this trace
     */
    public String getPath() {
        return fPath;
    }

    /**
     * Return a CtfTmfTrace object of this test trace. It will be already
     * initTrace()'ed.
     *
     * Make sure you call {@link #exists()} before calling this!
     *
     * @return A CtfTmfTrace reference to this trace
     */
    public CtfTmfTrace getTrace() {
        if (fTrace == null) {
            CtfTmfTrace trace = new CtfTmfTrace();
            try {
                trace.initTrace(null, fPath, CtfTmfEvent.class);
            } catch (TmfTraceException e) {
                /* Should not happen if tracesExist() passed */
                throw new RuntimeException(e);
            }
            fTrace = trace;
        }
        return fTrace;
    }

    /**
     * Check if the trace actually exists on disk or not.
     *
     * @return If the trace is present
     */
    public boolean exists() {
        return CtfTestTraces.valueOf(this.name()).exists();
    }
}
