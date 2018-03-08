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

import java.util.concurrent.Executors;

import org.eclipse.linuxtools.internal.tmf.core.component.TmfEventThread;

/**
 * We use the OS scheduler to schedule the request.
 *
 * @author Simon Delisle
 */
public class TmfOSRequestScheduler extends TmfRequestExecutor {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The tasks
    private TmfEventThread fTask;

    /**
     * Default constructor
     */
    public TmfOSRequestScheduler() {
        super(Executors.newCachedThreadPool());
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
        getExecutorService().execute(fTask);
    }
}