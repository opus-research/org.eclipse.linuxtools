/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam- Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.tracing.rcp.ui.commands;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tracing.rcp.ui.TracingRcpPlugin;
import org.eclipse.linuxtools.internal.tracing.rcp.ui.messages.Messages;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Trace text dump, this will help output the data
 *
 * @author Matthew Khouzam
 *
 */
public class TextDump {

    static long nbEvent = 0L;

    /**
     * Read a trace and dump it to the console
     *
     * @param path
     *            the path of the file to open
     * @return Status.OK if successful, otherwise the error.
     */
    public static IStatus babelTrace(String path) {
        if (path == null) {
            return new Status(IStatus.ERROR, TracingRcpPlugin.PLUGIN_ID, Messages.TextDump_NoOpenTrace);
        }
        TmfTraceType tt = TmfTraceType.getInstance();
        String[] traceTypes = tt.getAvailableTraceTypes();
        ArrayList<String> candidates = new ArrayList<String>();
        for (String traceType : traceTypes) {
            String[] traceInfo = traceType.split(":", 2); //$NON-NLS-1$
            String traceTypeId = tt.getTraceTypeId(traceInfo[0], traceInfo[1]);

            if (tt.validate(traceTypeId, path)) {
                candidates.add(traceType);
            }
        }
        String traceTypeToSet;
        if (candidates.isEmpty()) {
            final String errorMsg = Messages.TextDump_NoTypeMatch + path;
            return new Status(IStatus.ERROR, TracingRcpPlugin.PLUGIN_ID,
                    errorMsg);
        } else if (candidates.size() != 1) {
            Shell shell = new Shell(Display.getDefault());
            traceTypeToSet = new OpenTraceHelper().getTraceTypeToSet(candidates, shell);
            if (traceTypeToSet == null) {
                return Status.CANCEL_STATUS;
            }
        } else {
            traceTypeToSet = candidates.get(0);
        }

        ITmfTrace trace = new CtfTmfTrace();
        try {
            trace.initTrace(null, path, CtfTmfEvent.class);
        } catch (final TmfTraceException e) {
            return new Status(IStatus.ERROR, TracingRcpPlugin.PLUGIN_ID, e.getMessage(), e);
        }

        if (nbEvent != -1) {
            final ITmfContext traceContext = trace.seekEvent(0);

            ITmfEvent current = trace.getNext(traceContext);
            ITmfTimestamp prev = current.getTimestamp();
            while (current != null) {
                nbEvent++;

                System.out.println(Long.toString(nbEvent) +
                        '\t' + current.getTimestamp().toString() +
                        '\t' + current.getTimestamp().getDelta(prev) +
                        '\t' + current.toString());
                // advance the trace to the next event.
                prev = current.getTimestamp();
                current = trace.getNext(traceContext);
            }
        }
        return Status.OK_STATUS;
    }
}
