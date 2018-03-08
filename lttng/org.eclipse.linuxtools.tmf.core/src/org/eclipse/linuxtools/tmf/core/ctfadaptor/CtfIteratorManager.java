/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Ctf Iterator Manager, allows mapping of iterators (a limited resource) to
 * contexts (many many resources).
 *
 * @author Matthew Khouzam
 * @version 1.0
 * @since 1.1
 */
public abstract class CtfIteratorManager {
    /*
     * A side note synchronized works on the whole object, Therefore add and
     * remove will be thread safe.
     */

    /*
     * The map of traces to trace managers.
     */
    private static HashMap<CtfTmfTrace, CtfTraceManager> map = new HashMap<CtfTmfTrace, CtfTraceManager>();

    /**
     * Registers a trace to the iterator manager, the trace can now get
     * iterators.
     *
     * @param trace
     *            the trace to register.
     */
    public static synchronized void addTrace(final CtfTmfTrace trace) {
        map.put(trace, new CtfTraceManager(trace));
    }

    /**
     * Removes a trace to the iterator manager.
     *
     * @param trace
     *            the trace to register.
     */
    public static synchronized void removeTrace(final CtfTmfTrace trace) {
        map.remove(trace);
    }

    /**
     * Get an iterator for a given trace and context.
     *
     * @param trace
     *            the trace
     * @param ctx
     *            the context
     * @return the iterator
     */
    public static synchronized CtfIterator getIterator(final CtfTmfTrace trace,
            final CtfTmfLightweightContext ctx) {
        return map.get(trace).getIterator(ctx);
    }
}

/**
 * A trace manager
 *
 * @author Matthew Khouzam
 */
class CtfTraceManager {
    /*
     * Cache size. Under 1023 on linux32 systems. Number of file handles
     * created.
     */
    private final int MAX_SIZE = 100;
    /*
     * The map of the cache.
     */
    private final HashMap<CtfTmfLightweightContext, CtfIterator> map;
    /*
     * An array pointing to the same cache. this allows fast "random" accesses.
     */
    private final ArrayList<CtfTmfLightweightContext> randomAccess;
    /*
     * The parent trace
     */
    private final CtfTmfTrace fTrace;
    /*
     * Random number generator
     */
    private final Random rnd;

    public CtfTraceManager(CtfTmfTrace trace) {
        map = new HashMap<CtfTmfLightweightContext, CtfIterator>();
        randomAccess = new ArrayList<CtfTmfLightweightContext>();
        rnd = new Random(System.nanoTime());
        fTrace = trace;
    }

    /**
     * This needs explaining: the iterator table is effectively a cache.
     * Originally the contexts had a 1 to 1 structure with the file handles of a
     * trace. This failed since there is a limit to how many file handles we can
     * have opened simultaneously. Then a round-robin scheme was implemented,
     * this lead up to a two competing contexts syncing up and using the same
     * file handler, causing horrible slowdowns. Now a random replacement
     * algorithm is selected. This is the same as used by arm processors, and it
     * works quite well when many cores so this looks promising for very
     * multi-threaded systems.
     *
     * @param context
     *            the context to look up
     * @return the iterator refering to the context
     */
    public CtfIterator getIterator(final CtfTmfLightweightContext context) {
        /*
         * if the element is in the map, we don't need to do anything else.
         */
        CtfIterator retVal = map.get(context);
        if (retVal == null) {
            /*
             * Assign an iterator to a context, this means we will need to seek
             * at the end.
             */
            if (randomAccess.size() < MAX_SIZE) {
                /*
                 * if we're not full yet, just add an element.
                 */
                retVal = new CtfIterator(fTrace);
                addElement(context, retVal);

            } else {
                /*
                 * if we're full, randomly replace an element
                 */
                retVal = replaceRandomElement(context);
            }
            retVal.seek((Long) context.getLocation().getLocation());
        }
        return retVal;
    }

    /**
     * Add a pair of context and element to the hashmap and the arraylist.
     *
     * @param context
     *            the context
     * @param elem
     *            the iterator
     */
    private void addElement(final CtfTmfLightweightContext context,
            final CtfIterator elem) {
        map.put(context, elem);
        randomAccess.add(context);
    }

    /**
     * Replace a random element
     *
     * @param context
     *            the context to swap in
     * @return the iterator of the removed elements.
     */
    private CtfIterator replaceRandomElement(
            final CtfTmfLightweightContext context) {
        /*
         * This needs some explanation too: We need to select a random victim
         * and remove it. The order of the elements is not important, so instead
         * of just calling arraylist.remove(element) which has an O(n)
         * complexity, we pick an random number. The element is swapped out of
         * the array and removed and replaced in the hashmap.
         */
        final int size = randomAccess.size();
        final int pos = rnd.nextInt(size);
        final CtfTmfLightweightContext victim = randomAccess.get(pos);
        randomAccess.set(pos, context);
        final CtfIterator elem = map.remove(victim);
        map.put(context, elem);
        return elem;
    }

}
