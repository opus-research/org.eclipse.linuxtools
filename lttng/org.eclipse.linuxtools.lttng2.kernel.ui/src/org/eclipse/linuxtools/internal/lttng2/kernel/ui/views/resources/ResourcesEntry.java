/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for bar charts
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import org.eclipse.linuxtools.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.linuxtools.tmf.ui.views.barchart.BarChartEntry;

/**
 * An entry, or row, in the resource view
 *
 * @author Patrick Tasse
 */
public class ResourcesEntry extends BarChartEntry {

    /** Type of resource */
    public static enum Type {
        /** Null resources (filler rows, etc.) */
        NULL,
        /** Entries for CPUs */
        CPU,
        /** Entries for IRQs */
        IRQ,
        /** Entries for Soft IRQ */
        SOFT_IRQ
    }

    private final int fId;
    private final Type fType;

    /**
     * Constructor
     *
     * @param quark
     *            The attribute quark matching the entry
     * @param trace
     *            The trace on which we are working
     * @param name
     *            The exec_name of this entry
     * @param startTime
     *            The start time of this entry lifetime
     * @param endTime
     *            The end time of this entry
     * @param type
     *            The type of this entry
     * @param id
     *            The id of this entry
     */
    public ResourcesEntry(int quark, LttngKernelTrace trace, String name, long startTime, long endTime, Type type, int id) {
        super(quark, trace, name, startTime, endTime);
        fId = id;
        fType = type;
    }

    /**
     * Constructor
     *
     * @param trace
     *            The trace on which we are working
     * @param name
     *            The exec_name of this entry
     * @param startTime
     *            The start time of this entry lifetime
     * @param endTime
     *            The end time of this entry
     * @param id
     *            The id of this entry
     */
    public ResourcesEntry(LttngKernelTrace trace, String name, long startTime, long endTime, int id) {
        this(-1, trace, name, startTime, endTime, Type.NULL, id);
    }

    /**
     * Constructor
     *
     * @param quark
     *            The attribute quark matching the entry
     * @param trace
     *            The trace on which we are working
     * @param startTime
     *            The start time of this entry lifetime
     * @param endTime
     *            The end time of this entry
     * @param type
     *            The type of this entry
     * @param id
     *            The id of this entry
     */
    public ResourcesEntry(int quark, LttngKernelTrace trace, long startTime, long endTime, Type type, int id) {
        this(quark, trace, type.toString() + " " + id, startTime, endTime, type, id); //$NON-NLS-1$
    }

    /**
     * Get the entry's id
     *
     * @return the entry's id
     */
    public int getId() {
        return fId;
    }

    @Override
    public LttngKernelTrace getTrace() {
        return (LttngKernelTrace) super.getTrace();
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
     * Add a child to this entry of type ResourcesEntry
     *
     * @param entry
     *            The entry to add
     */
    public void addChild(ResourcesEntry entry) {
        int index;
        for (index = 0; index < fChildren.size(); index++) {
            ResourcesEntry other = (ResourcesEntry) fChildren.get(index);
            if (entry.getType().compareTo(other.getType()) < 0) {
                break;
            } else if (entry.getType().equals(other.getType())) {
                if (entry.getId() < other.getId()) {
                    break;
                }
            }
        }

        fParent = this;
        fChildren.add(index, entry);
    }

}
