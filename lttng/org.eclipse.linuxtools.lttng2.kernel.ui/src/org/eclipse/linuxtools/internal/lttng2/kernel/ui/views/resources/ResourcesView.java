/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Alexandre Montplaisir - Convert to extend AbstractTimeGraphView
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources.ResourcesEntry.Type;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.CtfKernelTrace;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemQuerier;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Main implementation for the LTTng 2.0 kernel Resource view
 *
 * @author Patrick Tasse
 */
public class ResourcesView extends AbstractTimeGraphView<ResourcesEntry> {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** View ID. */
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.ui.views.resources"; //$NON-NLS-1$

    /** Initial time range */
    private static final long INITIAL_WINDOW_OFFSET = (1L * 100  * 1000 * 1000); // .1sec

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public ResourcesView() {
        super(ID);
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected void selectExperiment(TmfExperiment experiment) {
        fStartTime = Long.MAX_VALUE;
        fEndTime = Long.MIN_VALUE;
        fSelectedExperiment = experiment;
        ArrayList<TraceEntry> entryList = new ArrayList<TraceEntry>();
        for (ITmfTrace trace : experiment.getTraces()) {
            if (trace instanceof CtfKernelTrace) {
                CtfKernelTrace ctfKernelTrace = (CtfKernelTrace) trace;
                IStateSystemQuerier ssq = ctfKernelTrace.getKernelStateSystem();
                long startTime = ssq.getStartTime();
                long endTime = ssq.getCurrentEndTime() + 1;
                TraceEntry groupEntry = new TraceEntry(ctfKernelTrace, trace.getName(), startTime, endTime);
                entryList.add(groupEntry);
                fStartTime = Math.min(fStartTime, startTime);
                fEndTime = Math.max(fEndTime, endTime);
                List<Integer> cpuQuarks = ssq.getQuarks(Attributes.CPUS, "*"); //$NON-NLS-1$
                ResourcesEntry[] cpuEntries = new ResourcesEntry[cpuQuarks.size()];
                for (int i = 0; i < cpuQuarks.size(); i++) {
                    int cpuQuark = cpuQuarks.get(i);
                    int cpu = Integer.parseInt(ssq.getAttributeName(cpuQuark));
                    ResourcesEntry entry = new ResourcesEntry(cpuQuark, ctfKernelTrace, Type.CPU, cpu);
                    groupEntry.addChild(entry);
                    cpuEntries[i] = entry;
                }
                List<Integer> irqQuarks = ssq.getQuarks(Attributes.RESOURCES, Attributes.IRQS, "*"); //$NON-NLS-1$
                ResourcesEntry[] irqEntries = new ResourcesEntry[irqQuarks.size()];
                for (int i = 0; i < irqQuarks.size(); i++) {
                    int irqQuark = irqQuarks.get(i);
                    int irq = Integer.parseInt(ssq.getAttributeName(irqQuark));
                    ResourcesEntry entry = new ResourcesEntry(irqQuark, ctfKernelTrace, Type.IRQ, irq);
                    groupEntry.addChild(entry);
                    irqEntries[i] = entry;
                }
                List<Integer> softIrqQuarks = ssq.getQuarks(Attributes.RESOURCES, Attributes.SOFT_IRQS, "*"); //$NON-NLS-1$
                ResourcesEntry[] softIrqEntries = new ResourcesEntry[softIrqQuarks.size()];
                for (int i = 0; i < softIrqQuarks.size(); i++) {
                    int softIrqQuark = softIrqQuarks.get(i);
                    int softIrq = Integer.parseInt(ssq.getAttributeName(softIrqQuark));
                    ResourcesEntry entry = new ResourcesEntry(softIrqQuark, ctfKernelTrace, Type.SOFT_IRQ, softIrq);
                    groupEntry.addChild(entry);
                    softIrqEntries[i] = entry;
                }
            }
        }
        synchronized (fEntryListSyncObj) {
            fEntryList = (ArrayList<TraceEntry>) entryList.clone();
        }
        refresh(INITIAL_WINDOW_OFFSET);

        for (TraceEntry traceEntry : entryList) {
            if (traceEntry.getTrace() instanceof CtfKernelTrace) {
                CtfKernelTrace ctfKernelTrace = (CtfKernelTrace) traceEntry.getTrace();
                IStateSystemQuerier ssq = ctfKernelTrace.getKernelStateSystem();
                long startTime = ssq.getStartTime();
                long endTime = ssq.getCurrentEndTime() + 1;
                long resolution = (endTime - startTime) / fDisplayWidth;
                for (ResourcesEntry entry : traceEntry.getChildren()) {
                    List<ITimeEvent> eventList = getEventList(entry, startTime, endTime, resolution, false, new NullProgressMonitor());
                    entry.setEventList(eventList);
                    redraw();
                }
            }
        }
    }

    @Override
    protected List<ITimeEvent> getEventList(ResourcesEntry entry,
            long startTime, long endTime, long resolution, boolean includeNull,
            IProgressMonitor monitor) {
        IStateSystemQuerier ssq = entry.getTrace().getKernelStateSystem();
        startTime = Math.max(startTime, ssq.getStartTime());
        endTime = Math.min(endTime, ssq.getCurrentEndTime() + 1);
        if (endTime <= startTime) {
            return null;
        }
        List<ITimeEvent> eventList = null;
        int quark = entry.getQuark();
        try {
            if (entry.getType().equals(Type.CPU)) {
                int statusQuark = ssq.getQuarkRelative(quark, Attributes.STATUS);
                List<ITmfStateInterval> statusIntervals = ssq.queryHistoryRange(statusQuark, startTime, endTime - 1, resolution, monitor);
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
                        eventList.add(new ResourcesEvent(entry, time, duration, status));
                        lastEndTime = time + duration;
                    } else {
                        if (includeNull) {
                            eventList.add(new ResourcesEvent(entry, time, duration));
                        }
                    }
                }
            } else if (entry.getType().equals(Type.IRQ)) {
                List<ITmfStateInterval> irqIntervals = ssq.queryHistoryRange(quark, startTime, endTime - 1, resolution, monitor);
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
                        eventList.add(new ResourcesEvent(entry, time, duration, cpu));
                        lastIsNull = false;
                    } else {
                        if (lastEndTime != time && lastEndTime != -1 && lastIsNull) {
                            eventList.add(new ResourcesEvent(entry, lastEndTime, time - lastEndTime, -1));
                        }
                        if (includeNull) {
                            eventList.add(new ResourcesEvent(entry, time, duration));
                        }
                        lastIsNull = true;
                    }
                    lastEndTime = time + duration;
                }
            } else if (entry.getType().equals(Type.SOFT_IRQ)) {
                List<ITmfStateInterval> softIrqIntervals = ssq.queryHistoryRange(quark, startTime, endTime - 1, resolution, monitor);
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
                        eventList.add(new ResourcesEvent(entry, time, duration, cpu));
                    } else {
                        if (lastEndTime != time && lastEndTime != -1 && lastIsNull) {
                            eventList.add(new ResourcesEvent(entry, lastEndTime, time - lastEndTime, -1));
                        }
                        if (includeNull) {
                            eventList.add(new ResourcesEvent(entry, time, duration));
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
        }
        return eventList;
    }

    @Override
    protected String viewName() {
        return "Resource"; //$NON-NLS-1$
    }

    @Override
    protected Class<? extends ITmfTrace> getTraceType() {
        return CtfKernelTrace.class;
    }

    @Override
    protected ITmfTimestamp getNewTimestamp(long ts) {
        return new CtfTmfTimestamp(ts);
    }

    @Override
    protected ITimeGraphPresentationProvider getNewPresentationProvider() {
        return new ResourcesPresentationProvider();
    }

}
