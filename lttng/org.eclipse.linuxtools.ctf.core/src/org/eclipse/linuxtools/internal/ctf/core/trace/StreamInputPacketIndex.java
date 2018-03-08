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

import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.Vector;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * <b><u>StreamInputPacketIndex</u></b>
 * <p>
 * TODO Implement me. Please.
 */
public class StreamInputPacketIndex {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Entries of the index. They are sorted by increasing begin timestamp.
     * index builder.
     */
    private final Vector<StreamInputPacketIndexEntry> entries = new Vector<StreamInputPacketIndexEntry>();

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the entries
     *
     * @return the entries
     */
    public Vector<StreamInputPacketIndexEntry> getEntries() {
        return this.entries;
    }

    /**
     * Gets an iterator to the entries
     *
     * @return an iterator to the entries
     */
    public ListIterator<StreamInputPacketIndexEntry> listIterator() {
        return this.entries.listIterator();
    }

    /**
     * Gets an iterator to the entries at a given position
     *
     * @param n
     *            the position to get
     * @return the iterator
     */
    public ListIterator<StreamInputPacketIndexEntry> listIterator(int n) {
        return this.entries.listIterator(n);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Adds an entry to the index.
     *
     * @param entry
     *            The entry to add
     * @throws CTFReaderException
     *             If there was a problem reading the entry
     */
    public void addEntry(StreamInputPacketIndexEntry entry)
            throws CTFReaderException {
        assert (entry.getContentSizeBits() != 0);
        assert (entry.getContentSizeBits() != 0);

        if (entry.getTimestampBegin() > entry.getTimestampEnd()) {
            throw new CTFReaderException("Packet begin timestamp is after end timestamp"); //$NON-NLS-1$
        }

        if (!this.entries.isEmpty()) {
            if (entry.getTimestampBegin() < this.entries.lastElement()
                    .getTimestampBegin()) {
                throw new CTFReaderException("Packets begin timestamp decreasing"); //$NON-NLS-1$
            }
        }

        this.entries.add(entry);
    }

    /**
     * This method returns the first packet with the end timestamp greater
     * or equal to the given timestamp. The returned packet is the first one
     * that could include the timestamp.
     *
     * @param timestamp
     *            The timestamp to look for.
     * @return The StreamInputPacketEntry that corresponds to the packet that
     *         includes the given timestamp.
     */
    public ListIterator<StreamInputPacketIndexEntry> search(final long timestamp) {
        /*
         * If the index is empty, return the iterator at the very beginning.
         */
        if (this.getEntries().isEmpty()) {
            return this.getEntries().listIterator();
        }

        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp is negative"); //$NON-NLS-1$
        }

        StreamInputPacketIndexEntry key = new StreamInputPacketIndexEntry(0);

        key.setTimestampEnd(timestamp);

        int guessI = Collections.binarySearch(this.entries ,key,  new StreamInputPacketIndexEntryComparator());

        guessI = (guessI < 0)? ~guessI: guessI;

        return this.entries.listIterator(guessI);
    }

    class StreamInputPacketIndexEntryComparator implements Comparator<StreamInputPacketIndexEntry>{

        @Override
        public int compare(StreamInputPacketIndexEntry left, StreamInputPacketIndexEntry right) {
            final long leftTimestampEnd = left.getTimestampEnd();
            final long rightTimestampEnd = right.getTimestampEnd();
            return leftTimestampEnd<rightTimestampEnd? -1 : leftTimestampEnd==rightTimestampEnd? 0 : 1;
        }

    }
}
