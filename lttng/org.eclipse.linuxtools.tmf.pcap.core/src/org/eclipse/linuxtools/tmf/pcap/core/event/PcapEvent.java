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

package org.eclipse.linuxtools.tmf.pcap.core.event;

import org.eclipse.linuxtools.pcap.core.packet.Packet;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Class that extends TmfEvent to allow TMF to use the packets from the parser.
 * It is a simple TmfEvent that wraps a Packet.
 *
 * @author Vincent Perot
 */
public class PcapEvent extends TmfEvent {

    /** Packet Source Field ID */
    public static final String EVENT_FIELD_PACKET_SOURCE = ":packetsource:"; //$NON-NLS-1$
    /** Packet Destination Field ID */
    public static final String EVENT_FIELD_PACKET_DESTINATION = ":packetdestination:"; //$NON-NLS-1$
    /** Packet Protocol Field ID */
    public static final String EVENT_FIELD_PACKET_PROTOCOL = ":protocol:"; //$NON-NLS-1$

    private final Packet fPacket;

    /**
     * Full constructor.
     *
     * @param trace
     *            the parent trace
     * @param rank
     *            the event rank (in the trace)
     * @param timestamp
     *            the event timestamp
     * @param source
     *            the event source
     * @param type
     *            the event type
     * @param content
     *            the event content (payload)
     * @param reference
     *            the event reference
     * @param packet
     *            The packet contained in this event
     */
    public PcapEvent(ITmfTrace trace, long rank, ITmfTimestamp timestamp, String source, TmfEventType type, ITmfEventField content, String reference, Packet packet) {
        super(trace, rank, timestamp, source, type, content, reference);
        fPacket = packet;
    }

    /**
     * Getter method that return the packt wrapped in this class.
     *
     * @return The packet.
     */
    public Packet getPacket() {
        return fPacket;
    }

}
