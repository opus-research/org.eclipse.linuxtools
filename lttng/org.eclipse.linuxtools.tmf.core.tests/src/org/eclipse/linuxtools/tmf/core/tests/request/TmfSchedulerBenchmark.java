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

package org.eclipse.linuxtools.tmf.core.tests.request;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;

/**
 * Benchmark for the request scheduler *
 */
public class TmfSchedulerBenchmark {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String PATH = "../org.eclipse.linuxtools.ctf.core.tests/traces/kernel"; //$NON-NLS-1$
    private static final String BENCHMARKFILE = "Request_benchmark.txt"; //$NON-NLS-1$
    private static final int NUM_LOOPS = 3;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private CtfTmfTrace fTrace;
    private PrintWriter fWriter;
    private long averageLatency = 0;
    private long averageWaitingBackground = 0;
    private long averageWaitingForeground1 = 0;
    private long averageWaitingForeground2 = 0;
    private long averageCompletedTime1 = 0;
    private long averageCompletedTime2 = 0;
    private long averageCompletedTime3 = 0;
    private long averageCompletedTime4 = 0;
    private long averageCompletedTime5 = 0;
    private long averageCompletedTime6 = 0;
    private ForegroundRequest lastForegroundRequest = null;
    private BackgroundRequest lastBackgroundReqeust = null;

    /**
     * Run the benchmark
     *
     * @param args
     *            The command-line arguments
     * @throws TmfTraceException
     *             If the trace is not found
     * @throws FileNotFoundException
     *             If the file is not found
     */
    public static void main(final String[] args) throws FileNotFoundException, TmfTraceException {
        TmfSchedulerBenchmark benchmark = new TmfSchedulerBenchmark();
        benchmark.startBenchmark();
    }

    /**
     * Default constructor
     *
     * @throws TmfTraceException
     *             If the trace is not found
     * @throws FileNotFoundException
     *             If the file is not found
     */
    public TmfSchedulerBenchmark() throws TmfTraceException, FileNotFoundException {
        fWriter = new PrintWriter(BENCHMARKFILE);
        fTrace = new CtfTmfTrace();
        fTrace.initTrace((IResource) null, PATH, CtfTmfEvent.class);
        fTrace.indexTrace(true);
    }

    /**
     * Start the benchmark
     */
    public void startBenchmark() {
        fWriter.println("---------- Benchmark started ! ----------"); //$NON-NLS-1$
        latencyBenchmark();
        averageWaitingTime();
        completedTime();
        benchmarkResults();
    }

