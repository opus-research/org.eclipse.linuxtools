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
 * @author ekadkou
 *
 */
public class CumulCpuUsageProvider extends AbstractTmfStateProvider {

    private int threadNode = -1;
    Map<Integer, Pair<Integer, Long>> pidExecTime = new HashMap<Integer, Pair<Integer, Long>>();
    Map<Integer, Long> cumulUsage = new HashMap<Integer, Long>();
    private final static long RUN_MODE = -3L;

    /**
     * @param trace
     *            the trace
     */
    public CumulCpuUsageProvider(CtfTmfTrace trace) {
        super(trace, CtfTmfEvent.class, "LTTng Kernel CPU Usage"); //$NON-NLS-1$

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

            Integer prevTid = ((Long) content.getField(LttngStrings.PREV_TID).getValue()).intValue();
            Integer nextTid = ((Long) content.getField(LttngStrings.NEXT_TID).getValue()).intValue();

            Integer formerThreadExectimeNode = ss.getQuarkRelativeAndAdd(threadNode, prevTid.toString(), Attributes.EXEC_TIME);
            Integer newCurrentThreadExectimeNode = ss.getQuarkRelativeAndAdd(threadNode, nextTid.toString(), Attributes.EXEC_TIME);

            try {
                if (prevTid == 0 || nextTid == 0) {
                    new Object();
                }
                long prevTidUsage = 0;
                long nextTidUsage = 0;
                /* update previous TID */
                if (pidExecTime.containsKey(prevTid)) {
                    prevTidUsage = cumulUsage.get(prevTid).longValue();
                    if (pidExecTime.get(prevTid).getFirst() == StateValues.PROCESS_STATUS_RUN_USERMODE) {
                        long delta = 0;
                        delta = ev.getTimestamp().getValue() - pidExecTime.get(prevTid).getSecond().longValue() - 1;
                        prevTidUsage += delta;
                        ss.updateOngoingState(TmfStateValue.newValueLong(prevTidUsage), formerThreadExectimeNode);
                    }
                }
                pidExecTime.put(prevTid, new Pair<Integer, Long>(StateValues.PROCESS_STATUS_WAIT_FOR_CPU, ev.getTimestamp().getValue()));
                cumulUsage.put(prevTid, prevTidUsage);

                /* update next TID */
                if (pidExecTime.containsKey(nextTid)) {
                    nextTidUsage = cumulUsage.get(nextTid).longValue();
                    if (pidExecTime.get(nextTid).getFirst() == StateValues.PROCESS_STATUS_WAIT_FOR_CPU) {
                        ss.updateOngoingState(TmfStateValue.newValueLong(nextTidUsage), newCurrentThreadExectimeNode);
                    } else if (pidExecTime.get(nextTid).getFirst() == StateValues.PROCESS_STATUS_RUN_USERMODE) {
                        long delta = 0;
                        delta = ev.getTimestamp().getValue() - pidExecTime.get(nextTid).getSecond().longValue() - 1;
                        nextTidUsage += delta;
                        ss.updateOngoingState(TmfStateValue.newValueLong(nextTidUsage), newCurrentThreadExectimeNode);
                    }
                }
                pidExecTime.put(nextTid, new Pair<Integer, Long>(StateValues.PROCESS_STATUS_RUN_USERMODE, ev.getTimestamp().getValue()));
                cumulUsage.put(nextTid, nextTidUsage);
                ss.modifyAttribute(ts, TmfStateValue.newValueLong(RUN_MODE), newCurrentThreadExectimeNode);
            } catch (TimeRangeException e) {
            } catch (AttributeNotFoundException e) {
            } catch (StateValueTypeException e) {
            }
        }
    }

    @Override
    public void dispose() {
        closeCumul();
        super.dispose();

    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new LTTngCpuUsageStateProvider((CtfTmfTrace) this.getTrace());
    }

    private void closeCumul() {
        waitForEmptyQueue();
        try {
            for (Map.Entry<Integer, Pair<Integer, Long>> entry : pidExecTime.entrySet()) {
                TmfStateValue value;
                long delta = 0;
                if (entry.getValue().getFirst() == StateValues.PROCESS_STATUS_RUN_USERMODE) {
                    delta = ss.getCurrentEndTime() - entry.getValue().getSecond().longValue();
                }
                value = TmfStateValue.newValueLong(cumulUsage.get(entry.getKey()) + delta);
                Integer entryNode = ss.getQuarkRelative(threadNode, entry.getKey().toString(), Attributes.EXEC_TIME);
                ss.updateOngoingState(value, entryNode);
            }
        } catch (AttributeNotFoundException e) {
        }
    }

    @Override
    public int getVersion() {
        return 0;
    }
}
