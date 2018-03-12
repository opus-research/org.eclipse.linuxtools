/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.pcap.ui.stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapEvent;
import org.eclipse.linuxtools.tmf.pcap.core.event.TmfPacketStream;
import org.eclipse.linuxtools.tmf.pcap.core.signal.TmfNewPacketStreamSignal;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Frame;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.IUml2SDLoader;

/**
 * Loader class used to load the Stream Diagram View with packet information. <br>
 * <br>
 * TODO Load only 50 packets at a time and use a paging system. Once paging is
 * implemented, make event request only from last known timestamp until 50
 * packets are found. This will improve performance. <br>
 * TODO When selecting a packet, show it in editor and properties view. <br>
 * TODO Add find, filter, properties, print, etc interfaces
 *
 * @author Vincent Perot
 */
public class StreamDiagramLoader implements IUml2SDLoader {

    /**
     * The Stream Diagram View ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.pcap.ui.view.stream.diagram"; //$NON-NLS-1$

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private @Nullable SDView fSDView;
    private @Nullable ITmfTrace fTrace;
    private @Nullable TmfPacketStream fStream;

    /**
     * Default constructor.
     */
    public StreamDiagramLoader() {
        TmfSignalManager.register(this);
    }

    /* Signal Handlers */

    /**
     * Handler called when a trace is opened.
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        SDView sdView = fSDView;
        if (sdView != null) {
            sdView.setFrame(null);
            fTrace = signal.getTrace();
            fStream = null;
        }
    }

    /**
     * Handler called when a trace is selected. *
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        // Does not reload the same trace if already opened
        if (signal.getTrace() != fTrace) {
            SDView sdView = fSDView;
            if (sdView != null) {
                sdView.setFrame(null);
                fTrace = signal.getTrace();
                fStream = null;
            }
        }
    }

    /**
     * Handler called when a trace is closed.
     *
     * @param signal
     *            the incoming signal
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {
        if (signal.getTrace() != fTrace) {
            return;
        }

        // Clear the internal data
        fTrace = null;
        fStream = null;
        SDView sdView = fSDView;
        if (sdView != null) {
            sdView.setFrame(null);
        }
    }

    /**
     * Handler called when a new packet stream is chosen.
     *
     * @param signal
     *            the incoming signal
     */
    @TmfSignalHandler
    public void handleNewPacketStream(TmfNewPacketStreamSignal signal) {
        fStream = signal.getStream();
        createFrame();
    }

    /* Methods */

    @Override
    public void setViewer(@Nullable SDView viewer) {
        fSDView = viewer;
        createFrame();
    }

    @Override
    public String getTitleString() {
        final String title = Messages.StreamDiagramLoader_TitleString;
        if (title != null) {
            return title;
        }
        return EMPTY_STRING;
    }

    @Override
    public void dispose() {
        TmfSignalManager.deregister(this);
        final SDView view = fSDView;
        if (view != null) {
            view.dispose();
        }
    }

    private void createFrame() {
        final TmfPacketStream stream = fStream;
        final ITmfTrace trace = fTrace;
        final SDView view = fSDView;
        if (stream == null || trace == null || view == null) {
            if (view != null) {
                view.setFrame(null);
            }
            return;
        }
        String name = Messages.StreamDiagramLoader_Stream + ' ' + stream.getProtocol().getShortName() + ' ' + stream.getFirstEndpoint()
                + " <--> " + stream.getSecondEndpoint(); //$NON-NLS-1$
        final Frame frame = new Frame();
        frame.setName(name);

        /*
         * Create endpoints
         */
        final Lifeline endpointA = new Lifeline();
        endpointA.setName(stream.getFirstEndpoint());
        frame.addLifeLine(endpointA);

        final Lifeline endpointB = new Lifeline();
        endpointB.setName(stream.getSecondEndpoint());
        frame.addLifeLine(endpointB);

        // Make event request
        // TODO use start/stop time for range. Better performance.
        TmfEventRequest request = new TmfEventRequest(PcapEvent.class,
                TmfTimeRange.ETERNITY, 0L, ITmfEventRequest.ALL_DATA,
                ITmfEventRequest.ExecutionType.BACKGROUND) {

            @Override
            public void handleData(ITmfEvent data) {
                // Called for each event
                super.handleData(data);
                if (!(data instanceof PcapEvent)) {
                    return;
                }
                PcapEvent event = (PcapEvent) data;
                SyncMessage packet = new SyncMessage();
                if (stream.getFirstEndpoint().equals(event.getSourceEndpoint(stream.getProtocol())) &&
                        stream.getSecondEndpoint().equals(event.getDestinationEndpoint(stream.getProtocol()))) {
                    endpointA.getNewEventOccurrence();
                    endpointB.getNewEventOccurrence();
                    packet.setStartLifeline(endpointA);
                    packet.setEndLifeline(endpointB);
                } else if (stream.getFirstEndpoint().equals(event.getDestinationEndpoint(stream.getProtocol())) &&
                        stream.getSecondEndpoint().equals(event.getSourceEndpoint(stream.getProtocol()))) {
                    endpointA.getNewEventOccurrence();
                    endpointB.getNewEventOccurrence();
                    packet.setStartLifeline(endpointB);
                    packet.setEndLifeline(endpointA);
                } else {
                    return;
                }
                packet.setTime(event.getTimestamp());
                packet.setName(event.toString());
                frame.addMessage(packet);
            }
        };
        trace.sendRequest(request);
        view.setFrame(frame);
    }
}
