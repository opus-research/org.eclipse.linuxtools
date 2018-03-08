/*******************************************************************************
 * Copyright (c) 2011-2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.trace;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;

/**
 * A kernel event type. This is an {@link CtfTmfEvent} with a getPid function.
 * @author Matthew Khouzam
 * @since 2.0
 */
final public class CtfKernelEvent extends CtfTmfEvent {

    /**
     * Constructor
     * @param eventDef the event definition from CTF
     * @param fileName the file that the event came from
     * @param originTrace the trace that the event came from
     */
    public CtfKernelEvent(EventDefinition eventDef, String fileName,
            CtfKernelTrace originTrace) {
        super(eventDef, fileName, originTrace);
    }

    /**
     * Copy constructor
     * @param ctfKernelEvent item to copy
     */
    public CtfKernelEvent(CtfKernelEvent ctfKernelEvent) {
        super(ctfKernelEvent);
    }

    @Override
    public String getSource() {
        return Integer.toString(getPid());
    }

    /**
     * Get the pid of the process that generated this event. This function is
     * slow and should not be called on the fast path.
     *
     * @return the pid if found or -1 if it is not found
     * @since 2.0
     */
    public int getPid() {
        // check if the pid is in the context
        ITmfEventField pidContext = this.getContent().getField("context._pid"); //$NON-NLS-1$
        if (null != pidContext) {
            return ((Integer) pidContext.getValue()).intValue();
        }
        // fall back on the state system
        CtfKernelTrace trace = (CtfKernelTrace) this.getTrace();
        ITmfStateSystem ss = trace.getStateSystem(CtfKernelTrace.STATE_ID);
        int retVal = -1;
        try {
            int threadNode = ss.getQuarkAbsolute(Attributes.CPUS, String.valueOf(getCPU()), Attributes.CURRENT_THREAD);
            ITmfStateValue sv = ss.querySingleState(this.getTimestamp().getValue(), threadNode).getStateValue();
            // nullValue == -1 == idle should be zero.
            retVal = Math.max(0, sv.unboxInt());

        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (TimeRangeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    @Override
    public CtfKernelEvent clone() {
        return new CtfKernelEvent(this);
    }
}
