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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.StateValues;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.ui.views.barchart.BarChartEvent;
import org.eclipse.linuxtools.tmf.ui.views.barchart.BarChartPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.views.barchart.IEnumState;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.ITimeGraphDrawingHelper;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.Resolution;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Presentation provider for the Resource view, based on the generic TMF
 * presentation provider.
 *
 * @author Patrick Tasse
 */
public class ResourcesPresentationProvider extends BarChartPresentationProvider {

    private long fLastThreadId = -1;

    private enum State implements IEnumState {
        IDLE            (new RGB(200, 200, 200)),
        USERMODE        (new RGB(0, 200, 0)),
        SYSCALL         (new RGB(0, 0, 200)),
        IRQ             (new RGB(200,   0, 100)),
        SOFT_IRQ        (new RGB(200, 150, 100)),
        IRQ_ACTIVE      (new RGB(200,   0, 100)),
        SOFT_IRQ_RAISED (new RGB(200, 200, 0)),
        SOFT_IRQ_ACTIVE (new RGB(200, 150, 100));

        public final RGB rgb;

        private State (RGB rgb) {
            this.rgb = rgb;
        }

        @Override
        public RGB rgb() {
            return rgb;
        }
    }

    /**
     * Default constructor
     */
    public ResourcesPresentationProvider() {
        super();
    }

    @Override
    public String getStateTypeName() {
        return Messages.ResourcesView_stateTypeName;
    }

    @Override
    protected IEnumState[] getStateValues() {
        return State.values();
    }

    @Override
    protected IEnumState getMatchingState(int status) {
        switch (status) {
        case StateValues.CPU_STATUS_IDLE:
            return State.IDLE;
        case StateValues.CPU_STATUS_RUN_USERMODE:
            return State.USERMODE;
        case StateValues.CPU_STATUS_RUN_SYSCALL:
            return State.SYSCALL;
        case StateValues.CPU_STATUS_IRQ:
            return State.IRQ;
        case StateValues.CPU_STATUS_SOFTIRQ:
            return State.SOFT_IRQ;
        case StateValues.SOFT_IRQ_RAISED:
            return State.SOFT_IRQ_RAISED;
        default:
            return State.USERMODE;
        }
    }

    private static IEnumState getEventState(ITimeEvent event) {
        if (event instanceof BarChartEvent) {
            BarChartEvent tcEvent = (BarChartEvent) event;

            ResourcesEntry entry = (ResourcesEntry)event.getEntry();
            int value = tcEvent.getValue();

            if (entry instanceof ResourcesEntryCpu) {
                if (value == StateValues.CPU_STATUS_IDLE) {
                    return State.IDLE;
                } else if (value == StateValues.CPU_STATUS_RUN_USERMODE) {
                    return State.USERMODE;
                } else if (value == StateValues.CPU_STATUS_RUN_SYSCALL) {
                    return State.SYSCALL;
                } else if (value == StateValues.CPU_STATUS_IRQ) {
                    return State.IRQ;
                } else if (value == StateValues.CPU_STATUS_SOFTIRQ) {
                    return State.SOFT_IRQ;
                }
            } else if (entry instanceof ResourcesEntryIrq && value != BarChartEvent.NOVALUE) {
                return State.IRQ_ACTIVE;
            } else if (entry instanceof ResourcesEntrySoftirq && value != BarChartEvent.NOVALUE) {
                if (value == StateValues.SOFT_IRQ_RAISED) {
                    return State.SOFT_IRQ_RAISED;
                }
                return State.SOFT_IRQ_ACTIVE;
            }
        }
        return null;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        IEnumState state = getEventState(event);
        if (state != null) {
            return state.ordinal();
        }
        if (event instanceof BarChartEvent) {
            return INVISIBLE;
        }
        return TRANSPARENT;
    }

