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
 * Contributors: Etienne Bergeron <etienne.bergeron@gmail.com>
 * Contributors: Mathieu Desnoyers <mathieu.desnoyers@efficios.com>
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * <b><u>StreamInputPacketIndex</u></b>
 * <p>
 * TODO Implement me. Please.
 */
public class StreamInputPacketIndex implements List<StreamInputPacketIndexEntry> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Entries of the index. They are sorted by increasing begin timestamp.
     * index builder.
     */
    private final List<StreamInputPacketIndexEntry> fEntries = new ArrayList<>();
    private transient StreamInputPacketIndexEntry fLastElement;

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public ListIterator<StreamInputPacketIndexEntry> listIterator() {
        return fEntries.listIterator();
    }

    /**
     * Gets an iterator to the entries at a given position
     *
     * @param n
     *            the position to get
     * @return the iterator
     */
    @Override
    public ListIterator<StreamInputPacketIndexEntry> listIterator(int n) {
        return fEntries.listIterator(n);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Appends the specified element to the end of this list (optional
     * operation).
     *
     * <p>
     * Lists that support this operation may place limitations on what elements
     * may be added to this list. In particular, some lists will refuse to add
     * null elements, and others will impose restrictions on the type of
     * elements that may be added. List classes should clearly specify in their
     * documentation any restrictions on what elements may be added.
     *
     * @param entry
     *            element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     * @throws UnsupportedOperationException
     *             if the <tt>add</tt> operation is not supported by this list
     * @throws ClassCastException
     *             if the class of the specified element prevents it from being
     *             added to this list
     * @throws NullPointerException
     *             if the specified element is null and this list does not
     *             permit null elements
     * @throws IllegalArgumentException
     *             if some property of this element prevents it from being added
     *             to this list
     * @throws CTFReaderException
     *             If there was a problem reading the entry
     */
    public boolean addEntry(StreamInputPacketIndexEntry entry)
            throws CTFReaderException {

        /* Validate consistent entry. */
        if (entry.getTimestampBegin() > entry.getTimestampEnd()) {
            throw new CTFReaderException("Packet begin timestamp is after end timestamp"); //$NON-NLS-1$
        }

        /*
         * Validate entries are inserted in monotonic increasing timestamp
         * order.
         */
        if (!fEntries.isEmpty()) {
            if (entry.getTimestampBegin() < fLastElement.getTimestampBegin()) {
                throw new CTFReaderException("Packets begin timestamp decreasing"); //$NON-NLS-1$
            }
        }
        fEntries.add(entry);
        fLastElement = fEntries.get(fEntries.size() - 1);
        return true;
    }

    /**
     * Returns the first PacketIndexEntry that could include the timestamp, that
     * is the last packet with a begin timestamp smaller than the given
     * timestamp.
     *
     * @param timestamp
     *            The timestamp to look for.
     * @return The StreamInputPacketEntry that corresponds to the packet that
     *         includes the given timestamp.
     */
    public ListIterator<StreamInputPacketIndexEntry> search(final long timestamp) {
        /*
         * Start with min and max covering all the elements.
         */
        int max = fEntries.size() - 1;
        int min = 0;

        int guessI;
        StreamInputPacketIndexEntry guessEntry = null;

        /*
         * If the index is empty, return the iterator at the very beginning.
         */
        if (isEmpty()) {
            return fEntries.listIterator();
        }

        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp is negative"); //$NON-NLS-1$
        }

        /* Binary search */
        for (;;) {
            /*
             * Guess in the middle of min and max.
             */
            guessI = min + ((max - min) / 2);
            guessEntry = fEntries.get(guessI);

            /*
             * If we reached the point where we focus on a single packet, our
             * search is done.
             */
            if (min == max) {
                break;
            }

            if (timestamp <= guessEntry.getTimestampEnd()) {
                /*
                 * If the timestamp is lower or equal to the end of the guess
                 * packet, then the guess packet becomes the new inclusive max.
                 */
                max = guessI;
            } else {
                /*
                 * If the timestamp is greater than the end of the guess packet,
                 * then the new inclusive min is the packet after the guess
                 * packet.
                 */
                min = guessI + 1;
            }
        }

        return fEntries.listIterator(guessI);
    }

    /**
     * Get the last element of the index
     *
     * @return the last element in the index
     */
    public StreamInputPacketIndexEntry lastElement() {
        return fLastElement;
    }

    @Override
    public boolean isEmpty() {
        return fEntries.isEmpty();
    }

    @Override
    public StreamInputPacketIndexEntry get(int index) {
        return fEntries.get(index);
    }

    /**
     * This will work in log(n) time since the data structure contains elements
     * in a non-repeating increasing manner.
     */
    @Override
    public int indexOf(Object o) {
        int indexOf = -1;
        if (o instanceof StreamInputPacketIndexEntry) {
            StreamInputPacketIndexEntry streamInputPacketIndexEntry = (StreamInputPacketIndexEntry) o;
            indexOf = Collections.binarySearch(fEntries, streamInputPacketIndexEntry);
        }
        return (indexOf < 0) ? -1 : indexOf;
    }

    @Override
    public int size() {
        return fEntries.size();
    }

    @Override
    public boolean contains(Object o) {
        return fEntries.contains(o);
    }

    @Override
    public Iterator<StreamInputPacketIndexEntry> iterator() {
        return fEntries.iterator();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return fEntries.containsAll(c);
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends StreamInputPacketIndexEntry> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends StreamInputPacketIndexEntry> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StreamInputPacketIndexEntry set(int index, StreamInputPacketIndexEntry element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, StreamInputPacketIndexEntry element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StreamInputPacketIndexEntry remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        return fEntries.lastIndexOf(o);
    }

    @Override
    public List<StreamInputPacketIndexEntry> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(StreamInputPacketIndexEntry e) {
        try {
            return addEntry(e);
        } catch (CTFReaderException e1) {
        }
        return false;
    }
}
