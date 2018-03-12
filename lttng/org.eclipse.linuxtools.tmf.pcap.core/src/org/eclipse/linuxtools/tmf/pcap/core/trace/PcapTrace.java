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

package org.eclipse.linuxtools.tmf.pcap.core.trace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.tmf.pcap.core.Activator;
import org.eclipse.linuxtools.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.pcap.core.packet.Packet;
import org.eclipse.linuxtools.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.pcap.core.protocol.pcap.PcapPacket;
import org.eclipse.linuxtools.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.pcap.core.util.LinkTypeHelper;
import org.eclipse.linuxtools.pcap.core.util.PcapTimestampScale;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTraceProperties;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TraceValidationStatus;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapEvent;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapEventField;

import com.google.common.collect.ImmutableMap;

// TODO handle fields in TmfEventType for the filter view.

/**
 * Class that represents a TMF Pcap Trace. It is used to make the glue between
 * the Pcap parser and TMF.
 *
 * @author Vincent Perot
 */
public class PcapTrace extends TmfTrace implements ITmfEventParser, ITmfTraceProperties, AutoCloseable {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final int CONFIDENCE = 50;
    private final Map<Protocol, TmfEventType> fEventTypes = new HashMap<>();
    private @Nullable PcapFile fPcapFile; // can't be final since it's set in
                                          // initTrace();
    private @Nullable ImmutableMap<String, String> fTraceProperties = null;

    @Override
    public ITmfLocation getCurrentLocation() {
        PcapFile pcap = fPcapFile;
        if (pcap == null) {
            return new TmfLongLocation(0);
        }
        return new TmfLongLocation(pcap.getCurrentRank());
    }

    @Override
    public double getLocationRatio(@Nullable ITmfLocation location) {
        TmfLongLocation loc = (TmfLongLocation) location;
        PcapFile pcap = fPcapFile;
        if (loc == null || pcap == null) {
            return 0;
        }
        try {
            // I have some doubt about what happens during indexing. Should I
            // use this.getNbEvents() instead of fPcapFile.getTotalNbPackets()?
            return (pcap.getTotalNbPackets() == 0 ? 0 : ((double) loc.getLocationInfo()) / pcap.getTotalNbPackets());
        } catch (IOException | BadPcapFileException e) {
            Activator activator = Activator.getDefault();
            if (activator == null) {
                return 0;
            }
            String message = e.getMessage();
            if (message == null) {
                message = EMPTY_STRING;
            }
            activator.logError(message, e);
            return 0;
        }

    }

    @Override
    public void initTrace(@Nullable IResource resource, @Nullable String path, @Nullable Class<? extends ITmfEvent> type) throws TmfTraceException {
        super.initTrace(resource, path, type);
        if (path == null) {
            throw new TmfTraceException("No path has been specified."); //$NON-NLS-1$
        }
        try {
            fPcapFile = new PcapFile(path);
        } catch (IOException | BadPcapFileException e) {
            throw new TmfTraceException(e.getMessage(), e);
        }
    }

    @Override
    public @Nullable PcapEvent parseEvent(@Nullable ITmfContext context) {
        if (context == null) {
            return null;
        }

        long rank = context.getRank();
        Packet packet = null;
        PcapFile pcap = fPcapFile;
        if (pcap == null) {
            return null;
        }
        try {
            pcap.seekPacket(rank);
            packet = pcap.parseNextPacket();
        } catch (IOException | BadPcapFileException | BadPacketException e) {
            Activator activator = Activator.getDefault();
            if (activator == null) {
                return null;
            }
            String message = e.getMessage();
            if (message == null) {
                message = EMPTY_STRING;
            }
            activator.logError(message, e);
            return null;
        }

        if (packet == null) {
            return null;
        }

        packet = packet.getPacket(Protocol.PCAP);
        if (packet == null) {
            return null;
        }

        long timestamp = ((PcapPacket) packet).getTimestamp();
        PcapTimestampScale scale = ((PcapPacket) packet).getTimestampScale();
        ITmfTimestamp tmfTimestamp;
        switch (scale) {
        case MICROSECOND:
            tmfTimestamp = new TmfTimestamp(timestamp, ITmfTimestamp.MICROSECOND_SCALE, (int) pcap.getTimeAccuracy());
            break;
        case NANOSECOND:
            tmfTimestamp = new TmfTimestamp(timestamp, ITmfTimestamp.NANOSECOND_SCALE, (int) pcap.getTimeAccuracy());
            break;
        default:
            throw new IllegalArgumentException("The timestamp precision is not valid!"); //$NON-NLS-1$
        }
        String fileName = pcap.getPath().substring(pcap.getPath().lastIndexOf('/') + 1);
        if (fileName == null) {
            fileName = EMPTY_STRING;
        }
        String dataLink = "linktype:" + LinkTypeHelper.toString((int) packet.getPcapFile().getDataLinkType()); //$NON-NLS-1$

        ITmfEventField[] fields = generatePacketFields(packet);
        ITmfEventField field = new PcapEventField(ITmfEventField.ROOT_FIELD_ID, EMPTY_STRING, fields, packet);
        packet = packet.getMostEcapsulatedPacket();
        if (!fEventTypes.containsKey(packet.getProtocol())) {
            String contextString = "Network/Pcap Event"; //$NON-NLS-1$
            String typeIdString = "packet:" + packet.getProtocol().getShortName(); //$NON-NLS-1$
            fEventTypes.put(packet.getProtocol(), new TmfEventType(contextString, typeIdString, null));
        }
        TmfEventType eventType = fEventTypes.get(packet.getProtocol());
        if (eventType == null) {
            eventType = new TmfEventType();
        }
        return new PcapEvent(this, rank, tmfTimestamp, dataLink, eventType, field, fileName, packet);
    }

