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
 *   Alexandre Montplaisir - Update to new TmfEventTableColumn API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.pcap.ui.editor;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapEvent;
import org.eclipse.linuxtools.tmf.pcap.core.protocol.TmfProtocol;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableColumn;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.ImmutableList;

/**
 * Default event table for pcap traces.
 *
 * @author Vincent Perot
 */
public class PcapEventsTable extends TmfEventsTable {

    // ------------------------------------------------------------------------
    // Table data
    // ------------------------------------------------------------------------

    @SuppressWarnings("null")
    private static final @NonNull Collection<TmfEventTableColumn> PCAP_COLUMNS = ImmutableList.of(
            TmfEventTableColumn.BaseColumns.TIMESTAMP,
            new PcapSourceColumn(),
            new PcapDestinationColumn(),
            TmfEventTableColumn.BaseColumns.REFERENCE,
            new PcapProtocolColumn(),
            TmfEventTableColumn.BaseColumns.CONTENTS
            );

    /**
     * The "packet source" column for pcap events
     */
    private static class PcapSourceColumn extends TmfEventTableColumn {

        public PcapSourceColumn() {
            super(getString(Messages.PcapEventsTable_Source));
        }

        @Override
        public String getItemString(ITmfEvent event) {
            if (!(event instanceof PcapEvent)) {
                return EMPTY_STRING;
            }
            PcapEvent pcapEvent = (PcapEvent) event;
            TmfProtocol protocol = pcapEvent.getMostEncapsulatedProtocol();

            return getString(pcapEvent.getSourceEndpoint(protocol));
        }

        @Override
        public @Nullable String getFilterFieldId() {
            return PcapEvent.EVENT_FIELD_PACKET_SOURCE;
        }
    }

    /**
     * The "packet destination" column for pcap events
     */
    private static class PcapDestinationColumn extends TmfEventTableColumn {

        public PcapDestinationColumn() {
            super(getString(Messages.PcapEventsTable_Destination));
        }

        @Override
        public String getItemString(ITmfEvent event) {
            if (!(event instanceof PcapEvent)) {
                return EMPTY_STRING;
            }
            PcapEvent pcapEvent = (PcapEvent) event;
            TmfProtocol protocol = pcapEvent.getMostEncapsulatedProtocol();
            return getString(pcapEvent.getDestinationEndpoint(protocol));
        }

        @Override
        public @Nullable String getFilterFieldId() {
            return PcapEvent.EVENT_FIELD_PACKET_DESTINATION;
        }
    }

    /**
     * The "packet protocol" column for pcap events
     */
    private static class PcapProtocolColumn extends TmfEventTableColumn {

        public PcapProtocolColumn() {
            super(getString(Messages.PcapEventsTable_Protocol));
        }

        @Override
        public String getItemString(ITmfEvent event) {
            if (!(event instanceof PcapEvent)) {
                return EMPTY_STRING;
            }
            PcapEvent pcapEvent = (PcapEvent) event;
            TmfProtocol protocol = pcapEvent.getMostEncapsulatedProtocol();

            @SuppressWarnings("null")
            @NonNull String proto = protocol.getShortName().toUpperCase();
            return proto;
        }

        @Override
        public @Nullable String getFilterFieldId() {
            return PcapEvent.EVENT_FIELD_PACKET_PROTOCOL;
        }
    }

    /**
     * Little convenience method to work around the incompatibilities between
     * null annotations and NLS files...
     */
    private static String getString(@Nullable String str) {
        return (str == null ? EMPTY_STRING : str);
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param cacheSize
     *            The size of the rows cache
     */
    public PcapEventsTable(Composite parent, int cacheSize) {
        super(parent, cacheSize, PCAP_COLUMNS);
    }
}
