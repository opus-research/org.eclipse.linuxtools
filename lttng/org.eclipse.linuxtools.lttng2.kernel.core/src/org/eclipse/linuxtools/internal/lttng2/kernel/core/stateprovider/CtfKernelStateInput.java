/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemBuilder;

/**
 * This is the state change input plugin for TMF's state system which handles
 * the LTTng 2.0 kernel traces in CTF format.
 *
 * It uses the reference handler defined in CTFKernelHandler.java.
 *
 * @author alexmont
 *
 */
public class CtfKernelStateInput implements IStateChangeInput {

    private static final int EVENTS_QUEUE_SIZE = 10000;

    private final BlockingQueue<CtfTmfEvent> eventsQueue;

    private final CtfTmfTrace trace;
    private final CtfKernelHandler eventHandler;

    private final Thread eventHandlerThread;

    private boolean ssAssigned;

    /**
     * Instantiate a new state provider plugin.
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     */
    public CtfKernelStateInput(CtfTmfTrace trace) {
        eventsQueue = new ArrayBlockingQueue<CtfTmfEvent>(EVENTS_QUEUE_SIZE);
        this.trace = trace;
        eventHandler = new CtfKernelHandler(eventsQueue);
        ssAssigned = false;

        eventHandlerThread = new Thread(eventHandler,
                "CTF Kernel Event Handler"); //$NON-NLS-1$
    }

    @Override
    public CtfTmfTrace getTrace() {
        return trace;
    }

    @Override
    public long getStartTime() {
        return trace.getStartTime().getValue();
    }

    @Override
    public CtfTmfEvent getExpectedEventType() {
        return CtfTmfEvent.getNullEvent();
    }

    @Override
    public void assignTargetStateSystem(IStateSystemBuilder ssb) {
        eventHandler.assignStateSystem(ssb);
        ssAssigned = true;
        eventHandlerThread.start();
    }

    @Override
    public void processEvent(ITmfEvent event) {
        /* Make sure the target state system has been assigned */
        if (!ssAssigned) {
            System.err.println("Cannot process event without a target state system"); //$NON-NLS-1$
            return;
        }

        /* Insert the event we're received into the events queue */
        CtfTmfEvent currentEvent = (CtfTmfEvent) event;
        try {
            eventsQueue.put(currentEvent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        /* Insert a null event in the queue to stop the event handler's thread. */
        try {
            eventsQueue.put(CtfTmfEvent.getNullEvent());
            eventHandlerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ssAssigned = false;
        eventHandler.assignStateSystem(null);
    }
}
