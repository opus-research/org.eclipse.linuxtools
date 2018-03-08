/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;

/**
 * "Buffer" between the TimeRange signals received by the view and the requests
 * that are send to the statistics back-end. We wait until $DELAY elapses since
 * the last signal before sending a request.
 *
 * @author Alexandre Montplaisir
 */
class RequestProcessor {

    /** Delay (in ms) to wait before sending a statistics request */
    private static final int DELAY = 500;

    private final TmfStatisticsViewer viewer;
    private final Timer timer;
    private TimerTask currentTask;

    RequestProcessor(TmfStatisticsViewer viewer) {
        this.viewer = viewer;
        this.timer = new Timer();

        /*
         * Initialize currentTask to something so we don't have to do a null
         * check every time
         */
        currentTask = new TimerTask() { @Override public void run() {} };
    }

    synchronized void setLatestSignal(TmfRangeSynchSignal signal) {
        currentTask.cancel();
        currentTask = new SendRequest(signal);
        timer.schedule(currentTask, DELAY);
    }

    synchronized void dispose() {
        timer.cancel();
        timer.purge();
    }

    private class SendRequest extends TimerTask {

        private final TmfRangeSynchSignal signal;

        SendRequest(TmfRangeSynchSignal signal) {
            this.signal = signal;
        }

        @Override
        public void run() {
            viewer.requestTimeRangeData(signal.getCurrentRange());
        }
    }
}