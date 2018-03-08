/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.event.matchandsync;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;

/**
 * Definitions used by all tests using CTF trace files
 *
 * @author gbastien
 *
 */
@SuppressWarnings("javadoc")
public abstract class MatchAndSyncTestFiles {

    /*
     * To run these tests, you will first need to run the get-traces.sh script
     * located under lttng/org.eclipse.linuxtools.ctf.core.tests/traces/ .
     */
    public final static String traceFile1 = "../org.eclipse.linuxtools.ctf.core.tests/traces/synctraces/scpM1"; //$NON-NLS-1$
    public final static String traceFile2 = "../org.eclipse.linuxtools.ctf.core.tests/traces/synctraces/scpM2"; //$NON-NLS-1$

    protected static CtfTmfTrace fTrace1;
    protected static CtfTmfTrace fTrace2;

    public synchronized static CtfTmfTrace getTestTrace() throws TmfTraceException {
        return getTestTrace(1);
    }

    public synchronized static CtfTmfTrace getTestTrace(int traceNo) throws TmfTraceException {
        if (traceNo == 2) {
            if (fTrace2 == null) {
                fTrace2 = new CtfTmfTrace();
                fTrace2.initTrace(null, traceFile2, CtfTmfEvent.class);
            }
            return fTrace2;
        }
        if (fTrace1 == null) {
            fTrace1 = new CtfTmfTrace();
            fTrace1.initTrace(null, traceFile1, CtfTmfEvent.class);
        }
        return fTrace1;

    }

}
