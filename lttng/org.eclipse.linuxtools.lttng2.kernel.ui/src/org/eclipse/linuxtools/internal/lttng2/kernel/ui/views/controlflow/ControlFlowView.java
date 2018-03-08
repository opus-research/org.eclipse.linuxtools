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
 *   Geneviève Bastien - Move code to provide base classes for bar charts
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.controlflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.barchart.AbstractBarChartView;
import org.eclipse.linuxtools.tmf.ui.views.barchart.BarChartEntry;
import org.eclipse.linuxtools.tmf.ui.views.barchart.BarChartEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * The Control Flow view main object
 *
 */
public class ControlFlowView extends AbstractBarChartView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * View ID.
     */
    public static final String CFVID = "org.eclipse.linuxtools.lttng2.kernel.ui.views.controlflow"; //$NON-NLS-1$

    private static final String PROCESS_COLUMN    = Messages.ControlFlowView_processColumn;
    private static final String TID_COLUMN        = Messages.ControlFlowView_tidColumn;
    private static final String PTID_COLUMN       = Messages.ControlFlowView_ptidColumn;
    private static final String BIRTH_TIME_COLUMN = Messages.ControlFlowView_birthTimeColumn;
    private static final String TRACE_COLUMN      = Messages.ControlFlowView_traceColumn;

    private final static String[] COLUMN_NAMES = new String[] {
            PROCESS_COLUMN,
            TID_COLUMN,
            PTID_COLUMN,
            BIRTH_TIME_COLUMN,
            TRACE_COLUMN
    };

    private final static String[] FILTER_COLUMN_NAMES = new String[] {
            PROCESS_COLUMN,
            TID_COLUMN
    };

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public ControlFlowView() {
        super(CFVID, COLUMN_NAMES, FILTER_COLUMN_NAMES, new ControlFlowPresentationProvider(), new ControlFlowEntryComparator() );
        fNextText = Messages.ControlFlowView_nextProcessActionNameText;
        fNextTooltip = Messages.ControlFlowView_nextProcessActionToolTipText;
        fPrevText = Messages.ControlFlowView_previousProcessActionNameText;
        fPrevTooltip = Messages.ControlFlowView_previousProcessActionToolTipText;
    }

    private static class ControlFlowEntryComparator extends BarChartEntryComparator {

        @Override
        public int compare(ITimeGraphEntry o1, ITimeGraphEntry o2) {
            int result = 0;

            result = super.compare(o1, o2);

            if (result == 0) {
                if ((o1 instanceof ControlFlowEntry) && (o2 instanceof ControlFlowEntry)) {
                    ControlFlowEntry entry1 = (ControlFlowEntry) o1;
                    ControlFlowEntry entry2 = (ControlFlowEntry) o2;

                    result = entry1.getThreadId() < entry2.getThreadId() ? -1 : entry1.getThreadId() > entry2.getThreadId() ? 1 : 0;
                }
            }

            return result;
        }
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected void buildEventList(final ITmfTrace trace, IProgressMonitor monitor) {
        fStartTime = Long.MAX_VALUE;
        fEndTime = Long.MIN_VALUE;

        ArrayList<BarChartEntry> rootList = new ArrayList<BarChartEntry>();
        for (ITmfTrace aTrace : fTraceManager.getActiveTraceSet()) {
            if (monitor.isCanceled()) {
                return;
            }
            if (aTrace instanceof LttngKernelTrace) {
                ArrayList<BarChartEntry> entryList = new ArrayList<BarChartEntry>();
                LttngKernelTrace ctfKernelTrace = (LttngKernelTrace) aTrace;
                ITmfStateSystem ssq = ctfKernelTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
                if (!ssq.waitUntilBuilt()) {
                    return;
                }
                long start = ssq.getStartTime();
                long end = ssq.getCurrentEndTime() + 1;
                fStartTime = Math.min(fStartTime, start);
                fEndTime = Math.max(fEndTime, end);
                List<Integer> threadQuarks = ssq.getQuarks(Attributes.THREADS, "*"); //$NON-NLS-1$
                for (int threadQuark : threadQuarks) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    String threadName = ssq.getAttributeName(threadQuark);
                    int threadId = -1;
                    try {
                        threadId = Integer.parseInt(threadName);
                    } catch (NumberFormatException e1) {
                        continue;
                    }
                    if (threadId == 0) { // ignore the swapper thread
                        continue;
                    }
                    int execNameQuark = -1;
                    try {
                        try {
                            execNameQuark = ssq.getQuarkRelative(threadQuark, Attributes.EXEC_NAME);
                        } catch (AttributeNotFoundException e) {
                            continue;
                        }
                        int ppidQuark = ssq.getQuarkRelative(threadQuark, Attributes.PPID);
                        List<ITmfStateInterval> execNameIntervals = ssq.queryHistoryRange(execNameQuark, start, end - 1); // use monitor when available in api
                        if (monitor.isCanceled()) {
                            return;
                        }
                        BarChartEntry entry = null;
                        for (ITmfStateInterval execNameInterval : execNameIntervals) {
                            if (monitor.isCanceled()) {
                                return;
                            }
                            if (!execNameInterval.getStateValue().isNull() &&
                                    execNameInterval.getStateValue().getType() == ITmfStateValue.Type.STRING) {
                                String execName = execNameInterval.getStateValue().unboxStr();
                                long startTime = execNameInterval.getStartTime();
                                long endTime = execNameInterval.getEndTime() + 1;
                                int ppid = -1;
                                if (ppidQuark != -1) {
                                    ITmfStateInterval ppidInterval = ssq.querySingleState(startTime, ppidQuark);
                                    ppid = ppidInterval.getStateValue().unboxInt();
                                }
                                if (entry == null) {
                                    entry = new ControlFlowEntry(threadQuark, ctfKernelTrace, execName, threadId, ppid, startTime, endTime);
                                    entryList.add(entry);
                                } else {
                                    // update the name of the entry to the latest execName
                                    entry.setName(execName);
                                }
                                entry.addEvent(new TimeEvent(entry, startTime, endTime - startTime));
                            } else {
                                entry = null;
                            }
                        }
                    } catch (AttributeNotFoundException e) {
                        e.printStackTrace();
                    } catch (TimeRangeException e) {
                        e.printStackTrace();
                    } catch (StateValueTypeException e) {
                        e.printStackTrace();
                    } catch (StateSystemDisposedException e) {
                        /* Ignored */
                    }
                }
                buildTree(entryList, rootList);
            }
            Collections.sort(rootList, fBarChartEntryComparator);
            synchronized (fEntryListMap) {
                fEntryListMap.put(trace, (ArrayList<BarChartEntry>) rootList.clone());
            }
            if (trace == fTrace) {
                refresh();
            }
        }
        for (BarChartEntry entry : rootList) {
            if (monitor.isCanceled()) {
                return;
            }
            buildStatusEvents(trace, entry, monitor);
        }
    }

    private void buildStatusEvents(ITmfTrace trace, BarChartEntry entry, IProgressMonitor monitor) {
        ITmfStateSystem ssq = entry.getTrace().getStateSystems().get(LttngKernelTrace.STATE_ID);

        long start = ssq.getStartTime();
        long end = ssq.getCurrentEndTime() + 1;
        long resolution = Math.max(1, (end - start) / fDisplayWidth);
        List<ITimeEvent> eventList = getEventList(entry, entry.getStartTime(), entry.getEndTime(), resolution, monitor);
        if (monitor.isCanceled()) {
            return;
        }
        entry.setEventList(eventList);
        if (trace == fTrace) {
            redraw();
        }
        for (ITimeGraphEntry child : entry.getChildren()) {
            if (monitor.isCanceled()) {
                return;
            }
            buildStatusEvents(trace, (BarChartEntry) child, monitor);
        }
    }

    private static void buildTree(ArrayList<BarChartEntry> entryList,
            ArrayList<BarChartEntry> rootList) {
        for (BarChartEntry listentry : entryList) {
            ControlFlowEntry entry = (ControlFlowEntry) listentry;
            boolean root = true;
            if (entry.getParentThreadId() > 0) {
                for (BarChartEntry parententry : entryList) {
                    ControlFlowEntry parent = (ControlFlowEntry) parententry;
                    if (parent.getThreadId() == entry.getParentThreadId() &&
                            entry.getStartTime() >= parent.getStartTime() &&
                            entry.getStartTime() <= parent.getEndTime()) {
                        parent.addChild(entry);
                        root = false;
                        break;
                    }
                }
            }
            if (root) {
                rootList.add(entry);
            }
        }
    }

    @Override
    protected List<ITimeEvent> getEventList(BarChartEntry entry, long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        final long realStart = Math.max(startTime, entry.getStartTime());
        final long realEnd = Math.min(endTime, entry.getEndTime());
        if (realEnd <= realStart) {
            return null;
        }
        ITmfStateSystem ssq = entry.getTrace().getStateSystems().get(LttngKernelTrace.STATE_ID);
        List<ITimeEvent> eventList = null;
        try {
            int statusQuark = ssq.getQuarkRelative(entry.getQuark(), Attributes.STATUS);
            List<ITmfStateInterval> statusIntervals = ssq.queryHistoryRange(statusQuark, realStart, realEnd - 1, resolution, monitor);
            eventList = new ArrayList<ITimeEvent>(statusIntervals.size());
            long lastEndTime = -1;
            for (ITmfStateInterval statusInterval : statusIntervals) {
                if (monitor.isCanceled()) {
                    return null;
                }
                long time = statusInterval.getStartTime();
                long duration = statusInterval.getEndTime() - time + 1;
                int status = -1;
                try {
                    status = statusInterval.getStateValue().unboxInt();
                } catch (StateValueTypeException e) {
                    e.printStackTrace();
                }
                if (lastEndTime != time && lastEndTime != -1) {
                    eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime));
                }
                eventList.add(new BarChartEvent(entry, time, duration, status));
                lastEndTime = time + duration;
            }
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (TimeRangeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
        return eventList;
    }
}
