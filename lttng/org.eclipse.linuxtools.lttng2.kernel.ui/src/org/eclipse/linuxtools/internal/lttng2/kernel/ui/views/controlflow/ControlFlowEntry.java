/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for time graph view
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.controlflow;

import java.util.Iterator;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.StateValues;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * An entry in the Control Flow view
 */
public class ControlFlowEntry extends TimeGraphEntry {

    private final LttngKernelTrace fTrace;
    private final int fThreadId;
    private final int fParentThreadId;
    private final int fThreadQuark;

    /**
     * Constructor
     *
     * @param quark
     *            The attribute quark matching the thread
     * @param trace
     *            The trace on which we are working
     * @param execName
     *            The exec_name of this entry
     * @param threadId
     *            The TID of the thread
     * @param parentThreadId
     *            the Parent_TID of this thread
     * @param startTime
     *            The start time of this process's lifetime
     * @param endTime
     *            The end time of this process
     */
    public ControlFlowEntry(int quark, LttngKernelTrace trace, String execName, int threadId, int parentThreadId, long startTime, long endTime) {
        super(execName, startTime, endTime);
        fTrace = trace;
        fThreadId = threadId;
        fParentThreadId = parentThreadId;
        fThreadQuark = quark;
    }

    /**
     * Get this entry's thread ID
     *
     * @return The TID
     */
    public int getThreadId() {
        return fThreadId;
    }

    /**
     * Get the entry's kernel trace
     *
     * @return the entry's kernel trace
     */
    public LttngKernelTrace getTrace() {
        return fTrace;
    }

    /**
     * Get this thread's parent TID
     *
     * @return The "PTID"
     */
    public int getParentThreadId() {
        return fParentThreadId;
    }

    /**
     * Get the quark of the attribute matching this thread's TID
     *
     * @return The quark
     */
    public int getThreadQuark() {
        return fThreadQuark;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + getName() + '[' + fThreadId + "])"; //$NON-NLS-1$
    }

    @Override
    public boolean isActive(long startTime, long endTime) {
        Iterator<ITimeEvent> iterator = getTimeEventsIterator(startTime, endTime, 1);
        while(iterator.hasNext()) {
            ITimeEvent event = iterator.next();
            if ((event.getTime() <= endTime && (event.getTime() + event.getDuration() >= startTime))) {
                if (event instanceof TimeEvent) {
                    TimeEvent timeEvent = (TimeEvent) event;
                    int value = timeEvent.getValue();
                    if (value != StateValues.PROCESS_STATUS_WAIT_BLOCKED) {
                        return true;
                    }
                }
            }

        }
        return false;
    }
}
