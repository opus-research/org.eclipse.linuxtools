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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * <b><u>StreamInputPacketIndex</u></b>
 * <p>
 * This is a data structure containing entries, you may append to this and read
 * it. It is not thread safe.
 */
public class StreamInputPacketIndex {

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

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence).
     *
     * @return a list iterator over the elements in this list (in proper
     *         sequence)
     */
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
    public ListIterator<StreamInputPacketIndexEntry> listIterator(int n) {
        return fEntries.listIterator(n);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Returns the number of elements in this data structure. If this data structure contains more
     * than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this data structure
     */
    public int size() {
        return fEntries.size();
    }

    /**
     * Returns <tt>true</tt> if this data structure contains no elements.
     *
     * @return <tt>true</tt> if this data structure contains no elements
     */
    public boolean isEmpty() {
        return fEntries.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this data structure contains the specified element. More
     * formally, returns <tt>true</tt> if and only if this data structure contains at
     * least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o
     *            element whose presence in this data structure is to be tested
     * @return <tt>true</tt> if this data structure contains the specified element
     * @throws ClassCastException
     *             if the type of the specified element is incompatible with
     *             this data structure (<a
     *             href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException
     *             if the specified element is null and this data structure does not
     *             permit null elements (<a
     *             href="Collection.html#optional-restrictions">optional</a>)
     */
    public boolean contains(Object o) {
        return fEntries.contains(o);
    }

    /**
     * Returns an iterator over the elements in this data structure in proper sequence.
     *
     * @return an iterator over the elements in this data structure in proper sequence
     */
    public Iterator<StreamInputPacketIndexEntry> iterator() {
        return fEntries.iterator();
    }

    /**
     * Returns an array containing all of the elements in this data structure in proper
     * sequence (from first to last element).
     *
     * <p>
     * The returned array will be "safe" in that no references to it are
     * maintained by this data structure. (In other words, this method must allocate a new
     * array even if this data structure is backed by an array). The caller is thus free
     * to modify the returned array.
     *
     * <p>
     * This method acts as bridge between array-based and collection-based APIs.
     *
     * @return an array containing all of the elements in this data structure in proper
     *         sequence
     * @see Arrays#asList(Object[])
     */
    public Object[] toArray() {
        return fEntries.toArray();
    }

    /**
     * Appends the specified element to the end of this data structure (optional
     * operation).
     *
     * <p>
     * Data structures that support this operation may place limitations on what elements
     * may be added to this data structure. In particular, some data structures will refuse to add
     * null elements, and others will impose restrictions on the type of
     * elements that may be added. data structure classes should clearly specify in their
     * documentation any restrictions on what elements may be added.
     *
     * @param entry
     *            element to be appended to this data structure
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     * @throws UnsupportedOperationException
     *             if the <tt>add</tt> operation is not supported by this data structure
     * @throws ClassCastException
     *             if the class of the specified element prevents it from being
     *             added to this data structure
     * @throws NullPointerException
     *             if the specified element is null and this data structure does not
     *             permit null elements
     * @throws IllegalArgumentException
     *             if some property of this element prevents it from being added
     *             to this data structure
     * @throws CTFReaderException
     *             If there was a problem reading the entry
     */
    public boolean add(StreamInputPacketIndexEntry entry)
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

    /**
     * Returns the element at the specified position in this data structure.
     *
     * @param index
     *            index of the element to return
     * @return the element at the specified position in this data structure
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (
     *             <tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public StreamInputPacketIndexEntry get(int index) {
        return fEntries.get(index);
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this data structure, or -1 if this data structure does not contain the element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     * This will work in log(n) time since the data structure contains elements
     * in a non-repeating increasing manner.
     *
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in
     *         this data structure, or -1 if this data structure does not contain the element
     * @throws ClassCastException if the type of the specified element
     *         is incompatible with this data structure
     *         (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *         data structure does not permit null elements
     *         (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    public int indexOf(Object o) {
        int indexOf = -1;
        if (o instanceof StreamInputPacketIndexEntry) {
            StreamInputPacketIndexEntry streamInputPacketIndexEntry = (StreamInputPacketIndexEntry) o;
            indexOf = Collections.binarySearch(fEntries, streamInputPacketIndexEntry);
        }
        return (indexOf < 0) ? -1 : indexOf;
    }

}
