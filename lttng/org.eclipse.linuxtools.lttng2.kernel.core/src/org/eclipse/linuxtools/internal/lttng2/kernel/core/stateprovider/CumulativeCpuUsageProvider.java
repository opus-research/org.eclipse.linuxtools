/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *Contributors:
 *     Jean-Christian Kouamé - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.Activator;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.LttngStrings;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.StateValues;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.util.Pair;

/**
 * A state provider for the cpu usage view. It stores the cumulative cpu usage per pid.
 *
 * @author Jean Christian Kouamé
 *
 */
public class CumulativeCpuUsageProvider extends AbstractTmfStateProvider {

    private static final long RUN_MODE = -3L;
    private final Map<Integer, Pair<Integer, Long>> pidExecTime = new HashMap<Integer, Pair<Integer, Long>>();
    private final Map<Integer, Long> cumulUsage = new HashMap<Integer, Long>();
    private int threadNode = -1;

    /**
     * Constructor
     *
     * @param trace
     *            the trace
     */
    public CumulativeCpuUsageProvider(CtfTmfTrace trace) {
        super(trace, CtfTmfEvent.class, Messages.CumulativeCpuUsageProvider_CpuUsage);

    }

    @Override
    public void assignTargetStateSystem(org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder ssb) {
        super.assignTargetStateSystem(ssb);
        threadNode = ss.getQuarkAbsoluteAndAdd(Attributes.THREADS);
    }

    @Override
    protected void eventHandle(ITmfEvent ev) {
        CtfTmfEvent event = (CtfTmfEvent) ev;
        if (event.getEventName().equals(LttngStrings.SCHED_SWITCH)) {
            final ITmfEventField content = event.getContent();

            final long ts = event.getTimestamp().getValue();

            int prevTid = ((Long) content.getField(LttngStrings.PREV_TID).getValue()).intValue();
            int nextTid = ((Long) content.getField(LttngStrings.NEXT_TID).getValue()).intValue();

            int formerThreadExectimeNode = ss.getQuarkRelativeAndAdd(threadNode,
                    Integer.toString(prevTid),
                    Attributes.EXEC_TIME);
            int newCurrentThreadExectimeNode = ss.getQuarkRelativeAndAdd(threadNode,
                    Integer.toString(nextTid),
                    Attributes.EXEC_TIME);

            try {
                long prevTidUsage = 0;
                long nextTidUsage = 0;
                /* update previous TID */
                final long nanoTime = ev.getTimestamp().normalize(0, (byte) -9).getValue();
                if (pidExecTime.containsKey(prevTid)) {
                    prevTidUsage = cumulUsage.get(prevTid).longValue();
                    if (pidExecTime.get(prevTid).getFirst().equals(StateValues.PROCESS_STATUS_RUN_USERMODE)) {
                        long delta = nanoTime - pidExecTime.get(prevTid).getSecond().longValue() - 1;
                        prevTidUsage += delta;
                        ss.updateOngoingState(TmfStateValue.newValueLong(prevTidUsage), formerThreadExectimeNode);
                    }
                }
                pidExecTime.put(prevTid, new Pair<Integer, Long>(StateValues.PROCESS_STATUS_WAIT_FOR_CPU, nanoTime));
                cumulUsage.put(prevTid, prevTidUsage);

                /* update next TID */
                if (pidExecTime.containsKey(nextTid)) {
                    nextTidUsage = cumulUsage.get(nextTid).longValue();
                    if (pidExecTime.get(nextTid).getFirst() == StateValues.PROCESS_STATUS_WAIT_FOR_CPU) {
                        ss.updateOngoingState(TmfStateValue.newValueLong(nextTidUsage), newCurrentThreadExectimeNode);
                    } else if (pidExecTime.get(nextTid).getFirst() == StateValues.PROCESS_STATUS_RUN_USERMODE) {
                        long delta = 0;
                        delta = nanoTime - pidExecTime.get(nextTid).getSecond().longValue() - 1;
                        nextTidUsage += delta;
                        ss.updateOngoingState(TmfStateValue.newValueLong(nextTidUsage), newCurrentThreadExectimeNode);
                    }
                }
                pidExecTime.put(nextTid, new Pair<Integer, Long>(StateValues.PROCESS_STATUS_RUN_USERMODE, nanoTime));
                cumulUsage.put(nextTid, nextTidUsage);
                /*
                 * HACK : Create a new interval with a fake value in order to
                 * close the previous one at the right timestamp.
                 */
                ss.modifyAttribute(ts, TmfStateValue.newValueLong(RUN_MODE), newCurrentThreadExectimeNode);
            } catch (TimeRangeException e) {
                Activator.getDefault().logError(Messages.CumulativeCpuUsageProvider_TimeRangeExceptionMessage, e);
            } catch (AttributeNotFoundException e) {
                Activator.getDefault().logError(Messages.CumulativeCpuUsageProvider_AttributeNotFoundMessage, e);
            } catch (StateValueTypeException e) {
                Activator.getDefault().logError(Messages.CumulativeCpuUsageProvider_StateValueTypeMessage, e);
            }
        }
    }

    @Override
    public void dispose() {
        waitForEmptyQueue();
        try {
            for (Map.Entry<Integer, Pair<Integer, Long>> entry : pidExecTime.entrySet()) {
                TmfStateValue value;
                long delta = 0;
                if (entry.getValue().getFirst() == StateValues.PROCESS_STATUS_RUN_USERMODE) {
                    delta = ss.getCurrentEndTime() - entry.getValue().getSecond().longValue();
                }
                value = TmfStateValue.newValueLong(cumulUsage.get(entry.getKey()) + delta);
                int entryNode = ss.getQuarkRelative(threadNode, entry.getKey().toString(), Attributes.EXEC_TIME);
                ss.updateOngoingState(value, entryNode);
            }
        } catch (AttributeNotFoundException e) {
            Activator.getDefault().logError(Messages.CumulativeCpuUsageProvider_AttributeNotFoundMessage, e);
        }
        super.dispose();

    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new CumulativeCpuUsageProvider((CtfTmfTrace) this.getTrace());
    }

    @Override
    public int getVersion() {
        return 0;
    }
}