    private void latencyBenchmark() {

        fWriter.println("----- Latency -----"); //$NON-NLS-1$
        for (int i = 0; i < NUM_LOOPS; i++) {
            try {
                ForegroundRequest foreground1 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                fTrace.sendRequest(foreground1);
                foreground1.waitForCompletion();
                averageLatency += foreground1.getLatency();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        fWriter.println((averageLatency / NUM_LOOPS) / 1000000 + " ms"); //$NON-NLS-1$
    }

    private void averageWaitingTime() {
        fWriter.println("----- Average waiting time with 3 requests -----"); //$NON-NLS-1$
        for (int i = 0; i < NUM_LOOPS; i++) {
            ForegroundRequest foreground1 = new ForegroundRequest(TmfTimeRange.ETERNITY);
            ForegroundRequest foreground2 = new ForegroundRequest(TmfTimeRange.ETERNITY);
            BackgroundRequest background1 = new BackgroundRequest(TmfTimeRange.ETERNITY);
            fTrace.sendRequest(background1);
            fTrace.sendRequest(foreground1);
            fTrace.sendRequest(foreground2);
            try {
                foreground1.waitForCompletion();
                foreground2.waitForCompletion();
                background1.waitForCompletion();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            averageWaitingBackground += background1.getAverageWaitingTime();
            averageWaitingForeground1 += foreground1.getAverageWaitingTime();
            averageWaitingForeground2 += foreground2.getAverageWaitingTime();
        }
        fWriter.println("-- Background --"); //$NON-NLS-1$
        fWriter.println((averageWaitingBackground / NUM_LOOPS) / 1000000 + " ms"); //$NON-NLS-1$

        fWriter.println("-- First foreground --"); //$NON-NLS-1$
        fWriter.println((averageWaitingForeground1 / NUM_LOOPS) / 1000000 + " ms"); //$NON-NLS-1$

        fWriter.println("-- Second foreground --"); //$NON-NLS-1$
        fWriter.println((averageWaitingForeground2 / NUM_LOOPS) / 1000000 + " ms"); //$NON-NLS-1$
    }

    private void completedTime() {
        fWriter.println("----- Time to complete request -----"); //$NON-NLS-1$
        for (int i = 0; i < NUM_LOOPS; i++) {
            try {
                ForegroundRequest foreground1 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                fTrace.sendRequest(foreground1);
                foreground1.waitForCompletion();
                averageCompletedTime1 += foreground1.getCompletedTime();

                ForegroundRequest foreground2 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground3 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                fTrace.sendRequest(foreground2);
                fTrace.sendRequest(foreground3);
                foreground2.waitForCompletion();
                foreground3.waitForCompletion();
                averageCompletedTime2 += (foreground2.getCompletedTime() + foreground3.getCompletedTime());

                ForegroundRequest foreground4 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                BackgroundRequest background1 = new BackgroundRequest(TmfTimeRange.ETERNITY);
                fTrace.sendRequest(foreground4);
                fTrace.sendRequest(background1);
                foreground4.waitForCompletion();
                background1.waitForCompletion();
                averageCompletedTime3 += (foreground4.getCompletedTime() + background1.getCompletedTime());

                ForegroundRequest foreground5 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground6 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                BackgroundRequest background2 = new BackgroundRequest(TmfTimeRange.ETERNITY);
                fTrace.sendRequest(foreground5);
                fTrace.sendRequest(foreground6);
                fTrace.sendRequest(background2);
                foreground5.waitForCompletion();
                foreground6.waitForCompletion();
                background2.waitForCompletion();
                averageCompletedTime4 += (foreground5.getCompletedTime() + foreground6.getCompletedTime() + background2.getCompletedTime());

                ForegroundRequest foreground7 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground8 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground9 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                BackgroundRequest background3 = new BackgroundRequest(TmfTimeRange.ETERNITY);
                fTrace.sendRequest(foreground7);
                fTrace.sendRequest(foreground8);
                fTrace.sendRequest(foreground9);
                fTrace.sendRequest(background3);
                foreground7.waitForCompletion();
                foreground8.waitForCompletion();
                foreground9.waitForCompletion();
                background3.waitForCompletion();
                averageCompletedTime5 += (foreground7.getCompletedTime() + foreground8.getCompletedTime() + foreground9.getCompletedTime() + background3.getCompletedTime());

                ForegroundRequest foreground10 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground11 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground12 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground13 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                BackgroundRequest background4 = new BackgroundRequest(TmfTimeRange.ETERNITY);
                fTrace.sendRequest(foreground10);
                fTrace.sendRequest(foreground11);
                fTrace.sendRequest(foreground12);
                fTrace.sendRequest(foreground13);
                fTrace.sendRequest(background4);
                foreground10.waitForCompletion();
                foreground11.waitForCompletion();
                foreground12.waitForCompletion();
                foreground13.waitForCompletion();
                background4.waitForCompletion();
                averageCompletedTime6 += (foreground10.getCompletedTime() + foreground11.getCompletedTime() + foreground12.getCompletedTime() + foreground13.getCompletedTime() + background4.getCompletedTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        fWriter.println("-- Time to complete one request --"); //$NON-NLS-1$
        fWriter.println((averageCompletedTime1 / NUM_LOOPS) / 1000000000 + " s"); //$NON-NLS-1$

        fWriter.println("-- Time to complete 2 requests (2 foreground) --"); //$NON-NLS-1$
        fWriter.println((averageCompletedTime2 / NUM_LOOPS) / 1000000000 + " s"); //$NON-NLS-1$

        fWriter.println("-- Time to complete 2 requests (1 foreground, 1 background) --"); //$NON-NLS-1$
        fWriter.println((averageCompletedTime3 / NUM_LOOPS) / 1000000000 + " s"); //$NON-NLS-1$

        fWriter.println("-- Time to complete 3 requests (2 foreground, 1 background) --"); //$NON-NLS-1$
        fWriter.println((averageCompletedTime4 / NUM_LOOPS) / 1000000000 + " s"); //$NON-NLS-1$

        fWriter.println("-- Time to complete 4 requests (3 foreground, 1 background) --"); //$NON-NLS-1$
        fWriter.println((averageCompletedTime5 / NUM_LOOPS) / 1000000000 + " s"); //$NON-NLS-1$

        fWriter.println("-- Time to complete 5 requests (4 foreground, 1 background) --"); //$NON-NLS-1$
        fWriter.println((averageCompletedTime6 / NUM_LOOPS) / 1000000000 + " s"); //$NON-NLS-1$
    }

    /**
     * The benchmark results
     */
    public void benchmarkResults() {
        fWriter.println("---------- Benchmark completed ! ----------"); //$NON-NLS-1$
        fWriter.close();
        System.out.println("Benchmark completed !"); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private class BackgroundRequest extends TmfEventRequest {
        private static final int CHUNK_SIZE = 0;
        private long startTime;
        private long endTimeLatency = -1;
        private long completedTime = 0;
        private long waitingTimeStart = 0;
        private long waitingTimeEnd = 0;
        private long waitingTime = 0;
        private int waitingCounter = 0;
        private boolean isWaiting = false;

        BackgroundRequest(TmfTimeRange timeRange) {
            super(fTrace.getEventType(), timeRange,
                    TmfDataRequest.ALL_DATA,
                    CHUNK_SIZE,
                    ExecutionType.BACKGROUND);
            startTime = System.nanoTime();
        }

        @Override
        public void handleData(final ITmfEvent event) {
            if (endTimeLatency == -1) {
                endTimeLatency = System.nanoTime();
            }
            super.handleData(event);
            if (lastForegroundRequest == null && lastBackgroundReqeust == null) {
                lastBackgroundReqeust = this;
            }
            if (isWaiting) {
                waitingTimeEnd = System.nanoTime();
                waitingTime += waitingTimeEnd - waitingTimeStart;
                ++waitingCounter;
                isWaiting = false;
            }
            if (lastForegroundRequest != null) {
                lastForegroundRequest.waitingTimeStart = System.nanoTime();
                lastForegroundRequest.isWaiting = true;
                lastForegroundRequest = null;
                lastBackgroundReqeust = this;
            }
            if (lastBackgroundReqeust != this) {
                lastBackgroundReqeust.waitingTimeStart = System.nanoTime();
                lastBackgroundReqeust.isWaiting = true;
                lastBackgroundReqeust = this;
            }
        }

        @Override
        public void handleCompleted() {
            completedTime = System.nanoTime();
            super.handleCompleted();
        }

        public long getLatency() {
            return endTimeLatency - startTime;
        }

        public long getCompletedTime() {
            return completedTime - startTime;
        }

        public long getAverageWaitingTime() {
            if (waitingCounter == 0) {
                return 0;
            }
            return waitingTime / waitingCounter;
        }
    }

    private class ForegroundRequest extends TmfEventRequest {
        private static final int CHUNK_SIZE = 0;
        private long startTime = 0;
        private long endTimeLatency = -1;
        private long completedTime = 0;
        private long waitingTimeStart = 0;
        private long waitingTimeEnd = 0;
        private long waitingTime = 0;
        private int waitingCounter = 0;
        private boolean isWaiting = false;

        ForegroundRequest(TmfTimeRange timeRange) {
            super(fTrace.getEventType(), timeRange,
                    TmfDataRequest.ALL_DATA,
                    CHUNK_SIZE,
                    ExecutionType.FOREGROUND);
            startTime = System.nanoTime();
        }

        @Override
        public void handleData(final ITmfEvent event) {
            if (endTimeLatency == -1) {
                endTimeLatency = System.nanoTime();
            }
            super.handleData(event);
            if (lastBackgroundReqeust == null && lastForegroundRequest == null) {
                lastForegroundRequest = this;
            }
            if (isWaiting) {
                waitingTimeEnd = System.nanoTime();
                waitingTime += waitingTimeEnd - waitingTimeStart;
                ++waitingCounter;
                isWaiting = false;
            }
            if (lastBackgroundReqeust != null) {
                lastBackgroundReqeust.waitingTimeStart = System.nanoTime();
                lastBackgroundReqeust.isWaiting = true;
                lastBackgroundReqeust = null;
                lastForegroundRequest = this;
            }
            if (lastForegroundRequest != this) {
                lastForegroundRequest.waitingTimeStart = System.nanoTime();
                lastForegroundRequest.isWaiting = true;
                lastForegroundRequest = this;
            }
        }

        @Override
        public void handleCompleted() {
            completedTime = System.nanoTime();
            super.handleCompleted();
        }

        public long getLatency() {
            return endTimeLatency - startTime;
        }

        public long getCompletedTime() {
            return completedTime - startTime;
        }

        public long getAverageWaitingTime() {
            if (waitingCounter == 0) {
                return 0;
            }
            return waitingTime / waitingCounter;
        }
    }
}
