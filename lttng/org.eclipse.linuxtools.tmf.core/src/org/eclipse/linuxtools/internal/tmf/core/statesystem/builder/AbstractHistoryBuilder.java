/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.tmf.core.statesystem.builder;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.StateSystem;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfHistoryBuilder;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Abstract builder class for state systems with various backends.
 *
 * @author Bernd Hufmann
 */
public abstract class AbstractHistoryBuilder implements ITmfHistoryBuilder {

    /** Size of the blocking queue to use when building a state history */
    protected static final int QUEUE_SIZE = 10000;
    /** The State provide instance */
    protected ITmfStateProvider fStateProvider;
    /** The State system instance */
    protected StateSystem fStateSystem;
    /** The history backend instances */
    protected IStateHistoryBackend fHtBackend;

    private ITmfEventRequest fRequest;

    /**
     * Default constructor
     */
    protected AbstractHistoryBuilder() {
    }

    /**
     * Gets the state system instance created by this builder.
     * @return the state system instance.
     */
    @Override
    public ITmfStateSystem getStateSystem() {
        return fStateSystem;
    }

    @Override
    public void dispose(boolean deleteFiles) {
        if (fStateProvider != null) {
            fStateProvider.dispose();
        }
        if (deleteFiles && (fHtBackend != null)) {
            fHtBackend.removeFiles();
        }
    }

    @Override
    public void build() {
        if ((fStateProvider == null) || (fStateSystem == null) || (fHtBackend == null)) {
            throw new IllegalArgumentException();
        }

        if ((fRequest != null) && (!fRequest.isCompleted())) {
            fRequest.cancel();
        }

        fRequest = new StateSystemEventRequest(this, fStateProvider);
        fStateProvider.getTrace().sendRequest(fRequest);

        try {
             fRequest.waitForCompletion();
        } catch (InterruptedException e) {
             e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        if ((fRequest != null) && (!fRequest.isCompleted())) {
            fRequest.cancel();
        }
    }

    class StateSystemEventRequest extends TmfEventRequest {
        private final AbstractHistoryBuilder builder;
        private final ITmfStateProvider sci;
        private final ITmfTrace trace;

        StateSystemEventRequest(AbstractHistoryBuilder builder, ITmfStateProvider sp) {
            super(sp.getExpectedEventType(),
                    TmfTimeRange.ETERNITY,
                    0,
                    ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.BACKGROUND);
            this.builder = builder;
            this.sci = sp;
            this.trace = sci.getTrace();
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (event != null && event.getTrace() == trace) {
                sci.processEvent(event);
            }
        }

        @Override
        public void handleSuccess() {
            super.handleSuccess();
            builder.dispose(false);
        }

        @Override
        public void handleCancel() {
            super.handleCancel();
            builder.dispose(true);
        }

        @Override
        public void handleFailure() {
            super.handleFailure();
            builder.dispose(true);
        }
    }
}
