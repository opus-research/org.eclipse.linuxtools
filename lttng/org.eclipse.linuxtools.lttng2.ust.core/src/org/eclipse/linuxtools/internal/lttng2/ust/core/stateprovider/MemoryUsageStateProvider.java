/**********************************************************************
 * Copyright (c) 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Geneviève Bastien - Memory is per thread and only total is kept
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ust.core.stateprovider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng2.ust.core.UstStrings;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * State provider to track the memory of the threads using the Ust libc wrapper
 * memory events.
 *
 * @author Matthew Khouzam
 * @author Geneviève Bastien
 */
public class MemoryUsageStateProvider extends AbstractTmfStateProvider {

    private final Map<String, Integer> eventNames = new HashMap<String, Integer>();
    private final Map<Long, Long> fMemory = new HashMap<Long, Long>();

    /**
     * Constructor
     *
     * @param trace
     *            trace
     */
    public MemoryUsageStateProvider(ITmfTrace trace) {
        super(trace, CtfTmfEvent.class, "Ust:Memory"); //$NON-NLS-1$
        fillEventNames();
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        String name = event.getType().getName();
        if (eventNames.containsKey(name)) {

            switch (eventNames.get(name)) {
            case 0:
                handleMalloc(event);
                break;
            case 1:
                handleFree(event);
                break;
            case 2:
                handleCalloc(event);
                break;
            case 3:
                handleRealloc(event);
                break;
            case 4:
                handleMemalign(event);
                break;
            case 5:
                handleMemalignPosix(event);
                break;
            default:
                break;
            }
        }

    }

    private void fillEventNames() {
        eventNames.put(UstStrings.MALLOC, 0);
        eventNames.put(UstStrings.FREE, 1);
        eventNames.put(UstStrings.CALLOC, 2);
        eventNames.put(UstStrings.REALLOC, 3);
        eventNames.put(UstStrings.MEMALIGN, 4);
        eventNames.put(UstStrings.POSIX_MEMALIGN, 5);
    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return null;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    private static long getVtid(ITmfEvent event) {
        ITmfEventField field = event.getContent().getField("context._vtid"); //$NON-NLS-1$
        if (field == null) {
            return -1;
        }
        return (Long) field.getValue();
    }

    private static String getProcname(ITmfEvent event) {
        ITmfEventField field = event.getContent().getField("context._procname"); //$NON-NLS-1$
        if (field == null) {
            return new String();
        }
        return (String) field.getValue();
    }

    private void handleCalloc(ITmfEvent event) {
        long ptr = (Long) event.getContent().getField("ptr").getValue(); //$NON-NLS-1$
        if (ptr == 0) {
            // malloc failed return
        } else {
            long nmemb = (Long) event.getContent().getField("nmemb").getValue(); //$NON-NLS-1$
            long size = (Long) event.getContent().getField("size").getValue(); //$NON-NLS-1$
            setMem(event, ptr, size * nmemb);
        }
    }

    private void handleFree(ITmfEvent event) {
        long ptr = (Long) event.getContent().getField("ptr").getValue(); //$NON-NLS-1$
        if (ptr == 0) {
            // failed return
        } else {
            setMem(event, ptr, 0);
        }
    }

    private void handleMalloc(ITmfEvent event) {
        long ptr = (Long) event.getContent().getField("ptr").getValue(); //$NON-NLS-1$
        if (ptr == 0) {
            // failed return
        } else {
            long size = (Long) event.getContent().getField("size").getValue(); //$NON-NLS-1$
            setMem(event, ptr, size);
        }
    }

    private void handleMemalign(ITmfEvent event) {
        long ptr = (Long) event.getContent().getField("ptr").getValue(); //$NON-NLS-1$
        if (ptr == 0) {
            // failed return
        } else {
            long size = (Long) event.getContent().getField("size").getValue(); //$NON-NLS-1$
            setMem(event, ptr, size);
        }
    }

    private void handleMemalignPosix(ITmfEvent event) {
        long ptr = (Long) event.getContent().getField("out_ptr").getValue(); //$NON-NLS-1$
        if (ptr == 0) {
            // failed return
        } else {
            long size = (Long) event.getContent().getField("size").getValue(); //$NON-NLS-1$
            setMem(event, ptr, size);
        }
    }

    private void handleRealloc(ITmfEvent event) {
        long ptr = (Long) event.getContent().getField("ptr").getValue(); //$NON-NLS-1$
        if (ptr == 0) {
            // failed return
        } else {
            long newPtr = (Long) event.getContent().getField("in_ptr").getValue(); //$NON-NLS-1$
            long size = (Long) event.getContent().getField("size").getValue(); //$NON-NLS-1$
            setMem(event, ptr, 0);
            setMem(event, newPtr, size);
        }
    }

    private void setMem(ITmfEvent event, long ptr, long size) {
        long ts = event.getTimestamp().getValue();
        long tid = getVtid(event);

        long memoryDiff = size;
        /* Size is 0, it means it was deleted */
        if (size == 0) {
            Long memSize = fMemory.remove(ptr);
            if (memSize == null) {
                return;
            }
            memoryDiff = -1 * memSize;
        } else {
            fMemory.put(ptr, size);
        }
        try {
            int tidQuark = ss.getQuarkAbsoluteAndAdd(String.valueOf(tid));
            int tidMemQuark = ss.getQuarkRelativeAndAdd(tidQuark, UstMemoryStrings.UST_MEMORY_MEMORY_ATTRIBUTE);

            ITmfStateValue prevMem = ss.queryOngoingState(tidMemQuark);
            /* First time we set this value */
            if (prevMem.isNull()) {
                int procNameQuark = ss.getQuarkRelativeAndAdd(tidQuark, UstMemoryStrings.UST_MEMORY_PROCNAME_ATTRIBUTE);
                String procName = getProcname(event);
                /*
                 * No tid/procname for the event for the event, added to a
                 * 'total' thread
                 */
                if (tid == -1) {
                    procName = "Total"; //$NON-NLS-1$
                }
                ss.modifyAttribute(ts, TmfStateValue.newValueString(procName), procNameQuark);
                prevMem = TmfStateValue.newValueLong(0);
            }

            long prevMemValue = prevMem.unboxLong();
            prevMemValue += memoryDiff;
            ss.modifyAttribute(ts, TmfStateValue.newValueLong(prevMemValue), tidMemQuark);
        } catch (AttributeNotFoundException e1) {
        } catch (TimeRangeException e) {
        } catch (StateValueTypeException e) {
        }
    }

}
