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

package org.eclipse.linuxtools.tmf.core.component;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;

/**
 * Accumulator of requests to be coalesced together. After a set amount of time
 * elapses with no new request joining the queue, the existing ones will be
 * sent.
 *
 * @author Alexandre Montplaisir
 */
public class TmfRequestAccumulator {

    private final TmfEventProvider fProvider;
    private final long fDelay;
    private final Timer fTimer;
    private TimerTask fCurrentTask;

    /**
     * Constructor
     *
     * @param provider
     *            The provider to which the requests are sent
     * @param delay
     *            Grace time to allow between requests, before sending the "bus"
     *            (in ms)
     */
    public TmfRequestAccumulator(TmfEventProvider provider, long delay) {
        fProvider = provider;
        fDelay = delay;
        fTimer = new Timer();

        /*
         * Initialize currentTask to something, so we don't have to do a null
         * check every time
         */
        fCurrentTask = new TimerTask() { @Override public void run() {} };
    }

    /**
     * Queue a request for coalescing. The request will only actually be sent
     * when 'delay' has expired with no new request coming in.
     *
     * @param request
     *            The request to queue
     */
    public synchronized void queue(ITmfEventRequest request) {
        fCurrentTask.cancel();

        fProvider.coalesceEventRequest(request);

        fCurrentTask = new RequestSender();
        fTimer.schedule(fCurrentTask, fDelay);
    }

    /**
     * Dispose method. Will discard any existing request, and prevent the timer
     * from being used ever again.
     */
    public synchronized void dispose() {
        fTimer.cancel();
        fTimer.purge();
    }

    /**
     * What to do when the timer elapses.
     */
    private class RequestSender extends TimerTask {
        @Override
        public void run() {
            fProvider.fireRequest();
        }
    }
}
