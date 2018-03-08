/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Simon Delisle - Use the OS scheduler for requests
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.request;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.internal.tmf.core.component.TmfEventThread;

/**
 * We use the OS scheduler to schedule the request.
 *
 * @author Simon Delisle
 */
public class TmfOSRequestScheduler implements ITmfRequestExecutor {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The request executor
    private final ExecutorService fExecutorService = Executors.newCachedThreadPool();
    private final String fExecutorName;

    // The tasks
    private TmfEventThread fTask;

    /**
     * Default constructor
     */
    public TmfOSRequestScheduler() {
        String canonicalName = fExecutorService.getClass().getCanonicalName();
        fExecutorName = canonicalName.substring(canonicalName.lastIndexOf('.') + 1);
        if (TmfCoreTracer.isComponentTraced()) {
            TmfCoreTracer.trace(fExecutorService + " created"); //$NON-NLS-1$
        }
    }

    @Override
    public void execute(final Runnable command) {
        // We are expecting MyEventThread:s
        if (!(command instanceof TmfEventThread)) {
            // TODO: Log an error
            return;
        }

        // Wrap the thread in a MyThread
        fTask = (TmfEventThread) command;
        scheduleNext();
    }

    @Override
    public void scheduleNext() {
        fExecutorService.execute(fTask);
    }

    @Override
    public boolean isShutdown() {
        return fExecutorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return fExecutorService.isTerminated();
    }

    @Override
    public void stop() {
        fExecutorService.shutdown();
        if (TmfCoreTracer.isComponentTraced()) {
            TmfCoreTracer.trace(fExecutorService + " terminated"); //$NON-NLS-1$
        }
    }

    @Override
    public boolean hasTasks() {
        return false;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfRequestExecutor(" + fExecutorName + ")]";
    }
}
