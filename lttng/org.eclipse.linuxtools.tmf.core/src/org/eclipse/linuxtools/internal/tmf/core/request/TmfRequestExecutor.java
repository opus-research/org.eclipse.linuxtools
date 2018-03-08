/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Simon Delisle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.request;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;

/**
 * The TMF request executor
 *
 * @author Simon Delisle
 */
public abstract class TmfRequestExecutor implements Executor {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The request executor
     */
    protected final ExecutorService fExecutorService;
    private final String fExecutorName;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     *
     * @param executorService
     *            the executor service
     */
    public TmfRequestExecutor(ExecutorService executorService) {
        fExecutorService = executorService;
        String canonicalName = fExecutorService.getClass().getCanonicalName();
        fExecutorName = canonicalName.substring(canonicalName.lastIndexOf('.') + 1);
        if (TmfCoreTracer.isComponentTraced()) {
            TmfCoreTracer.trace(fExecutorService + " created"); //$NON-NLS-1$
        }
    }

    /**
     * @return the shutdown state (i.e. if it is accepting new requests)
     */
    public synchronized boolean isShutdown() {
        return fExecutorService.isShutdown();
    }

    /**
     * @return the termination state
     */
    public synchronized boolean isTerminated() {
        return fExecutorService.isTerminated();
    }

    /**
     * Stops the executor
     */
    public synchronized void stop() {
        fExecutorService.shutdown();
        if (TmfCoreTracer.isComponentTraced()) {
            TmfCoreTracer.trace(fExecutorService + " terminated"); //$NON-NLS-1$
        }
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
