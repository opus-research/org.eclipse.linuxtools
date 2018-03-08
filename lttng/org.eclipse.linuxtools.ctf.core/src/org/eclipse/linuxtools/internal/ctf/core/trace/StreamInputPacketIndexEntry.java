/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
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

/**
 * <b><u>StreamInputPacketIndexEntry</u></b>
 * <p>
 * Represents an entry in the index of event packets.
 */
public class StreamInputPacketIndexEntry {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------


    /**
     * Offset of the packet in the file, in bytes
     */
    final private long offsetBytes;

    /**
     * Offset of the data in the packet, in bits
     */
    private int dataOffsetBits = 0;

    /**
     * Packet size, in bits
     */
    private int packetSizeBits = 0;

    /**
     * Content size, in bits
     */
    private int contentSizeBits = 0;

    /**
     * Begin timestamp
     */
    private long timestampBegin = 0;

    /**
     * End timestamp
     */
    private long timestampEnd = 0;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs an index entry.
     *
     * @param offset
     *            The offset of the packet in the file, in bytes.
     */

    public StreamInputPacketIndexEntry(long offset) {
        this.offsetBytes = offset;
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
        return (ts >= timestampBegin) && (ts <= timestampEnd);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "StreamInputPacketIndexEntry [offsetBytes=" + offsetBytes //$NON-NLS-1$
                + ", timestampBegin=" + timestampBegin + ", timestampEnd=" //$NON-NLS-1$ //$NON-NLS-2$
                + timestampEnd + "]"; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------------

    /**
     * @return the offsetBytes
     */
    public long getOffsetBytes() {
        return offsetBytes;
    }

    /**
     * @return the dataOffsetBits
     */
    public int getDataOffsetBits() {
        return dataOffsetBits;
    }

    /**
     * @param dataOffsetBits
     *            the dataOffsetBits to set
     */
    public void setDataOffsetBits(int dataOffsetBits) {
        this.dataOffsetBits = dataOffsetBits;
    }

    /**
     * @return the packetSizeBits
     */
    public int getPacketSizeBits() {
        return packetSizeBits;
    }

    /**
     * @param packetSizeBits
     *            the packetSizeBits to set
     */
    public void setPacketSizeBits(int packetSizeBits) {
        this.packetSizeBits = packetSizeBits;
    }

    /**
     * @return the contentSizeBits
     */
    public int getContentSizeBits() {
        return contentSizeBits;
    }

    /**
     * @param contentSizeBits
     *            the contentSizeBits to set
     */
    public void setContentSizeBits(int contentSizeBits) {
        this.contentSizeBits = contentSizeBits;
    }

    /**
     * @return the timestampBegin
     */
    public long getTimestampBegin() {
        return timestampBegin;
    }

    /**
     * @param timestampBegin
     *            the timestampBegin to set
     */
    public void setTimestampBegin(long timestampBegin) {
        this.timestampBegin = timestampBegin;
    }

    /**
     * @return the timestampEnd
     */
    public long getTimestampEnd() {
        return timestampEnd;
    }

    /**
     * @param timestampEnd
     *            the timestampEnd to set
     */
    public void setTimestampEnd(long timestampEnd) {
        this.timestampEnd = timestampEnd;
    }

}
