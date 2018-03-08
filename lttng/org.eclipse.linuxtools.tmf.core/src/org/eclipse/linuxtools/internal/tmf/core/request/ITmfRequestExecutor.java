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

/**
 * The TMF request executor
 *
 * @author Simon Delisle
 */
public interface ITmfRequestExecutor extends Executor {
    /**
     * Schedule the next request
     */
    void scheduleNext();

    /**
     * @return the shutdown state (i.e. if it is accepting new requests)
     */
    boolean isShutdown();

    /**
     * @return the termination state
     */
    boolean isTerminated();

    /**
     * Stops the executor
     */
    void stop();

    /**
     * @return if the scheduler has tasks
     */
    boolean hasTasks();
}
