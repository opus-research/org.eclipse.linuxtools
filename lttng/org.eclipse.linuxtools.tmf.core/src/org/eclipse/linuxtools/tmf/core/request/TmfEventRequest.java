/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Consolidate constructors, merge with TmfDataRequest
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.request;

import java.util.concurrent.CountDownLatch;

import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;

/**
 * An extension of TmfDataRequest for timestamped events.
 *
 * @author Francois Chouinard
 * @since 3.0
 */
public abstract class TmfEventRequest implements ITmfEventRequest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static int fRequestNumber = 0;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Class<? extends ITmfEvent> fDataType;
    private final ExecutionType fExecType;

    /** A unique request ID */
    private final int fRequestId;

    /** The requested events time range */
    private final TmfTimeRange fRange;

    /** The index (rank) of the requested event */
    protected long fIndex;

    /** The number of requested events (ALL_DATA for all) */
    protected int fNbRequested;

    /** The number of reads so far */
    private int fNbRead;

    private final CountDownLatch startedLatch = new CountDownLatch(1);
    private final CountDownLatch completedLatch = new CountDownLatch(1);

    private boolean fRequestRunning;
    private boolean fRequestCompleted;
    private boolean fRequestFailed;
    private boolean fRequestCanceled;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Request 'n' events of a given type for the given time range (given
     * priority). Events are returned in blocks of the given size.
     *
     * @param dataType
     *            The requested data type.
     * @param range
     *            The time range of the requested events. You can use
     *            {@link TmfTimeRange#ETERNITY} to indicate you want to cover
     *            the whole trace.
     * @param index
     *            The index of the first event to retrieve. You can use '0' to
     *            start at the beginning of the trace.
     * @param nbRequested
     *            The number of events requested. You can use
     *            {@link TmfEventRequest#ALL_DATA} to indicate you want all
     *            events in the time range.
     * @param priority
     *            The requested execution priority.
     */
    public TmfEventRequest(Class<? extends ITmfEvent> dataType,
            TmfTimeRange range,
            long index,
            int nbRequested,
            ExecutionType priority) {

        fRequestId = fRequestNumber++;
        fDataType = dataType;
        fIndex = index;
        fNbRequested = nbRequested;
        fExecType = priority;
        fRange = range;
        fNbRead = 0;

        fRequestRunning = false;
        fRequestCompleted = false;
        fRequestFailed = false;
        fRequestCanceled = false;

        /* Setup the request tracing if it's enabled */
        if (TmfCoreTracer.isRequestTraced()) {
            String type = getClass().getName();
            type = type.substring(type.lastIndexOf('.') + 1);
            @SuppressWarnings("nls")
            String message = "CREATED "
                    + (getExecType() == ExecutionType.BACKGROUND ? "(BG)" : "(FG)")
                    + " Type=" + type + " Index=" + getIndex() + " NbReq=" + getNbRequested()
                    + " Range=" + getRange()
                    + " DataType=" + getDataType().getSimpleName();
            TmfCoreTracer.traceRequest(this, message);
        }
    }

    /**
     * Resets the request counter (used for testing)
     */
    public static void reset() {
        fRequestNumber = 0;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the request ID
     */
    @Override
    public int getRequestId() {
        return fRequestId;
    }

    /**
     * @return the index of the first event requested
     */
    @Override
    public long getIndex() {
        return fIndex;
    }

    /**
     * @return the execution type (priority)
     */
    @Override
    public ExecutionType getExecType() {
        return fExecType;
    }

    /**
     * @return the number of requested events (ALL_DATA = all)
     */
    @Override
    public int getNbRequested() {
        return fNbRequested;
    }

    /**
     * @return the number of events read so far
     */
    @Override
    public synchronized int getNbRead() {
        return fNbRead;
    }

    /**
     * @return indicates if the request is currently running
     */
    @Override
    public synchronized boolean isRunning() {
        return fRequestRunning;
    }

    /**
     * @return indicates if the request is completed
     */
    @Override
    public synchronized boolean isCompleted() {
        return fRequestCompleted;
    }

    /**
     * @return indicates if the request has failed
     */
    @Override
    public synchronized boolean isFailed() {
        return fRequestFailed;
    }

    /**
     * @return indicates if the request is canceled
     */
    @Override
    public synchronized boolean isCancelled() {
        return fRequestCanceled;
    }

    /**
     * @return the requested data type
     */
    @Override
    public Class<? extends ITmfEvent> getDataType() {
        return fDataType;
    }

    /**
     * @return the requested time range
     */
    @Override
    public TmfTimeRange getRange() {
        return fRange;
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    /**
     * This method is called by the event provider to set the index
     * corresponding to the time range start time
     *
     * @param index
     *            The start time index
     */
    protected void setIndex(int index) {
        fIndex = index;
    }

    /**
     * This method is called by the event provider to set the index
     * corresponding to the time range start time once it is known
     *
     * @param index
     *            The start index
     */
    @Override
    public void setStartIndex(int index) {
        setIndex(index);
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    /**
     * Handle incoming data, one event at a time i.e. this method is invoked
     * for every data item obtained by the request.
     *
     * - Data items are received in the order they appear in the stream
     * - Called by the request processor, in its execution thread, every time
     *   a block of data becomes available.
     * - Request processor performs a synchronous call to handleData() i.e.
     *   its execution threads holds until handleData() returns.
     * - Original data items are disposed of on return i.e. keep a reference
     *   (or a copy) if some persistence is needed between invocations.
     * - When there is no more data, done() is called.
     *
     * @param data a piece of data
     */
    @Override
    public void handleData(ITmfEvent data) {
        if (data != null) {
            fNbRead++;
        }
    }

    @Override
    public void handleStarted() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "STARTED"); //$NON-NLS-1$
        }
    }

    /**
     * Handle the completion of the request. It is called when there is no
     * more data available either because:
     * - the request completed normally
     * - the request failed
     * - the request was canceled
     *
     * As a convenience, handleXXXX methods are provided. They are meant to be
     * overridden by the application if it needs to handle these conditions.
     */
    @Override
    public void handleCompleted() {
        boolean requestFailed = false;
        boolean requestCanceled = false;
        synchronized (this) {
            requestFailed = fRequestFailed;
            requestCanceled = fRequestCanceled;
        }

        if (requestFailed) {
            handleFailure();
        } else if (requestCanceled) {
            handleCancel();
        } else {
            handleSuccess();
        }
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "COMPLETED (" + fNbRead + " events read)"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public void handleSuccess() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "SUCCEEDED"); //$NON-NLS-1$
        }
    }

    @Override
    public void handleFailure() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "FAILED"); //$NON-NLS-1$
        }
    }

    @Override
    public void handleCancel() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "CANCELLED"); //$NON-NLS-1$
        }
    }

    /**
     * To suspend the client thread until the request starts (or is canceled).
     *
     * @throws InterruptedException
     *             If the thread was interrupted while waiting
     */
    public void waitForStart() throws InterruptedException {
        while (!fRequestRunning) {
            startedLatch.await();
        }
    }

    /**
     * To suspend the client thread until the request completes (or is
     * canceled).
     *
     * @throws InterruptedException
     *             If the thread was interrupted while waiting
     */
    @Override
    public void waitForCompletion() throws InterruptedException {
        while (!fRequestCompleted) {
            completedLatch.await();
        }
    }

    /**
     * Called by the request processor upon starting to service the request.
     */
    @Override
    public void start() {
        synchronized (this) {
            fRequestRunning = true;
        }
        handleStarted();
        startedLatch.countDown();
    }

    /**
     * Called by the request processor upon completion.
     */
    @Override
    public void done() {
        synchronized (this) {
            if (!fRequestCompleted) {
                fRequestRunning = false;
                fRequestCompleted = true;
            } else {
                return;
            }
        }
        try {
            handleCompleted();
        } finally {
            completedLatch.countDown();
        }
    }

    /**
     * Called by the request processor upon failure.
     */
    @Override
    public void fail() {
        synchronized (this) {
            fRequestFailed = true;
        }
        done();
    }

    /**
     * Called by the request processor upon cancellation.
     */
    @Override
    public void cancel() {
        synchronized (this) {
            fRequestCanceled = true;
        }
        done();
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    // All requests have a unique id
    public int hashCode() {
        return getRequestId();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TmfEventRequest) {
            TmfEventRequest request = (TmfEventRequest) other;
            return request.fDataType == fDataType
                    && request.fIndex == fIndex
                    && request.fNbRequested == fNbRequested
                    && request.fRange.equals(fRange);
        }
        return false;
    }

    @Override
    public String toString() {
        String name = getClass().getName();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            name = name.substring(dot + 1);
        }
        return '[' + name + '(' + getRequestId() + ',' + getDataType().getSimpleName() +
                ',' + getExecType() + ',' + getRange() + ',' + getIndex() +
                ',' + getNbRequested() + ")]"; //$NON-NLS-1$
    }

}
