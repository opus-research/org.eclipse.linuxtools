package org.eclipse.linuxtools.tmf.ui.views.timegraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public abstract class AbstractTimeGraphEntry implements ITimeGraphEntry {

    private final int fQuark;
    private final ITmfTrace fTrace;
    private final List<AbstractTimeGraphEntry> children = new ArrayList<AbstractTimeGraphEntry>();
    private final String fName;
    private ITimeGraphEntry fParent = null;

    private long fStartTime;
    private long fEndTime;

    private List<ITimeEvent> fEventList = new ArrayList<ITimeEvent>();
    private List<ITimeEvent> fZoomedEventList = null;


    protected AbstractTimeGraphEntry(int quark, ITmfTrace trace, String name) {
        fQuark = quark;
        fTrace = trace;
        fName = name;
    }

    @Override
    public ITimeGraphEntry getParent() {
        return fParent;
    }

    @Override
    public boolean hasChildren() {
        return children != null && children.size() > 0;
    }

    @Override
    public List<AbstractTimeGraphEntry> getChildren() {
        return children;
    }

    /**
     * Add a child entry to this entry.
     *
     * @param child
     *            The child entry to add
     */
    public void addChild(AbstractTimeGraphEntry child) {
        child.setParent(this);
        children.add(child);
    }

    @Override
    public String getName() {
        return fName;
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

    /**
     * Assign a parent entry to this one, to organize them in a tree in the
     * view.
     *
     * @param parent
     *            The parent entry
     */
    public void setParent(ITimeGraphEntry parent) {
        fParent = parent;
    }

    /**
     * Retrieve the attribute quark that's represented by this entry.
     *
     * @return The integer quark
     */
    public int getQuark() {
        return fQuark;
    }

    /**
     * Retrieve the trace that is associated to this Resource view.
     *
     * @return The LTTng 2 kernel trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Add an event to the current event list
     *
     * @param timeEvent
     *          The event
     */
    public void addEvent(ITimeEvent timeEvent) {
        fEventList.add(timeEvent);
    }

    /**
     * Assign the target event list to this view.
     *
     * @param eventList
     *            The list of time events
     */
    public void setEventList(List<ITimeEvent> eventList) {
        fEventList = eventList;
        if (eventList != null && eventList.size() > 0) {
            fStartTime = eventList.get(0).getTime();
            ITimeEvent lastEvent = eventList.get(eventList.size() - 1);
            fEndTime = lastEvent.getTime() + lastEvent.getDuration();
        }
    }

    /**
     * Assign the zoomed event list to this view.
     *
     * @param eventList
     *            The list of "zoomed" time events
     */
    public void setZoomedEventList(List<ITimeEvent> eventList) {
        fZoomedEventList = eventList;
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator() {
        return new EventIterator(fEventList, fZoomedEventList);
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator(long startTime,
            long stopTime, long visibleDuration) {
        return new EventIterator(fEventList, fZoomedEventList, startTime, stopTime);
    }
}
