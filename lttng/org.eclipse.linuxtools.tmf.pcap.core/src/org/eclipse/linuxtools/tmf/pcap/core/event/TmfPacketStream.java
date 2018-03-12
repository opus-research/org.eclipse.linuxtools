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

import org.eclipse.linuxtools.internal.tmf.pcap.core.util.ProtocolConversion;
import org.eclipse.linuxtools.pcap.core.stream.PacketStream;
import org.eclipse.linuxtools.tmf.pcap.core.protocol.TmfProtocol;

/**
 * Class that wraps a Packet Stream.
 *
 * @author Vincent Perot
 */
public class TmfPacketStream {

    private final PacketStream fPacketStream;

    /**
     * Class constructor.
     *
     * @param stream
     *            The stream ot build the TmfPacketStream from.
     */
    public TmfPacketStream(PacketStream stream) {
        fPacketStream = stream;
    }

    /**
     * Method that returns the first endpoint of the packet stream.
     *
     * @return The first endpoint.
     */
    public String getFirstEndpoint() {
        return fPacketStream.getEndpointPair().getFirstEndpoint().toString();
    }

    /**
     * Method that returns the second endpoint of the packet stream.
     *
     * @return The second endpoint.
     */
    public String getSecondEndpoint() {
        return fPacketStream.getEndpointPair().getSecondEndpoint().toString();
    }

    /**
     * Method that returns the ID of the packet stream.
     *
     * @return The ID of the packet stream.
     */
    public int getID() {
        return fPacketStream.getID();
    }

    /**
     * Method that returns the TmfProtocol of the packet stream.
     *
     * @return The TmfProtocol of the packet stream.
     */
    public TmfProtocol getProtocol() {
        return ProtocolConversion.wrap(fPacketStream.getProtocol());
    }

    /**
     * Method that returns the size of the packet stream.
     *
     * @return The size of the packet stream (nb of packets).
     */
    public int size() {
        return fPacketStream.size();
    }
}
