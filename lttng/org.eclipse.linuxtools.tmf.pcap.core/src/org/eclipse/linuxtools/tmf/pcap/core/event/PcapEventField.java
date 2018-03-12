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

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.pcap.core.packet.Packet;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

/**
 * Class that represents a TMF Pcap Event Field. It is identical to a
 * TmfEventField, except that it overrides the toString() method.
 *
 * @author Vincent Perot
 */
public class PcapEventField extends TmfEventField {

    private final Packet fPacket;

    /**
     * Full constructor
     *
     * @param name
     *            The event field id.
     * @param value
     *            The event field value.
     * @param fields
     *            The list of subfields.
     * @param packet
     *            The packet from which to take the fields from.
     * @throws IllegalArgumentException
     *             If 'name' is null, or if 'fields' has duplicate field names.
     */
    public PcapEventField(String name, Object value, @Nullable ITmfEventField[] fields, Packet packet) {
        super(name, value, fields);
        fPacket = packet;
    }

    /**
     * Copy constructor
     *
     * @param field
     *            the other event field
     */
    public PcapEventField(final PcapEventField field) {
        super(field);
        fPacket = field.fPacket;
    }

    @Override
    public String toString() {
        String name = getName();
        if (name.equals(ITmfEventField.ROOT_FIELD_ID)) {
            /*
             * If this field is a top-level "field container", we will print a
             * special string that indicates the function of the packet.
             */
            return fPacket.getGlobalSummaryString();

        }
        /* The field has its own values */
        return fPacket.getLocalSummaryString();
    }

    @Override
    public @Nullable ITmfEventField getField(@Nullable String name) {
        if (name == null) {
            return null;
        }
        if (PcapEvent.EVENT_FIELD_PACKET_SOURCE.equals(name) ||
                PcapEvent.EVENT_FIELD_PACKET_DESTINATION.equals(name) ||
                PcapEvent.EVENT_FIELD_PACKET_PROTOCOL.equals(name)) {
            return new PcapFieldWrapper(name);
        }
        return super.getField(name);
    }

    /**
     * This class is used as a workaround to return custom fields for column and
     * allow filtering on those custom columns. That way, the API is not broken.
     *
     * @author Vincent Perot
     */
    private class PcapFieldWrapper implements ITmfEventField {

        private final String fName;

        public PcapFieldWrapper(String name) {
            fName = name;
        }

        @Override
        public @Nullable String getName() {
            return null;
        }

        @Override
        public @Nullable Object getValue() {
            switch (fName) {
            case PcapEvent.EVENT_FIELD_PACKET_SOURCE:
                return fPacket.getMostEcapsulatedPacket().getSourceEndpoint().toString();
            case PcapEvent.EVENT_FIELD_PACKET_DESTINATION:
                return fPacket.getMostEcapsulatedPacket().getDestinationEndpoint().toString();
            case PcapEvent.EVENT_FIELD_PACKET_PROTOCOL:
                return fPacket.getMostEcapsulatedPacket().getProtocol().getShortName().toUpperCase();
            default:
                return null;
            }
        }

        @Override
        public @Nullable String getFormattedValue() {
            return null;
        }

        @Override
        public @Nullable Collection<String> getFieldNames() {
            return null;
        }

        @Override
        public @Nullable Collection<? extends ITmfEventField> getFields() {
            return null;
        }

        @Override
        public @Nullable ITmfEventField getField(@Nullable String name) {
            return null;
        }

        @Override
        public @Nullable ITmfEventField getSubField(@Nullable String... path) {
            return null;
        }

    }

}