    @Override
    public String getEventName(ITimeEvent event) {
        IEnumState state = getEventState(event);
        if (state != null) {
            return state.toString();
        }
        return null;
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {

        Map<String, String> retMap = new LinkedHashMap<String, String>();
        if (event instanceof BarChartEvent) {

            BarChartEvent tcEvent = (BarChartEvent) event;
            ResourcesEntry entry = (ResourcesEntry)event.getEntry();

            if (tcEvent.hasValue()) {
                // Check for IRQ or Soft_IRQ type
                if (entry instanceof ResourcesEntryIrq ||entry instanceof ResourcesEntrySoftirq) {

                    // Get CPU of IRQ or SoftIRQ and provide it for the tooltip display
                    int cpu = tcEvent.getValue();
                    if (cpu >= 0) {
                        retMap.put(Messages.ResourcesView_attributeCpuName, String.valueOf(cpu));
                    }
                }

                // Check for type CPU
                else if (entry instanceof ResourcesEntryCpu) {
                    int status = tcEvent.getValue();

                    if (status == StateValues.CPU_STATUS_IRQ) {
                        // In IRQ state get the IRQ that caused the interruption
                        ITmfStateSystem ss = entry.getTrace().getStateSystems().get(LttngKernelTrace.STATE_ID);
                        int cpu = entry.getId();

                        try {
                            List<ITmfStateInterval> fullState = ss.queryFullState(event.getTime());
                            List<Integer> irqQuarks = ss.getQuarks(Attributes.RESOURCES, Attributes.IRQS, "*"); //$NON-NLS-1$

                            for (int irqQuark : irqQuarks) {
                                if (fullState.get(irqQuark).getStateValue().unboxInt() == cpu) {
                                    ITmfStateInterval value = ss.querySingleState(event.getTime(), irqQuark);
                                    if (!value.getStateValue().isNull()) {
                                        int irq = Integer.parseInt(ss.getAttributeName(irqQuark));
                                        retMap.put(Messages.ResourcesView_attributeIrqName, String.valueOf(irq));
                                    }
                                    break;
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
                    } else if (status == StateValues.CPU_STATUS_SOFTIRQ) {
                        // In SOFT_IRQ state get the SOFT_IRQ that caused the interruption
                        ITmfStateSystem ss = entry.getTrace().getStateSystems().get(LttngKernelTrace.STATE_ID);
                        int cpu = entry.getId();

                        try {
                            List<ITmfStateInterval> fullState = ss.queryFullState(event.getTime());
                            List<Integer> softIrqQuarks = ss.getQuarks(Attributes.RESOURCES, Attributes.SOFT_IRQS, "*"); //$NON-NLS-1$

                            for (int softIrqQuark : softIrqQuarks) {
                                if (fullState.get(softIrqQuark).getStateValue().unboxInt() == cpu) {
                                    ITmfStateInterval value = ss.querySingleState(event.getTime(), softIrqQuark);
                                    if (!value.getStateValue().isNull()) {
                                        int softIrq = Integer.parseInt(ss.getAttributeName(softIrqQuark));
                                        retMap.put(Messages.ResourcesView_attributeSoftIrqName, String.valueOf(softIrq));
                                    }
                                    break;
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
                    } else if (status == StateValues.CPU_STATUS_RUN_USERMODE || status == StateValues.CPU_STATUS_RUN_SYSCALL){
                        // In running state get the current tid
                        ITmfStateSystem ssq = entry.getTrace().getStateSystems().get(LttngKernelTrace.STATE_ID);

                        try {
                            retMap.put(Messages.ResourcesView_attributeHoverTime, Utils.formatTime(hoverTime, TimeFormat.CALENDAR, Resolution.NANOSEC));
                            int cpuQuark = entry.getQuark();
                            int currentThreadQuark = ssq.getQuarkRelative(cpuQuark, Attributes.CURRENT_THREAD);
                            ITmfStateInterval interval = ssq.querySingleState(hoverTime, currentThreadQuark);
                            if (!interval.getStateValue().isNull()) {
                                ITmfStateValue value = interval.getStateValue();
                                int currentThreadId = value.unboxInt();
                                retMap.put(Messages.ResourcesView_attributeTidName, Integer.toString(currentThreadId));
                                int execNameQuark = ssq.getQuarkAbsolute(Attributes.THREADS, Integer.toString(currentThreadId), Attributes.EXEC_NAME);
                                interval = ssq.querySingleState(hoverTime, execNameQuark);
                                if (!interval.getStateValue().isNull()) {
                                    value = interval.getStateValue();
                                    retMap.put(Messages.ResourcesView_attributeProcessName, value.unboxStr());
                                }
                                if (status == StateValues.CPU_STATUS_RUN_SYSCALL) {
                                    int syscallQuark = ssq.getQuarkAbsolute(Attributes.THREADS, Integer.toString(currentThreadId), Attributes.SYSTEM_CALL);
                                    interval = ssq.querySingleState(hoverTime, syscallQuark);
                                    if (!interval.getStateValue().isNull()) {
                                        value = interval.getStateValue();
                                        retMap.put(Messages.ResourcesView_attributeSyscallName, value.unboxStr());
                                    }
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
                }
            }
        }

        return retMap;
    }

    @Override
    public void postDrawEvent(ITimeGraphDrawingHelper drawingHelper, ITimeEvent event, Rectangle bounds, GC gc) {
        if (bounds.width <= gc.getFontMetrics().getAverageCharWidth()) {
            return;
        }

        if (!(event instanceof BarChartEvent)) {
            return;
        }
        BarChartEvent tcEvent = (BarChartEvent) event;
        if (!tcEvent.hasValue()) {
            return;
        }

        ResourcesEntry entry = (ResourcesEntry)event.getEntry();
        if (!(entry instanceof ResourcesEntryCpu)) {
            return;
        }

        int status = tcEvent.getValue();
        if (status != StateValues.CPU_STATUS_RUN_USERMODE && status != StateValues.CPU_STATUS_RUN_SYSCALL) {
            return;
        }

        ITmfStateSystem ss = entry.getTrace().getStateSystems().get(LttngKernelTrace.STATE_ID);
        long time = event.getTime();
        try {
            while (time < event.getTime() + event.getDuration()) {
                int cpuQuark = entry.getQuark();
                int currentThreadQuark = ss.getQuarkRelative(cpuQuark, Attributes.CURRENT_THREAD);
                ITmfStateInterval tidInterval = ss.querySingleState(time, currentThreadQuark);
                if (!tidInterval.getStateValue().isNull()) {
                    ITmfStateValue value = tidInterval.getStateValue();
                    int currentThreadId = value.unboxInt();
                    if (status == StateValues.CPU_STATUS_RUN_USERMODE && currentThreadId != fLastThreadId) {
                        int execNameQuark = ss.getQuarkAbsolute(Attributes.THREADS, Integer.toString(currentThreadId), Attributes.EXEC_NAME);
                        ITmfStateInterval interval = ss.querySingleState(time, execNameQuark);
                        if (!interval.getStateValue().isNull()) {
                            value = interval.getStateValue();
                            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
                            long startTime = Math.max(tidInterval.getStartTime(), event.getTime());
                            long endTime = Math.min(tidInterval.getEndTime() + 1, event.getTime() + event.getDuration());
                            if (drawingHelper.getXForTime(endTime) > bounds.x) {
                                int x = Math.max(drawingHelper.getXForTime(startTime), bounds.x);
                                int width = Math.min(drawingHelper.getXForTime(endTime), bounds.x + bounds.width) - x;
                                int drawn = Utils.drawText(gc, value.unboxStr(), x + 1, bounds.y - 2, width - 1, true, true);
                                if (drawn > 0) {
                                    fLastThreadId = currentThreadId;
                                }
                            }
                        }
                    } else if (status == StateValues.CPU_STATUS_RUN_SYSCALL) {
                        int syscallQuark = ss.getQuarkAbsolute(Attributes.THREADS, Integer.toString(currentThreadId), Attributes.SYSTEM_CALL);
                        ITmfStateInterval interval = ss.querySingleState(time, syscallQuark);
                        if (!interval.getStateValue().isNull()) {
                            value = interval.getStateValue();
                            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
                            long startTime = Math.max(tidInterval.getStartTime(), event.getTime());
                            long endTime = Math.min(tidInterval.getEndTime() + 1, event.getTime() + event.getDuration());
                            if (drawingHelper.getXForTime(endTime) > bounds.x) {
                                int x = Math.max(drawingHelper.getXForTime(startTime), bounds.x);
                                int width = Math.min(drawingHelper.getXForTime(endTime), bounds.x + bounds.width) - x;
                                Utils.drawText(gc, value.unboxStr().substring(4), x + 1, bounds.y - 2, width - 1, true, true);
                            }
                        }
                    }
                }
                time = tidInterval.getEndTime() + 1;
                if (time < event.getTime() + event.getDuration()) {
                    int x = drawingHelper.getXForTime(time);
                    if (x >= bounds.x) {
                        gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_GRAY));
                        gc.drawLine(x, bounds.y + 1, x, bounds.y + bounds.height - 2);
                    }
                }
            }
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (TimeRangeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postDrawEntry(ITimeGraphEntry entry, Rectangle bounds, GC gc) {
        fLastThreadId = -1;
    }
}
