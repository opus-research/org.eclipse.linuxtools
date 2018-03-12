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

package org.eclipse.linuxtools.tmf.pcap.core.analysis;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.pcap.core.protocol.pcap.PcapPacket;
import org.eclipse.linuxtools.pcap.core.stream.PacketStreamBuilder;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapEvent;
import org.eclipse.linuxtools.tmf.pcap.core.trace.PcapTrace;

/**
 * A pcap-specific analysis that parse an entire trace to find all the streams.
 *
 * @author Vincent Perot
 */
public class StreamListAnalysis extends TmfAbstractAnalysisModule {

    /**
     * The Stream List analysis ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.pcap.core.analysis.stream"; //$NON-NLS-1$

    private final ITmfEventRequest fRequest;
    private final Map<Protocol, PacketStreamBuilder> fBuilders;

    /**
     * The default constructor. It initializes all variables.
     */
    public StreamListAnalysis() {
        super();
        fBuilders = new HashMap<>();
        for (Protocol protocol : Protocol.getAllProtocols()) {
            if (protocol.supportsStream()) {
                fBuilders.put(protocol, new PacketStreamBuilder(protocol));
            }
        }

        @SuppressWarnings("null")
        @NonNull
        TmfTimeRange eternity = TmfTimeRange.ETERNITY;

        fRequest = new TmfEventRequest(PcapEvent.class,
                eternity, 0L, ITmfEventRequest.ALL_DATA,
                ITmfEventRequest.ExecutionType.BACKGROUND) {

            @Override
            public void handleData(ITmfEvent data) {
                // Called for each event
                super.handleData(data);
                if (!(data instanceof PcapEvent)) {
                    return;
                }

                PcapPacket packet = (PcapPacket) (((PcapEvent) data).getPacket().getPacket(Protocol.PCAP));
                if (packet == null) {
                    return;
                }
                for (Protocol protocol : fBuilders.keySet()) {
                    fBuilders.get(protocol).addPacketToStream(packet);
                }

            }
        };

    }

    @Override
    public boolean canExecute(ITmfTrace trace) {

        // Trace is Pcap
        if (trace instanceof PcapTrace) {
            return true;
        }

        // Trace is not a TmfExperiment
        if (!(trace instanceof TmfExperiment)) {
            return false;
        }

        // Trace is TmfExperiment. Check if it has a PcapTrace.
        TmfExperiment experiment = (TmfExperiment) trace;
        ITmfTrace[] traces = experiment.getTraces();
        for (int i = 0; i < traces.length; i++) {
            if (traces[i] instanceof PcapTrace) {
                return true;
            }
        }

        // No Pcap :(
        return false;
    }

    @Override
    protected boolean executeAnalysis(@Nullable IProgressMonitor monitor) throws TmfAnalysisException {
        IProgressMonitor mon = (monitor == null ? new NullProgressMonitor() : monitor);
        if (getTrace() == null) {
            return false;
        }
        getTrace().sendRequest(fRequest);
        try {
            fRequest.waitForCompletion();
        } catch (InterruptedException e) {
            // Request was canceled.
            return false;
        }

        return !mon.isCanceled() && !fRequest.isCancelled() && !fRequest.isFailed();

    }

    @Override
    protected void canceling() {
        fRequest.cancel();

    }

    /**
     * Getter method that returns the packet builder associated to a particular
     * protocol.
     *
     * @param protocol
     *            The specified protocol.
     * @return The builder.
     */
    public @Nullable PacketStreamBuilder getBuilder(Protocol protocol) {
        return fBuilders.get(protocol);
    }

    /**
     * Method that indicates if the analysis is still running or has finished.
     *
     * @return Whether the analysis is finished or not.
     */
    public boolean isFinished() {
        return fRequest.isCompleted();
    }

}
