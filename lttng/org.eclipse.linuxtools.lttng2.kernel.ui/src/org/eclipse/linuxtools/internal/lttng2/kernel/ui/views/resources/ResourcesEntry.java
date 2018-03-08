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
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.barchart.BarChartEntry;

/**
 * An entry, or row, in the resource view
 *
 * @author Patrick Tasse
 */
public class ResourcesEntry extends BarChartEntry {

    private final int fId;

    /**
     * Constructor
     *
     * @param quark
     *            The attribute quark matching the thread
     * @param trace
     *            The trace on which we are working
     * @param name
     *            The exec_name of this entry
     * @param startTime
     *            The start time of this process's lifetime
     * @param endTime
     *            The end time of this process
     * @param id
     *            The id of this entry
     */
    public ResourcesEntry(int quark, ITmfTrace trace, String name, long startTime, long endTime, int id) {
        super(quark, trace, name, startTime, endTime);
        fId = id;
    }

    /**
     * Get the entry's id
     *
     * @return the entry's id
     */
    public int getId() {
        return fId;
    }

    /**
     * Add a child to this entry of type ResourcesEntry
     *
     * @param entry
     *            The entry to add
     */
    public void addChild(ResourcesEntry entry) {
        int index;
        for (index = 0; index < getChildren().size(); index++) {
            ResourcesEntry other = (ResourcesEntry) getChildren().get(index);
            if (entry.getClass().getName().compareTo(other.getClass().getName()) < 0) {
                break;
            } else if (entry.getClass().getName().equals(other.getClass().getName())) {
                if (entry.getId() < other.getId()) {
                    break;
                }
            }
        }
        fParent = this;
        fChildren.add(index, entry);
    }
}

final class ResourcesEntryNull extends ResourcesEntry {

    public ResourcesEntryNull(LttngKernelTrace trace, String name, long startTime, long endTime, int id) {
        super(0, trace, name, startTime, endTime, id);
    }

    @Override
    public boolean hasTimeEvents() {
        return false;
    }

}

final class ResourcesEntryCpu extends ResourcesEntry {

    public ResourcesEntryCpu(int quark, LttngKernelTrace trace, long startTime, long endTime, int id) {
        super(quark, trace, "CPU " + Integer.toString(id), startTime, endTime, id); //$NON-NLS-1$
    }

}

final class ResourcesEntryIrq extends ResourcesEntry {

    public ResourcesEntryIrq(int quark, ITmfTrace trace, long startTime, long endTime, int id) {
        super(quark, trace, "IRQ " + Integer.toString(id), startTime, endTime, id); //$NON-NLS-1$
    }

}

final class ResourcesEntrySoftirq extends ResourcesEntry {

    public ResourcesEntrySoftirq(int quark, ITmfTrace trace, long startTime, long endTime, int id) {
        super(quark, trace, "SOFT_IRQ " + Integer.toString(id), startTime, endTime, id); //$NON-NLS-1$
    }

}
