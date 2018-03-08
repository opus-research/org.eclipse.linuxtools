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
 *
 */
public abstract class CtfIteratorManager {
    private static HashMap<CtfTmfTrace, CtfTraceManager> map = null;

    /**
     * Initialisation of the manager, if it's not called, you will
     */
    public static synchronized void init() {
        if (map == null) {
            map = new HashMap<CtfTmfTrace, CtfTraceManager>();
        }
    }

    /**
     * registers a trace to the iterator manager, the trace can now get
     * iterators.
     *
     * @param trace
     *            the trace to register.
     */
    public static synchronized void addTrace(CtfTmfTrace trace) {
        map.put(trace, new CtfTraceManager(trace));
    }

    /**
     * Removes a trace to the iterator manager.
     *
     * @param trace
     *            the trace to register.
     */
    public static synchronized void removeTrace(CtfTmfTrace trace) {
        map.remove(trace);
    }

    /**
     * Get an iterator for a given trace and context.
     * @param trace the trace
     * @param ctx the context
     * @return the iterator
     */
    public static synchronized CtfIterator getIterator(CtfTmfTrace trace,
            CtfTmfLightweightContext ctx) {
        return map.get(trace).getIterator(ctx);
    }
}

/**
 * A trace manager
 *
 * @author Matthew Khouzam
 */
class CtfTraceManager {
    private final int MAX_SIZE = 100;
    private final HashMap<CtfTmfLightweightContext, CtfIterator> map;
    private final ArrayList<CtfTmfLightweightContext> randomAccess;
    private final CtfTmfTrace fTrace;
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
     * works quite well when many cores so this looks promising for
     * multi-multi-threaded systems.
     *
     * @param context
     * @return
     */
    synchronized public CtfIterator getIterator(CtfTmfLightweightContext context) {
        CtfIterator retVal = map.get(context);
        if (retVal == null) {
            final int size = randomAccess.size();
            if (size < MAX_SIZE) {
                final CtfIterator elem = new CtfIterator(fTrace);
                addElement(context, elem);
                retVal = elem;
            } else {
                CtfIterator elem = removeRandomElement(size);
                addElement(context, elem);
                retVal = elem;
            }
            retVal.seek((Long) context.getLocation().getLocation());
        }
        return retVal;
    }

    /**
     * @param context
     * @param elem
     */
    private void addElement(CtfTmfLightweightContext context, CtfIterator elem) {
        map.put(context, elem);
        randomAccess.add(context);
    }

    /**
     * @param size
     * @return
     */
    private CtfIterator removeRandomElement(final int size) {
        final int pos = rnd.nextInt(size);
        CtfTmfLightweightContext victim = randomAccess.get(pos);

        final int index = size - 1;
        if (index != pos) {
            randomAccess.set(pos, randomAccess.get(index));
        }
        randomAccess.remove(index);
        CtfIterator elem = map.get(victim);
        map.remove(victim);
        return elem;
    }

}