    private static ITmfEventField[] generatePacketFields(Packet packet) {
        // TODO This is SOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO slow. Must find a
        // way to use less intermediate data structures.
        List<ITmfEventField> fieldList = new ArrayList<>();
        List<ITmfEventField> subfieldList = new ArrayList<>();
        Packet localPacket = packet.getPacket(Protocol.PCAP);

        while (localPacket != null) {
            subfieldList.clear();
            for (Map.Entry<String, String> entry : localPacket.getFields().entrySet()) {

                @SuppressWarnings("null")
                @NonNull
                String key = entry.getKey();

                @SuppressWarnings("null")
                @NonNull
                String value = entry.getValue();
                subfieldList.add(new PcapEventField(key, value, null, localPacket));
            }
            ITmfEventField[] subfieldArray = subfieldList.toArray(new ITmfEventField[subfieldList.size()]);
            fieldList.add(new PcapEventField(localPacket.getProtocol().getName(), EMPTY_STRING, subfieldArray, localPacket));
            localPacket = localPacket.getChildPacket();
        }

        ITmfEventField[] fieldArray = fieldList.toArray(new ITmfEventField[fieldList.size()]);
        if (fieldArray == null) {
            return new ITmfEventField[0];
        }
        return fieldArray;
    }

    @Override
    public ITmfContext seekEvent(double ratio) {
        long position;
        PcapFile pcap = fPcapFile;
        if (pcap == null) {
            return new TmfContext(new TmfLongLocation(0), 0);
        }

        try {
            position = (long) (ratio * pcap.getTotalNbPackets());
        } catch (IOException | BadPcapFileException e) {
            Activator activator = Activator.getDefault();
            if (activator == null) {
                return new TmfContext(new TmfLongLocation(0), 0);
            }
            String message = e.getMessage();
            if (message == null) {
                message = EMPTY_STRING;
            }
            activator.logError(message, e);
            return new TmfContext(new TmfLongLocation(0), 0);
        }
        TmfLongLocation loc = new TmfLongLocation(position);
        return new TmfContext(loc, loc.getLocationInfo());
    }

    @Override
    public ITmfContext seekEvent(@Nullable ITmfLocation location) {
        TmfLongLocation loc = (TmfLongLocation) location;
        if (loc == null) {
            return new TmfContext(new TmfLongLocation(0));
        }

        return new TmfContext(loc, loc.getLocationInfo());
    }

    @Override
    public IStatus validate(@Nullable IProject project, @Nullable String path) {

        // All validations are made when making a new pcap file.
        if (path == null) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, EMPTY_STRING);
        }
        try (PcapFile file = new PcapFile(path)) {
        } catch (IOException | BadPcapFileException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString());
        }
        return new TraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID);
    }

    @Override
    public synchronized void dispose() {
        PcapFile pcap = fPcapFile;
        if (pcap == null) {
            return;
        }
        super.dispose();
        try {
            pcap.close();
        } catch (IOException e) {
            Activator activator = Activator.getDefault();
            if (activator == null) {
                return;
            }
            String message = e.getMessage();
            if (message == null) {
                message = EMPTY_STRING;
            }
            activator.logError(message, e);
            return;
        }
    }

    @Override
    public Map<String, String> getTraceProperties() {
        PcapFile pcap = fPcapFile;
        if (pcap == null) {
            return new HashMap<>();
        }

        ImmutableMap<String, String> properties = fTraceProperties;
        if (properties == null) {
            @SuppressWarnings("null")
            @NonNull
            ImmutableMap<String, String> newProperties = ImmutableMap.<String, String> builder()
                    .put(Messages.PcapTrace_Version, String.format("%d%c%d", pcap.getMajorVersion(), '.', pcap.getMinorVersion())) //$NON-NLS-1$
                    .put(Messages.PcapTrace_TimeZoneCorrection, pcap.getTimeZoneCorrection() + " second") //$NON-NLS-1$
                    .put(Messages.PcapTrace_TimestampAccuracy, String.valueOf(pcap.getTimeAccuracy()))
                    .put(Messages.PcapTrace_MaxSnapLength, pcap.getSnapLength() + " bytes") //$NON-NLS-1$
                    .put(Messages.PcapTrace_LinkLayerHeaderType, LinkTypeHelper.toString((int) pcap.getDataLinkType()) + " (" + pcap.getDataLinkType() + ")") //$NON-NLS-1$ //$NON-NLS-2$
                    .put(Messages.PcapTrace_FileEndianness, pcap.getByteOrder().toString())
                    .build();
            fTraceProperties = newProperties;
            return newProperties;

        }

        return properties;
    }

    @Override
    public void close() {
        dispose();
    }
}
