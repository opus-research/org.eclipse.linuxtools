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

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import java.util.ArrayList;
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
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.barchart.AbstractBarChartView;
import org.eclipse.linuxtools.tmf.ui.views.barchart.BarChartEntry;
import org.eclipse.linuxtools.tmf.ui.views.barchart.BarChartEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Main implementation for the LTTng 2.0 kernel Resource view
 *
 * @author Patrick Tasse
 */
public class ResourcesView extends AbstractBarChartView {

    /** View ID. */
    public static final String RESID = "org.eclipse.linuxtools.lttng2.kernel.ui.views.resources"; //$NON-NLS-1$

    private static final String PROCESS_COLUMN = Messages.ControlFlowView_processColumn;

    private final static String[] COLUMN_NAMES = new String[] {
            PROCESS_COLUMN

    };

    private final static String[] FILTER_COLUMN_NAMES = new String[] {

            };

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public ResourcesView() {
        super(RESID, COLUMN_NAMES, FILTER_COLUMN_NAMES, new ResourcesPresentationProvider(), 15);
        fNextText = Messages.ResourcesView_nextResourceActionNameText;
        fNextTooltip = Messages.ResourcesView_nextResourceActionToolTipText;
        fPrevText = Messages.ResourcesView_previousResourceActionNameText;
        fPrevTooltip = Messages.ResourcesView_previousResourceActionToolTipText;
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected void buildEventList(ITmfTrace trace, IProgressMonitor monitor) {
        fStartTime = Long.MAX_VALUE;
        fEndTime = Long.MIN_VALUE;
        ArrayList<ResourcesEntry> entryList = new ArrayList<ResourcesEntry>();
        for (ITmfTrace aTrace : fTraceManager.getActiveTraceSet()) {
            if (monitor.isCanceled()) {
                return;
            }
            if (aTrace instanceof LttngKernelTrace) {
                LttngKernelTrace lttngKernelTrace = (LttngKernelTrace) aTrace;
                ITmfStateSystem ssq = lttngKernelTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
                if (!ssq.waitUntilBuilt()) {
                    return;
                }
                long startTime = ssq.getStartTime();
                long endTime = ssq.getCurrentEndTime() + 1;
                ResourcesEntry groupEntry = new ResourcesEntryNull(lttngKernelTrace, aTrace.getName(), startTime, endTime, 0);
                entryList.add(groupEntry);
                fStartTime = Math.min(fStartTime, startTime);
                fEndTime = Math.max(fEndTime, endTime);
                List<Integer> cpuQuarks = ssq.getQuarks(Attributes.CPUS, "*"); //$NON-NLS-1$
                ResourcesEntry[] cpuEntries = new ResourcesEntry[cpuQuarks.size()];
                for (int i = 0; i < cpuQuarks.size(); i++) {
                    int cpuQuark = cpuQuarks.get(i);
                    int cpu = Integer.parseInt(ssq.getAttributeName(cpuQuark));
                    ResourcesEntry entry = new ResourcesEntryCpu(cpuQuark, lttngKernelTrace, fStartTime, fEndTime, cpu);
                    groupEntry.addChild(entry);
                    cpuEntries[i] = entry;
                }
                List<Integer> irqQuarks = ssq.getQuarks(Attributes.RESOURCES, Attributes.IRQS, "*"); //$NON-NLS-1$
                ResourcesEntry[] irqEntries = new ResourcesEntry[irqQuarks.size()];
                for (int i = 0; i < irqQuarks.size(); i++) {
                    int irqQuark = irqQuarks.get(i);
                    int irq = Integer.parseInt(ssq.getAttributeName(irqQuark));
                    ResourcesEntry entry = new ResourcesEntryIrq(irqQuark, lttngKernelTrace, fStartTime, fEndTime, irq);
                    groupEntry.addChild(entry);
                    irqEntries[i] = entry;
                }
                List<Integer> softIrqQuarks = ssq.getQuarks(Attributes.RESOURCES, Attributes.SOFT_IRQS, "*"); //$NON-NLS-1$
                ResourcesEntry[] softIrqEntries = new ResourcesEntry[softIrqQuarks.size()];
                for (int i = 0; i < softIrqQuarks.size(); i++) {
                    int softIrqQuark = softIrqQuarks.get(i);
                    int softIrq = Integer.parseInt(ssq.getAttributeName(softIrqQuark));
                    ResourcesEntry entry = new ResourcesEntrySoftirq(softIrqQuark, lttngKernelTrace, fStartTime, fEndTime, softIrq);
                    groupEntry.addChild(entry);
                    softIrqEntries[i] = entry;
                }
            }
        }
        synchronized (fEntryListMap) {
            fEntryListMap.put(trace, (ArrayList<BarChartEntry>) entryList.clone());
        }
        if (trace == fTrace) {
            refresh();
        }
        for (ResourcesEntry traceEntry : entryList) {
            if (monitor.isCanceled()) {
                return;
            }
            LttngKernelTrace lttngKernelTrace = (LttngKernelTrace) traceEntry.getTrace();
            ITmfStateSystem ssq = lttngKernelTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
            long startTime = ssq.getStartTime();
            long endTime = ssq.getCurrentEndTime() + 1;
            long resolution = (endTime - startTime) / fDisplayWidth;
            for (BarChartEntry entry : traceEntry.getChildren()) {
                List<ITimeEvent> eventList = getEventList(entry, startTime, endTime, resolution, monitor);
                entry.setEventList(eventList);
                redraw();
            }
        }
    }

    @Override
    protected List<ITimeEvent> getEventList(BarChartEntry entry,
            long startTime, long endTime, long resolution,
            IProgressMonitor monitor) {
        ITmfStateSystem ssq = entry.getTrace().getStateSystems().get(LttngKernelTrace.STATE_ID);
        final long realStart = Math.max(startTime, ssq.getStartTime());
        final long realEnd = Math.min(endTime, ssq.getCurrentEndTime() + 1);
        if (realEnd <= realStart) {
            return null;
        }
        List<ITimeEvent> eventList = null;
        int quark = entry.getQuark();
        try {
            if (entry instanceof ResourcesEntryCpu) {
                int statusQuark = ssq.getQuarkRelative(quark, Attributes.STATUS);
                List<ITmfStateInterval> statusIntervals = ssq.queryHistoryRange(statusQuark, realStart, realEnd - 1, resolution, monitor);
                eventList = new ArrayList<ITimeEvent>(statusIntervals.size());
                long lastEndTime = -1;
                for (ITmfStateInterval statusInterval : statusIntervals) {
                    if (monitor.isCanceled()) {
                        return null;
                    }
                    int status = statusInterval.getStateValue().unboxInt();
                    long time = statusInterval.getStartTime();
                    long duration = statusInterval.getEndTime() - time + 1;
                    if (!statusInterval.getStateValue().isNull()) {
                        if (lastEndTime != time && lastEndTime != -1) {
                            eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime));
                        }
                        eventList.add(new BarChartEvent(entry, time, duration, status));
                        lastEndTime = time + duration;
                    } else {
                        if (true) {// includeNull) {
                            eventList.add(new BarChartEvent(entry, time, duration));
                        }
                    }
                }
            } else if (entry instanceof ResourcesEntryIrq) {
                List<ITmfStateInterval> irqIntervals = ssq.queryHistoryRange(quark, realStart, realEnd - 1, resolution, monitor);
                eventList = new ArrayList<ITimeEvent>(irqIntervals.size());
                long lastEndTime = -1;
                boolean lastIsNull = true;
                for (ITmfStateInterval irqInterval : irqIntervals) {
                    if (monitor.isCanceled()) {
                        return null;
                    }
                    long time = irqInterval.getStartTime();
                    long duration = irqInterval.getEndTime() - time + 1;
                    if (!irqInterval.getStateValue().isNull()) {
                        int cpu = irqInterval.getStateValue().unboxInt();
                        eventList.add(new BarChartEvent(entry, time, duration, cpu));
                        lastIsNull = false;
                    } else {
                        if (lastEndTime != time && lastEndTime != -1 && lastIsNull) {
                            eventList.add(new BarChartEvent(entry, lastEndTime, time - lastEndTime, -1));
                        }
                        if (true) { // includeNull
                            eventList.add(new BarChartEvent(entry, time, duration));
                        }
                        lastIsNull = true;
                    }
                    lastEndTime = time + duration;
                }
            } else if (entry instanceof ResourcesEntrySoftirq) {
                List<ITmfStateInterval> softIrqIntervals = ssq.queryHistoryRange(quark, realStart, realEnd - 1, resolution, monitor);
                eventList = new ArrayList<ITimeEvent>(softIrqIntervals.size());
                long lastEndTime = -1;
                boolean lastIsNull = true;
                for (ITmfStateInterval softIrqInterval : softIrqIntervals) {
                    if (monitor.isCanceled()) {
                        return null;
                    }
                    long time = softIrqInterval.getStartTime();
                    long duration = softIrqInterval.getEndTime() - time + 1;
                    if (!softIrqInterval.getStateValue().isNull()) {
                        int cpu = softIrqInterval.getStateValue().unboxInt();
                        eventList.add(new BarChartEvent(entry, time, duration, cpu));
                    } else {
                        if (lastEndTime != time && lastEndTime != -1 && lastIsNull) {
                            eventList.add(new BarChartEvent(entry, lastEndTime, time - lastEndTime, -1));
                        }
                        if (true) { // includeNull) {
                            eventList.add(new BarChartEvent(entry, time, duration));
                        }
                        lastIsNull = true;
                    }
                    lastEndTime = time + duration;
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
        return eventList;
    }

    @Override
    protected int getSelectedThread(long time) {
        // TODO Auto-generated method stub
        return 0;
    }
}
