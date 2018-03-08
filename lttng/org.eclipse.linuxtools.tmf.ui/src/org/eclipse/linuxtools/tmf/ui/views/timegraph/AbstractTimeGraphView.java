/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.timegraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfStateSystemBuildCompleted;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphRangeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphRangeUpdateEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;

/**
 * @param <E> Type of TimeGraph entries used in this view
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public abstract class AbstractTimeGraphView<E extends AbstractTimeGraphEntry> extends TmfView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Redraw state enum
     */
    private enum State { IDLE, BUSY, PENDING }


    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    // The time graph viewer
    private TimeGraphViewer fTimeGraphViewer;

    // The time graph entry list
    protected ArrayList<TraceEntry> fEntryList;

    // The time graph entry list synchronization object
    protected final Object fEntryListSyncObj = new Object();

    // The display width
    protected final int fDisplayWidth;

    // The next resource action
    private Action fNextDeviceAction;

    // The previous resource action
    private Action fPreviousDeviceAction;

    // The zoom thread
    private ZoomThread fZoomThread;

    // The redraw state used to prevent unnecessary queuing of display runnables
    private State fRedrawState = State.IDLE;

    // The redraw synchronization object
    protected final Object fSyncObj = new Object();

    // The start time
    protected long fStartTime;

    // The end time
    protected long fEndTime;

    // The selected experiment
    protected TmfExperiment fSelectedExperiment;

    // ------------------------------------------------------------------------
    // Inner classes
    // ------------------------------------------------------------------------

    protected class TraceEntry implements ITimeGraphEntry {
        // The Trace
        private final ITmfTrace fTrace;
        // The start time
        private final long fTraceStartTime;
        // The end time
        private final long fTraceEndTime;
        // The children of the entry
        private final ArrayList<E> fChildren;
        // The name of entry
        private final String fName;

        public TraceEntry(ITmfTrace trace, String name, long startTime, long endTime) {
            fTrace = trace;
            fChildren = new ArrayList<E>();
            fName = name;
            fTraceStartTime = startTime;
            fTraceEndTime = endTime;
        }

        @Override
        public ITimeGraphEntry getParent() {
            return null;
        }

        @Override
        public boolean hasChildren() {
            return fChildren != null && fChildren.size() > 0;
        }

        @Override
        public List<E> getChildren() {
            return fChildren;
        }

        @Override
        public String getName() {
            return fName;
        }

        @Override
        public long getStartTime() {
            return fTraceStartTime;
        }

        @Override
        public long getEndTime() {
            return fTraceEndTime;
        }

        @Override
        public boolean hasTimeEvents() {
            return false;
        }

        @Override
        public Iterator<ITimeEvent> getTimeEventsIterator() {
            return null;
        }

        @Override
        public <T extends ITimeEvent> Iterator<T> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
            return null;
        }

        public ITmfTrace getTrace() {
            return fTrace;
        }

        public void addChild(E entry1) {
            int index;
            for (index = 0; index < fChildren.size(); index++ ) {
                ITimeGraphEntry other = fChildren.get(index);
                if (entry1.getName().compareTo(other.getName()) < 0) {
                    break;
                }
            }
            entry1.setParent(this);
            fChildren.add(index, entry1);
        }
    }

    private static class TraceEntryComparator implements Comparator<ITimeGraphEntry> {
        @Override
        public int compare(ITimeGraphEntry o1, ITimeGraphEntry o2) {
            int result = o1.getStartTime() < o2.getStartTime() ? -1 : o1.getStartTime() > o2.getStartTime() ? 1 : 0;
            if (result == 0) {
                result = o1.getName().compareTo(o2.getName());
            }
            return result;
        }
    }

    private class ZoomThread extends Thread {
        private final long fZoomStartTime;
        private final long fZoomEndTime;
        private final IProgressMonitor fMonitor;

        public ZoomThread(long startTime, long endTime) {
            super(viewName() + "View zoom"); //$NON-NLS-1$
            fZoomStartTime = startTime;
            fZoomEndTime = endTime;
            fMonitor = new NullProgressMonitor();
        }

        @Override
        public void run() {
            ArrayList<TraceEntry> entryList = null;
            synchronized (fEntryListSyncObj) {
                entryList = fEntryList;
            }
            if (entryList == null) {
                return;
            }
            long resolution = Math.max(1, (fZoomEndTime - fZoomStartTime) / fDisplayWidth);
            for (TraceEntry traceEntry : entryList) {
                for (E child : traceEntry.getChildren()) {
                    if (fMonitor.isCanceled()) {
                        break;
                    }
                    E entry = child;
                    if (fZoomStartTime <= fStartTime && fZoomEndTime >= fEndTime) {
                        entry.setZoomedEventList(null);
                    } else {
                        List<ITimeEvent> zoomedEventList = getEventList(entry,
                                fZoomStartTime, fZoomEndTime, resolution, false,
                                fMonitor);
                        if (zoomedEventList != null) {
                            entry.setZoomedEventList(zoomedEventList);
                        }
                    }
                    redraw();
                }
            }
        }

        public void cancel() {
            fMonitor.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param id
     *            The ID of the view to be created
     */
    public AbstractTimeGraphView(String id) {
        super(id);
        fDisplayWidth = Display.getDefault().getBounds().width;
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.TmfView#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        fTimeGraphViewer = new TimeGraphViewer(parent, SWT.NONE);

        fTimeGraphViewer.setTimeGraphProvider(getNewPresentationProvider());

        fTimeGraphViewer.addRangeListener(new ITimeGraphRangeListener() {
            @Override
            public void timeRangeUpdated(TimeGraphRangeUpdateEvent event) {
                long startTime = event.getStartTime();
                long endTime = event.getEndTime();
                TmfTimeRange range = new TmfTimeRange(getNewTimestamp(startTime), getNewTimestamp(endTime));
                ITmfTimestamp time = getNewTimestamp(fTimeGraphViewer.getSelectedTime());
                broadcast(new TmfRangeSynchSignal(this, range, time));
                startZoomThread(startTime, endTime);
            }
        });

        fTimeGraphViewer.addTimeListener(new ITimeGraphTimeListener() {
            @Override
            public void timeSelected(TimeGraphTimeEvent event) {
                long time = event.getTime();
                broadcast(new TmfTimeSynchSignal(this, getNewTimestamp(time)));
            }
        });

        final Thread thread = new Thread(viewName() + "View build") { //$NON-NLS-1$
            @Override
            public void run() {
                if (TmfExperiment.getCurrentExperiment() != null) {
                    selectExperiment(TmfExperiment.getCurrentExperiment());
                }
            }
        };
        thread.start();

        // View Action Handling
        makeActions();
        contributeToActionBars();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        fTimeGraphViewer.setFocus();
    }

    // ------------------------------------------------------------------------
    // UI methods
    // ------------------------------------------------------------------------

    protected void refresh() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphViewer.getControl().isDisposed()) {
                    return;
                }
                ITimeGraphEntry[] entries = null;
                synchronized (fEntryListSyncObj) {
                    entries = fEntryList.toArray(new ITimeGraphEntry[0]);
                }
                Arrays.sort(entries, new TraceEntryComparator());
                fTimeGraphViewer.setInput(entries);
                for (ITimeGraphEntry entry : entries) {
                    for (ITimeGraphEntry child : entry.getChildren()) {
                        fTimeGraphViewer.setExpandedState(child, false);
                    }
                }
                fTimeGraphViewer.setTimeBounds(fStartTime, fEndTime);
                fTimeGraphViewer.setStartFinishTime(fStartTime, fEndTime);

                startZoomThread(fStartTime, fEndTime);
            }
        });
    }

    protected void refresh(final long windowRange) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphViewer.getControl().isDisposed()) {
                    return;
                }
                ITimeGraphEntry[] entries = null;
                synchronized (fEntryListSyncObj) {
                    entries = fEntryList.toArray(new ITimeGraphEntry[0]);
                }
                Arrays.sort(entries, new TraceEntryComparator());
                fTimeGraphViewer.setInput(entries);
                fTimeGraphViewer.setTimeBounds(fStartTime, fEndTime);

                long endTime = fStartTime + windowRange;

                if (fEndTime < endTime) {
                    endTime = fEndTime;
                }
                fTimeGraphViewer.setStartFinishTime(fStartTime, endTime);

                startZoomThread(fStartTime, endTime);
            }
        });
    }

    protected void refresh(final boolean updateTimeBounds) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (fTimeGraphViewer.getControl().isDisposed()) {
                        return;
                    }
                    ITimeGraphEntry[] entries = null;
                    synchronized (fEntryListSyncObj) {
                        entries = fEntryList.toArray(new ITimeGraphEntry[0]);
                    }
                    Arrays.sort(entries, new TraceEntryComparator());
                    fTimeGraphViewer.setInput(entries);
                    for (ITimeGraphEntry entry : entries) {
                        for (ITimeGraphEntry child : entry.getChildren()) {
                            fTimeGraphViewer.setExpandedState(child, false);
                        }
                    }
                    if (updateTimeBounds) {
                        fTimeGraphViewer.setTimeBounds(fStartTime, fEndTime);
                        fTimeGraphViewer.setStartFinishTime(fStartTime, fEndTime);
                    }

                    startZoomThread(fStartTime, fEndTime);
                }
            });
        }

    protected void redraw() {
        synchronized (fSyncObj) {
            if (fRedrawState == State.IDLE) {
                fRedrawState = State.BUSY;
            } else {
                fRedrawState = State.PENDING;
                return;
            }
        }
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphViewer.getControl().isDisposed()) {
                    return;
                }
                fTimeGraphViewer.getControl().redraw();
                fTimeGraphViewer.getControl().update();
                synchronized (fSyncObj) {
                    if (fRedrawState == State.PENDING) {
                        fRedrawState = State.IDLE;
                        redraw();
                    } else {
                        fRedrawState = State.IDLE;
                    }
                }
            }
        });
    }

    private void startZoomThread(long startTime, long endTime) {
        if (fZoomThread != null) {
            fZoomThread.cancel();
        }
        fZoomThread = new ZoomThread(startTime, endTime);
        fZoomThread.start();
    }

    private void makeActions() {
        fPreviousDeviceAction = fTimeGraphViewer.getPreviousItemAction();
        fPreviousDeviceAction.setText("Previous Entry"); //$NON-NLS-1$
        fPreviousDeviceAction.setToolTipText("Select Previous Entry"); //$NON-NLS-1$
        fNextDeviceAction = fTimeGraphViewer.getNextItemAction();
        fNextDeviceAction.setText("Next Entry"); //$NON-NLS-1$
        fNextDeviceAction.setToolTipText("Select Next Entry"); //$NON-NLS-1$
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(fTimeGraphViewer.getShowLegendAction());
        manager.add(new Separator());
        manager.add(fTimeGraphViewer.getResetScaleAction());
        manager.add(fTimeGraphViewer.getPreviousEventAction());
        manager.add(fTimeGraphViewer.getNextEventAction());
        manager.add(fPreviousDeviceAction);
        manager.add(fNextDeviceAction);
        manager.add(fTimeGraphViewer.getZoomInAction());
        manager.add(fTimeGraphViewer.getZoomOutAction());
        manager.add(new Separator());
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the ExperimentSelected signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void experimentSelected(final TmfExperimentSelectedSignal signal) {
        if (signal.getExperiment().equals(fSelectedExperiment)) {
            return;
        }

        final Thread thread = new Thread(viewName() + "View build") { //$NON-NLS-1$
            @Override
            public void run() {
                selectExperiment(signal.getExperiment());
            }
        };
        thread.start();
    }

    /**
     * Handler for the TimeSynch signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void synchToTime(final TmfTimeSynchSignal signal) {
        if (signal.getSource() == this || fSelectedExperiment == null) {
            return;
        }
        final long time = signal.getCurrentTime().normalize(0, -9).getValue();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphViewer.getControl().isDisposed()) {
                    return;
                }
                fTimeGraphViewer.setSelectedTime(time, true);
                startZoomThread(fTimeGraphViewer.getTime0(), fTimeGraphViewer.getTime1());
            }
        });
    }

    /**
     * Handler for the RangeSynch signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void synchToRange(final TmfRangeSynchSignal signal) {
        if (signal.getSource() == this || fSelectedExperiment == null) {
            return;
        }
        final long startTime = signal.getCurrentRange().getStartTime().normalize(0, -9).getValue();
        final long endTime = signal.getCurrentRange().getEndTime().normalize(0, -9).getValue();
        final long time = signal.getCurrentTime().normalize(0, -9).getValue();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphViewer.getControl().isDisposed()) {
                    return;
                }
                fTimeGraphViewer.setStartFinishTime(startTime, endTime);
                fTimeGraphViewer.setSelectedTime(time, false);
                startZoomThread(startTime, endTime);
            }
        });
    }

    /**
     * Handler for the StatesystemBuildCompleted signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void stateSystemBuildCompleted (final TmfStateSystemBuildCompleted signal) {
        final TmfExperiment selectedExperiment = fSelectedExperiment;
        if (selectedExperiment == null || selectedExperiment.getTraces() == null) {
            return;
        }
        for (ITmfTrace trace : selectedExperiment.getTraces()) {
            if (trace == signal.getTrace() && getTraceType().isInstance(trace)) {
                final Thread thread = new Thread(viewName() + "View build") { //$NON-NLS-1$
                    @Override
                    public void run() {
                        // rebuild the model
                        selectExperiment(selectedExperiment);
                    }
                };
                thread.start();
            }
        }
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    protected abstract String viewName();

    protected abstract Class<? extends ITmfTrace> getTraceType();

    protected abstract ITmfTimestamp getNewTimestamp(long ts);

    protected abstract ITimeGraphPresentationProvider getNewPresentationProvider();

    protected abstract void selectExperiment(TmfExperiment experiment);

    protected abstract List<ITimeEvent> getEventList(E entry,
            long fZoomStartTime, long fZoomEndTime, long resolution,
            boolean includeNull, IProgressMonitor fMonitor);

}
