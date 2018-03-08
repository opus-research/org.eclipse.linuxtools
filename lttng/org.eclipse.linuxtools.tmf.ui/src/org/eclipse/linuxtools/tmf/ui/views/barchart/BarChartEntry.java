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

package org.eclipse.linuxtools.tmf.ui.views.barchart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * An entry in the Control Flow view
 *
 * @since 2.0
 */
public class BarChartEntry implements ITimeGraphEntry {

    private final int fQuark;
    private final ITmfTrace fTrace;
    /**
     * Entry's parent
     */
    protected BarChartEntry fParent = null;
    /**
     * List of child entries
     */
    protected final List<BarChartEntry> fChildren = new ArrayList<BarChartEntry>();
    /**
     * Name of this entry (text to show)
     */
    protected String fName;
    private long fStartTime = -1;
    private long fEndTime = -1;
    private List<ITimeEvent> fEventList = new ArrayList<ITimeEvent>();
    private List<ITimeEvent> fZoomedEventList = null;

    /**
     * Constructor
     *
     * @param quark
     *            The quark of the state system attribute whose state is shown
     *            on this row
     * @param trace
     *            The trace on which we are working
     * @param name
     *            The exec_name of this entry
     * @param startTime
     *            The start time of this process's lifetime
     * @param endTime
     *            The end time of this process
     */
    public BarChartEntry(int quark, ITmfTrace trace, String name, long startTime, long endTime) {
        fQuark = quark;
        fTrace = trace;
        fName = name;
        fStartTime = startTime;
        fEndTime = endTime;
    }

    @Override
    public ITimeGraphEntry getParent() {
        return fParent;
    }

    @Override
    public boolean hasChildren() {
        return fChildren.size() > 0;
    }

    @Override
    public List<BarChartEntry> getChildren() {
        return fChildren;
    }

    @Override
    public String getName() {
        return fName;
    }

    /**
     * Update the entry name
     *
     * @param execName
     *            the updated entry name
     */
    public void setName(String execName) {
        fName = execName;
    }

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    @Override
    public long getEndTime() {
        return fEndTime;
    }

    @Override
    public boolean hasTimeEvents() {
        return true;
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator() {
        if (hasTimeEvents()) {
            return new EventIterator(fEventList, fZoomedEventList);
        }
        return null;
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
        return new EventIterator(fEventList, fZoomedEventList, startTime, stopTime);
    }

    /**
     * Get the quark of the attribute matching this thread's TID
     *
     * @return The quark
     */
    public int getQuark() {
        return fQuark;
    }

    /**
     * Get the CTF trace object
     *
     * @return The trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Add an event to this process's timeline
     *
     * @param event
     *            The time event
     */
    public void addEvent(ITimeEvent event) {
        long start = event.getTime();
        long end = start + event.getDuration();
        synchronized (fEventList) {
            fEventList.add(event);
            if (fStartTime == -1 || start < fStartTime) {
                fStartTime = start;
            }
            if (fEndTime == -1 || end > fEndTime) {
                fEndTime = end;
            }
        }
    }

    /**
     * Set the general event list of this entry
     *
     * @param eventList
     *            The list of time events
     */
    public void setEventList(List<ITimeEvent> eventList) {
        fEventList = eventList;
    }

    /**
     * Set the zoomed event list of this entry
     *
     * @param eventList
     *            The list of time events
     */
    public void setZoomedEventList(List<ITimeEvent> eventList) {
        fZoomedEventList = eventList;
    }

    /**
     * Add a child entry to this one (to show relationships between processes as
     * a tree)
     *
     * @param child
     *            The child entry
     */
    public void addChild(BarChartEntry child) {
        child.fParent = this;
        fChildren.add(child);
    }

    /**
     * Get the text to display in a given column for this entry
     *
     * @param columnIndex
     *            The index of the column to get text from
     * @return The text this entry displays for this column
     */
    public String getColumnText(final int columnIndex) {
        if (columnIndex == 0) {
            return getName();
        }
        return ""; //$NON-NLS-1$
    }
}
