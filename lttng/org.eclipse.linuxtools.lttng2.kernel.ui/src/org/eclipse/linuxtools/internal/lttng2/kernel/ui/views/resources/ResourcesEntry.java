/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import org.eclipse.linuxtools.lttng2.kernel.core.trace.CtfKernelTrace;
import org.eclipse.linuxtools.tmf.ui.views.timegraph.AbstractTimeGraphEntry;

/**
 * An entry, or row, in the resource view
 *
 * @author Patrick Tasse
 */
public class ResourcesEntry extends AbstractTimeGraphEntry {

    /** Type of resource */
    public static enum Type {
        /** Null resources (filler rows, etc.) */
        NULL,
        /** Entries for CPUs */
        CPU,
        /** Entries for IRQs */
        IRQ,
        /** Entries for Soft IRQ */
        SOFT_IRQ }


    private final Type fType;
    private final int fId;

    /**
     * Standard constructor
     *
     * @param quark
     *            The quark of the state system attribute whose state is shown
     *            on this row
     * @param trace
     *            The trace that this view is talking about
     * @param type
     *            Type of entry, see the Type enum
     * @param id
     *            The integer id associated with this entry or row
     */
    public ResourcesEntry(int quark, CtfKernelTrace trace, Type type, int id) {
        super(quark, trace, type.toString() + ' ' + Integer.toString(id));
        fType = type;
        fId = id;
    }

    /**
     * Get the entry Type of this entry. Uses the inner Type enum.
     *
     * @return The entry type
     */
    public Type getType() {
        return fType;
    }

    /**
     * Get the integer ID associated with this entry.
     *
     * @return The ID
     */
    public int getId() {
        return fId;
    }

    @Override
    public CtfKernelTrace getTrace() {
        /*
         * The fTrace field is final, and the constructor requires a
         * CtfKernelTrace type, so this cast should always be safe.
         */
        return (CtfKernelTrace) super.getTrace();
    }

}
