/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * Copyright (c) 2012 École Polytechnique de Montréal
 * Copyright (c) 2012 Matthew Khouzam <matthew.khouzam@ericsson.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider;

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
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;

/**
 * @author Matthew Khouzam
 *
 */
public class LTTngCpuUsageStateProvider extends AbstractTmfStateProvider {

    private int threadsNode = -1;
    private int cpusNode = -1;

    /**
     * @param trace
     *            the trace
     */
    public LTTngCpuUsageStateProvider(CtfTmfTrace trace) {
        super(trace, CtfTmfEvent.class, "LTTng Kernel CPU Usage"); //$NON-NLS-1$
    }

    @Override
    public void assignTargetStateSystem(org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder ssb) {
        /* We can only set up the locations once the state system is assigned */
        super.assignTargetStateSystem(ssb);
        threadsNode = ss.getQuarkAbsoluteAndAdd(Attributes.THREADS);
        cpusNode = ss.getQuarkAbsoluteAndAdd(Attributes.CPUS);
    }

    @Override
    protected void eventHandle(ITmfEvent ev) {
        int quark;
        ITmfStateValue value;

        /*
         * AbstractStateChangeInput should have already checked for the correct
         * class type
         */
        CtfTmfEvent event = (CtfTmfEvent) ev;
        if (event.getEventName().equals(LttngStrings.SCHED_SWITCH)) {
            final ITmfEventField content = event.getContent();

            final Integer currentCPUNode = ss.getQuarkRelativeAndAdd(cpusNode, String.valueOf(event.getCPU()));

            final long ts = event.getTimestamp().getValue();

            Integer prevTid = ((Long) content.getField(LttngStrings.PREV_TID).getValue()).intValue();
            String nextProcessName = (String) content.getField(LttngStrings.NEXT_COMM).getValue();
            Integer nextTid = ((Long) content.getField(LttngStrings.NEXT_TID).getValue()).intValue();

            Integer formerThreadNode = ss.getQuarkRelativeAndAdd(threadsNode, prevTid.toString());
            Integer newCurrentThreadNode = ss.getQuarkRelativeAndAdd(threadsNode, nextTid.toString());

            try {

                final ITmfStateValue userMode = TmfStateValue.newValueInt(StateValues.PROCESS_STATUS_RUN_USERMODE);
                final ITmfStateValue waitMode = TmfStateValue.newValueInt(StateValues.PROCESS_STATUS_WAIT_FOR_CPU);

                /* Set the status of the process that got scheduled out. */
                quark = ss.getQuarkRelativeAndAdd(formerThreadNode, Attributes.STATUS);
                ss.modifyAttribute(ts, waitMode, quark);

                /* Set the status of the new scheduled process */
                quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.STATUS);
                ss.modifyAttribute(ts, userMode, quark);

                /* Set the exec name of the new process */
                quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.EXEC_NAME);
                value = TmfStateValue.newValueString(nextProcessName);
                ss.modifyAttribute(ts, value, quark);

                /* Set the current scheduled process on the relevant CPU */
                quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.CURRENT_THREAD);
                value = TmfStateValue.newValueInt(nextTid);
                ss.modifyAttribute(ts, value, quark);

                /* Set the status of the CPU itself */
                if (nextTid > 0) {
                    value = userMode;
                } else {
                    value = TmfStateValue.newValueInt(StateValues.CPU_STATUS_IDLE);
                }
                quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
                ss.modifyAttribute(ts, value, quark);
            } catch (TimeRangeException e) {
                e.printStackTrace();
            } catch (AttributeNotFoundException e) {
                e.printStackTrace();
            } catch (StateValueTypeException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new LTTngCpuUsageStateProvider((CtfTmfTrace) this.getTrace());
    }

    @Override
    public int getVersion() {
        return 0;
    }
}
