/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.trace;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.ctf.core.event.types.EnumDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.FloatDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;

/**
 * <b><u>StreamInputPacketIndexEntry</u></b>
 * <p>
 * Represents an entry in the index of event packets.
 */
public class StreamInputPacketIndexEntry implements Comparable<StreamInputPacketIndexEntry> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Offset of the packet in the file, in bytes
     */
    private final long fOffsetBytes;

    /**
     * Offset of the data in the packet, in bits
     */
    private final long fDataOffsetBits;

    /**
     * Packet size, in bits
     */
    private final long fPacketSizeBits;

    /**
     * Content size, in bits
     */
    private final long fContentSizeBits;

    /**
     * Begin timestamp
     */
    private final long fTimestampBegin;

    /**
     * End timestamp
     */
    private final long fTimestampEnd;

    /**
     * How many lost events are there?
     */
    private final long fLostEvents;

    /**
     * Which target is being traced
     */
    private String fTarget;
    private long fTargetID;

    /**
     * Attributes of this index entry
     */
    private final Map<String, Object> fAttributes = new HashMap<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs an index entry.
     *
     * @param dataOffsetBits
     *            offset in the file for the start of data
     * @param fileSizeBytes
     *            number of bytes in a file
     *
     * @param position
     *            position in the bitbuffer for the number of bytes this packet
     *            is located at
     */

    public StreamInputPacketIndexEntry(long dataOffsetBits, long fileSizeBytes, long position) {
        fDataOffsetBits = dataOffsetBits;
        fContentSizeBits = (fileSizeBytes * 8);
        fPacketSizeBits = (fileSizeBytes * 8);
        fOffsetBytes = position;
        fLostEvents = 0;
        fTarget = ""; //$NON-NLS-1$
        fTargetID = 0;
        fTimestampBegin = Long.MIN_VALUE;
        fTimestampEnd = Long.MAX_VALUE;

    }

    /**
     * full Constructor
     *
     * @param dataOffsetBits
     *            offset in the file for the start of data
     * @param streamPacketContextDef
     *            packet context
     * @param fileSizeBytes
     *            number of bytes in a file
     * @param position
     *            position in the bitbuffer that the packet is located at
     * @param lostSoFar
     *            number of lost events so far
     */
    public StreamInputPacketIndexEntry(long dataOffsetBits, StructDefinition streamPacketContextDef, long fileSizeBytes, long position, long lostSoFar) {
        fDataOffsetBits = dataOffsetBits;
        for (String field : streamPacketContextDef.getDeclaration()
                .getFieldsList()) {
            IDefinition id = streamPacketContextDef.lookupDefinition(field);
            if (id instanceof IntegerDefinition) {
                this.addAttribute(field,
                        ((IntegerDefinition) id).getValue());
            } else if (id instanceof FloatDefinition) {
                this.addAttribute(field,
                        ((FloatDefinition) id).getValue());
            } else if (id instanceof EnumDefinition) {
                this.addAttribute(field,
                        ((EnumDefinition) id).getValue());
            } else if (id instanceof StringDefinition) {
                this.addAttribute(field,
                        ((StringDefinition) id).getValue());
            }
        }

        Long contentSize = (Long) this.lookupAttribute("content_size"); //$NON-NLS-1$
        Long packetSize = (Long) this.lookupAttribute("packet_size"); //$NON-NLS-1$
        Long tsBegin = (Long) this.lookupAttribute("timestamp_begin"); //$NON-NLS-1$
        Long tsEnd = (Long) this.lookupAttribute("timestamp_end"); //$NON-NLS-1$
        String device = (String) this.lookupAttribute("device"); //$NON-NLS-1$
        // LTTng Specific
        Long cpuId = (Long) this.lookupAttribute("cpu_id"); //$NON-NLS-1$
        Long lostEvents = (Long) this.lookupAttribute("events_discarded"); //$NON-NLS-1$

        /* Read the content size in bits */
        if (contentSize != null) {
            fContentSizeBits = (contentSize.intValue());
        } else if (packetSize != null) {
            fContentSizeBits = (packetSize.longValue());
        } else {
            fContentSizeBits = ((int) (fileSizeBytes * 8));
        }

        /* Read the packet size in bits */
        if (packetSize != null) {
            fPacketSizeBits = (packetSize.intValue());
        } else if (this.getContentSizeBits() != 0) {
            fPacketSizeBits = (getContentSizeBits());
        } else {
            fPacketSizeBits = ((int) (fileSizeBytes * 8));
        }

        /* Read the begin timestamp */
        if (tsBegin != null) {
            fTimestampBegin = (tsBegin.longValue());
        } else {
            fTimestampBegin = Long.MIN_VALUE;
        }

        /* Read the end timestamp */
        if (tsEnd != null) {
            if (tsEnd == -1) {
                tsEnd = Long.MAX_VALUE;
            }
            fTimestampEnd = (tsEnd.longValue());
        } else {
            fTimestampEnd = Long.MAX_VALUE;
        }

        if (device != null) {
            this.setTarget(device);
        }

        if (cpuId != null) {
            this.setTarget("CPU" + cpuId.toString()); //$NON-NLS-1$
        }

        if (lostEvents != null) {
            fLostEvents = (lostEvents - lostSoFar);
        } else {
            fLostEvents = 0;
        }

        fOffsetBytes = position;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Returns whether the packet includes (inclusively) the given timestamp in
     * the begin-end timestamp range.
     *
     * @param ts
     *            The timestamp to check.
     * @return True if the packet includes the timestamp.
     */
    boolean includes(long ts) {
        return (ts >= fTimestampBegin) && (ts <= fTimestampEnd);
    }

    @Override
    public String toString() {
        return "StreamInputPacketIndexEntry [offsetBytes=" + fOffsetBytes //$NON-NLS-1$
                + ", timestampBegin=" + fTimestampBegin + ", timestampEnd=" //$NON-NLS-1$ //$NON-NLS-2$
                + fTimestampEnd + "]"; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------------

    /**
     * @return the offsetBytes
     */
    public long getOffsetBytes() {
        return fOffsetBytes;
    }

    /**
     * @return the dataOffsetBits
     */
    public long getDataOffsetBits() {
        return fDataOffsetBits;
    }

    /**
     * @return the packetSizeBits
     */
    public long getPacketSizeBits() {
        return fPacketSizeBits;
    }

    /**
     * @return the contentSizeBits
     */
    public long getContentSizeBits() {
        return fContentSizeBits;
    }

    /**
     * @return the timestampBegin
     */
    public long getTimestampBegin() {
        return fTimestampBegin;
    }

    /**
     * @return the timestampEnd
     */
    public long getTimestampEnd() {
        return fTimestampEnd;
    }

    /**
     * @return the lostEvents in this packet
     */
    public long getLostEvents() {
        return fLostEvents;
    }

    /**
     * Add an attribute to this index entry
     *
     * @param field
     *            The name of the attribute
     * @param value
     *            The value to insert
     */
    public void addAttribute(String field, Object value) {
        fAttributes.put(field, value);
    }

    /**
     * Retrieve the value of an existing attribute
     *
     * @param field
     *            The name of the attribute
     * @return The value that was stored, or null if it wasn't found
     */
    public Object lookupAttribute(String field) {
        return fAttributes.get(field);
    }

    /**
     * @return The target that is being traced
     */
    public String getTarget() {
        return fTarget;
    }

    /**
     * Assign a target to this index entry
     *
     * @param target
     *            The target to assign
     */
    public void setTarget(String target) {
        fTarget = target;
        fTargetID = Integer.parseInt(target.replaceAll("[\\D]", "")); //$NON-NLS-1$ //$NON-NLS-2$ // slow
    }

    /**
     * @return The ID of the target
     */
    public long getTargetId() {
        return fTargetID;
    }

    @Override
    public int compareTo(StreamInputPacketIndexEntry o) {
        if (fTimestampBegin > o.fTimestampBegin) {
            return 1;
        }
        if (fTimestampBegin < o.fTimestampBegin) {
            return -1;
        }
        if (fTimestampEnd > o.fTimestampEnd) {
            return 1;
        }
        if (fTimestampEnd < o.fTimestampEnd) {
            return -1;
        }
        return 0;
    }
}
