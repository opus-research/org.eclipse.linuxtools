/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Simon Delisle - Added scheduler for requests
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.request;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.internal.tmf.core.component.TmfEventThread;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;

/**
 * The request scheduler works with 5 slots with a specific time. It has 4 slots
 * for foreground requests and 1 slot for background requests, and it passes
 * through all the slots (foreground first and background after).
 *
 * Example: if we have one foreground and one background request, the foreground
 * request will be executed four times more often than the background request.
 *
 * @author Simon Delisle
 */
public class TmfRequestScheduler extends TmfRequestExecutor {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The time slice for the scheduler. Each 100ms we do scheduleNext
     */
    private static final long REQUEST_TIME = 100;
    /**
     * Number of slot for the foreground request
     */
    private static final int FOREGROUND_SLOT = 4;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The request queues
    private final Queue<TmfEventThread> fForegroundTasks = new ArrayBlockingQueue<TmfEventThread>(100);
    private final Queue<TmfEventThread> fBackgroundTasks = new ArrayBlockingQueue<TmfEventThread>(100);

    // The tasks
    private TmfEventThread fActiveTask;

    private final Timer fTimer = new Timer(true);
    private TimerTask fTimerTask;

    private int fForegroundCycle = 0;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfRequestScheduler() {
        super(Executors.newCachedThreadPool());

        // Initialize the timer for the schedSwitch
        fTimerTask = new SchedSwitch();
        fTimer.schedule(fTimerTask, 0, REQUEST_TIME);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public synchronized void execute(final Runnable command) {

        // We are expecting MyEventThread:s
        if (!(command instanceof TmfEventThread)) {
            Activator.logError(command + " Wrong thread type. Expecting TmfEventThread"); //$NON-NLS-1$
            return;
        }

        // Wrap the thread in a MyThread
        TmfEventThread thread = (TmfEventThread) command;
        TmfEventThread wrapper = new TmfEventThread(thread) {
            @Override
            public void run() {
                try {
                    command.run();
                } finally {
                    scheduleNext();
                }
            }
        };

        // Add the thread to the appropriate queue
        ExecutionType priority = thread.getExecType();

        if (priority == ExecutionType.FOREGROUND) {
            fForegroundTasks.add(wrapper);
        } else {
            fBackgroundTasks.add(wrapper);
        }
    }

    /**
     * Timer task to trigger scheduleNext()
     */
    private class SchedSwitch extends TimerTask {

        SchedSwitch() {
        }

        @Override
        public void run() {
            scheduleNext();
        }
    }

    /**
     * Executes the next pending request, if applicable.
     */
    private synchronized void scheduleNext() {
        if (!isShutdown()) {
            if (fActiveTask == null) {
                schedule();
            } else if (fActiveTask.getExecType() == ExecutionType.FOREGROUND) {
                if (fActiveTask.getThread().isCompleted()) {
                    schedule();
                } else {
                    if (hasTasks()) {
                        fActiveTask.getThread().suspend();
                        fForegroundTasks.add(fActiveTask);
                        schedule();
                    }
                }

            } else if (fActiveTask.getExecType() == ExecutionType.BACKGROUND) {
                if (fActiveTask.getThread().isCompleted()) {
                    schedule();
                } else {
                    if (hasTasks()) {
                        fActiveTask.getThread().suspend();
                        fBackgroundTasks.add(fActiveTask);
                        schedule();
                    }
                }
            }
        }
    }

    /**
     * Stops the executor
     */
    @Override
    public synchronized void stop() {
        fTimerTask.cancel();
        fTimer.cancel();

        if (fActiveTask != null) {
            fActiveTask.cancel();
        }

        while ((fActiveTask = fForegroundTasks.poll()) != null) {
            fActiveTask.cancel();
        }
        while ((fActiveTask = fBackgroundTasks.poll()) != null) {
            fActiveTask.cancel();
        }

        getExecutorService().shutdown();
        if (TmfCoreTracer.isComponentTraced()) {
            TmfCoreTracer.trace(getExecutorService() + " terminated"); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Determine which type of request (foreground or background) we schedule
     * next
     */
    private void schedule() {
        if (!fForegroundTasks.isEmpty()) {
            scheduleNextForeground();
        } else {
            scheduleNextBackground();
        }
    }

    /**
     * Schedule the next foreground request
     */
    private void scheduleNextForeground() {
        if (fForegroundCycle < FOREGROUND_SLOT || fBackgroundTasks.isEmpty()) {
            ++fForegroundCycle;
            fActiveTask = fForegroundTasks.poll();
            executefActiveTask();
        } else {
            fActiveTask = null;
            scheduleNextBackground();
        }
    }

    /**
     * Schedule the next background request
     */
    private void scheduleNextBackground() {
        fForegroundCycle = 0;
        if (!fBackgroundTasks.isEmpty()) {
            fActiveTask = fBackgroundTasks.poll();
            executefActiveTask();
        }
    }

    /**
     * Execute or resume the active task
     */
    private void executefActiveTask() {
        if (fActiveTask.getThread().isPaused()) {
            fActiveTask.getThread().resume();
        } else {
            getExecutorService().execute(fActiveTask);
        }
    }

    /**
     * Check if the scheduler has tasks
     *
     * @return if the scheduler has tasks
     */
    private boolean hasTasks() {
        return !(fForegroundTasks.isEmpty() && fBackgroundTasks.isEmpty());
    }
}