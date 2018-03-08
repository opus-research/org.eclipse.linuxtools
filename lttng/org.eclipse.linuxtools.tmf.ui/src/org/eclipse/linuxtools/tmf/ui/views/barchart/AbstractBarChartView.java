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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphRangeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphSelectionListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphCombo;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphRangeUpdateEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;

/**
 * An abstract view all bar chart views can inherit
 *
 * @since 2.0
 */
public abstract class AbstractBarChartView extends TmfView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * View ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.barchart.abstract"; //$NON-NLS-1$

    private final String[] fColumns;
    private final String[] fFilterColumns;

    /**
     * Redraw state enum
     */
    private enum State {
        IDLE, BUSY, PENDING
    }

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    // The timegraph combo
    private TimeGraphCombo fTimeGraphCombo;

    /**
     * The selected trace
     */
    protected ITmfTrace fTrace;

    // The timegraph entry list
    private ArrayList<BarChartEntry> fEntryList;

    /**
     * The trace to entry list hash map
     */
    protected final HashMap<ITmfTrace, ArrayList<BarChartEntry>> fEntryListMap = new HashMap<ITmfTrace, ArrayList<BarChartEntry>>();

    // The trace to build thread hash map
    final private HashMap<ITmfTrace, BuildThread> fBuildThreadMap = new HashMap<ITmfTrace, BuildThread>();

    /**
     * The start time
     */
    protected long fStartTime;

    /**
     * The end time
     */
    protected long fEndTime;

    /**
     * The display width
     */
    protected final int fDisplayWidth;

    // The zoom thread
    private ZoomThread fZoomThread;

    // The next resource action
    private Action fNextResourceAction;

    // The previous resource action
    private Action fPreviousResourceAction;

    // The relative weight of the left part of the graph combo
    private int fWeightLeft;
    /**
     * A comparator class
     */
    protected final BarChartEntryComparator fBarChartEntryComparator;

    // The redraw state used to prevent unnecessary queuing of display runnables
    private State fRedrawState = State.IDLE;

    // The redraw synchronization object
    final private Object fSyncObj = new Object();

    final private TimeGraphPresentationProvider fPresentation;

    /**
     * Text for the "next" button
     */
    protected String fNextText = Messages.AbstractBarChartView_NextText;
    /**
     * Tooltip for the "next" button
     */
    protected String fNextTooltip = Messages.AbstractBarChartView_NextTooltip;
    /**
     * Text for the "Previous" button
     */
    protected String fPrevText = Messages.AbstractBarChartView_PreviousText;
    /**
     * Tooltip for the "Previous" button
     */
    protected String fPrevTooltip = Messages.AbstractBarChartView_PreviousTooltip;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private class TreeContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return (ITimeGraphEntry[]) inputElement;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            ITimeGraphEntry entry = (ITimeGraphEntry) parentElement;
            List<? extends ITimeGraphEntry> children = entry.getChildren();
            return children.toArray(new ITimeGraphEntry[children.size()]);
        }

        @Override
        public Object getParent(Object element) {
            ITimeGraphEntry entry = (ITimeGraphEntry) element;
            return entry.getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
            ITimeGraphEntry entry = (ITimeGraphEntry) element;
            return entry.hasChildren();
        }

    }

    private class TreeLabelProvider implements ITableLabelProvider {

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            BarChartEntry entry = (BarChartEntry) element;
            return entry.getColumnText(columnIndex);
        }

    }

    /**
     * Comparator for bar chart entries
     */
    protected static class BarChartEntryComparator implements Comparator<ITimeGraphEntry> {

        @Override
        public int compare(ITimeGraphEntry o1, ITimeGraphEntry o2) {
            int result = 0;

            if ((o1 instanceof BarChartEntry) && (o2 instanceof BarChartEntry)) {
                BarChartEntry entry1 = (BarChartEntry) o1;
                BarChartEntry entry2 = (BarChartEntry) o2;
                result = entry1.getTrace().getStartTime().compareTo(entry2.getTrace().getStartTime());
                if (result == 0) {
                    result = entry1.getTrace().getName().compareTo(entry2.getTrace().getName());
                }
            }

            if (result == 0) {
                result = o1.getStartTime() < o2.getStartTime() ? -1 : o1.getStartTime() > o2.getStartTime() ? 1 : 0;
            }

            return result;
        }
    }

    private class BuildThread extends Thread {
        private final ITmfTrace fBuildTrace;
        private final IProgressMonitor fMonitor;

        public BuildThread(final ITmfTrace trace, final String name) {
            super(name + " build"); //$NON-NLS-1$
            fBuildTrace = trace;
            fMonitor = new NullProgressMonitor();
        }

        @Override
        public void run() {
            buildEventList(fBuildTrace, fMonitor);
            synchronized (fBuildThreadMap) {
                fBuildThreadMap.remove(this);
            }
        }

        public void cancel() {
            fMonitor.setCanceled(true);
        }
    }

    private class ZoomThread extends Thread {
        private final ArrayList<BarChartEntry> fZoomEntryList;
        private final long fZoomStartTime;
        private final long fZoomEndTime;
        private final long fResolution;
        private final IProgressMonitor fMonitor;

        public ZoomThread(ArrayList<BarChartEntry> entryList, long startTime, long endTime, String name) {
            super(name + " zoom"); //$NON-NLS-1$
            fZoomEntryList = entryList;
            fZoomStartTime = startTime;
            fZoomEndTime = endTime;
            fResolution = Math.max(1, (fZoomEndTime - fZoomStartTime) / fDisplayWidth);
            fMonitor = new NullProgressMonitor();
        }

        @Override
        public void run() {
            if (fZoomEntryList == null) {
                return;
            }
            for (BarChartEntry entry : fZoomEntryList) {
                if (fMonitor.isCanceled()) {
                    break;
                }
                zoom(entry, fMonitor);
            }
        }

        private void zoom(BarChartEntry entry, IProgressMonitor monitor) {
            if (fZoomStartTime <= fStartTime && fZoomEndTime >= fEndTime) {
                entry.setZoomedEventList(null);
            } else {
                List<ITimeEvent> zoomedEventList = getEventList(entry, fZoomStartTime, fZoomEndTime, fResolution, monitor);
                if (zoomedEventList != null) {
                    entry.setZoomedEventList(zoomedEventList);
                }
            }
            redraw();
            for (BarChartEntry child : entry.getChildren()) {
                if (fMonitor.isCanceled()) {
                    return;
                }
                zoom(child, monitor);
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
     *            The id of the view
     * @param cols
     *            The columns to display in the tree view on the left
     * @param filterCols
     *            The columns list to filter the view
     * @param pres
     *            The presentation provider
     * @param fComparator
     *            the entry comparator object
     * @param weightLeft
     *            The relative weight of the left part of the time graph combo
     */
    public AbstractBarChartView(String id, String[] cols, String[] filterCols,
            TimeGraphPresentationProvider pres, BarChartEntryComparator fComparator, int weightLeft) {
        super(id);
        fColumns = cols;
        fFilterColumns = filterCols;
        fPresentation = pres;
        fDisplayWidth = Display.getDefault().getBounds().width;
        fBarChartEntryComparator = fComparator;
        fWeightLeft = weightLeft;
    }

    /**
     * Constructor
     *
     * @param id
     *            The id of the view
     * @param cols
     *            The columns to display in the tree view on the left
     * @param filterCols
     *            The columns list to filter the view
     * @param pres
     *            The presentation provider
     * @param fComparator
     *            the entry comparator object
     */
    public AbstractBarChartView(String id, String[] cols, String[] filterCols,
            TimeGraphPresentationProvider pres, BarChartEntryComparator fComparator) {
        this(id, cols, filterCols, pres, fComparator, 50);
    }

    /**
     * Constructor
     *
     * @param id
     *            The id of the view
     * @param cols
     *            The columns to display in the tree view on the left
     * @param filterCols
     *            The columns list to filter the view
     * @param pres
     *            The presentation provider
     */
    public AbstractBarChartView(String id, String[] cols, String[] filterCols,
            TimeGraphPresentationProvider pres) {
        this(id, cols, filterCols, pres, new BarChartEntryComparator(), 50);
    }

    /**
     * Constructor
     *
     * @param id
     *            The id of the view
     * @param cols
     *            The columns to display in the tree view on the left
     * @param filterCols
     *            The columns list to filter the view
     * @param pres
     *            The presentation provider
     * @param weightLeft
     *            The relative weight of the left part of the time graph combo
     */
    public AbstractBarChartView(String id, String[] cols, String[] filterCols,
            TimeGraphPresentationProvider pres, int weightLeft) {
        this(id, cols, filterCols, pres, new BarChartEntryComparator(), weightLeft);
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {
        fTimeGraphCombo = new TimeGraphCombo(parent, SWT.NONE, fWeightLeft);

        fTimeGraphCombo.setTreeContentProvider(new TreeContentProvider());

        fTimeGraphCombo.setTreeLabelProvider(new TreeLabelProvider());

        fTimeGraphCombo.setTimeGraphProvider(fPresentation);

        fTimeGraphCombo.setTreeColumns(fColumns);

        fTimeGraphCombo.setFilterContentProvider(new TreeContentProvider());

        fTimeGraphCombo.setFilterLabelProvider(new TreeLabelProvider());

        fTimeGraphCombo.setFilterColumns(fFilterColumns);

        fTimeGraphCombo.getTimeGraphViewer().addRangeListener(new ITimeGraphRangeListener() {
            @Override
            public void timeRangeUpdated(TimeGraphRangeUpdateEvent event) {
                final long startTime = event.getStartTime();
                final long endTime = event.getEndTime();
                TmfTimeRange range = new TmfTimeRange(new CtfTmfTimestamp(startTime), new CtfTmfTimestamp(endTime));
                TmfTimestamp time = new CtfTmfTimestamp(fTimeGraphCombo.getTimeGraphViewer().getSelectedTime());
                broadcast(new TmfRangeSynchSignal(AbstractBarChartView.this, range, time));
                if (fZoomThread != null) {
                    fZoomThread.cancel();
                }
                startZoomThread(startTime, endTime);
            }
        });

        fTimeGraphCombo.getTimeGraphViewer().addTimeListener(new ITimeGraphTimeListener() {
            @Override
            public void timeSelected(TimeGraphTimeEvent event) {
                long time = event.getTime();
                broadcast(new TmfTimeSynchSignal(AbstractBarChartView.this, new CtfTmfTimestamp(time)));
            }
        });

        fTimeGraphCombo.addSelectionListener(new ITimeGraphSelectionListener() {
            @Override
            public void selectionChanged(TimeGraphSelectionEvent event) {
                // ITimeGraphEntry selection = event.getSelection();
            }
        });

        fTimeGraphCombo.getTimeGraphViewer().setTimeFormat(TimeFormat.CALENDAR);

        // View Action Handling
        makeActions();
        contributeToActionBars();

        ITmfTrace trace = getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }

        // make selection available to other views
        getSite().setSelectionProvider(fTimeGraphCombo.getTreeViewer());
    }

    @Override
    public void setFocus() {
        fTimeGraphCombo.setFocus();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the trace selected signal
     *
     * @param signal
     *            The signal that's received
     */
    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        if (signal.getTrace() == fTrace) {
            return;
        }
        fTrace = signal.getTrace();

        synchronized (fEntryListMap) {
            fEntryList = fEntryListMap.get(fTrace);
            if (fEntryList == null) {
                synchronized (fBuildThreadMap) {
                    BuildThread buildThread = new BuildThread(fTrace, this.getName());
                    fBuildThreadMap.put(fTrace, buildThread);
                    buildThread.start();
                }
            } else {
                fStartTime = fTrace.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                fEndTime = fTrace.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                refresh();
            }
        }
    }

    /**
     * Trace is closed: clear the data structures and the view
     *
     * @param signal
     *            the signal received
     */
    @TmfSignalHandler
    public void traceClosed(final TmfTraceClosedSignal signal) {
        synchronized (fBuildThreadMap) {
            BuildThread buildThread = fBuildThreadMap.remove(signal.getTrace());
            if (buildThread != null) {
                buildThread.cancel();
            }
        }
        synchronized (fEntryListMap) {
            fEntryListMap.remove(signal.getTrace());
        }
        if (signal.getTrace() == fTrace) {
            fTrace = null;
            fStartTime = 0;
            fEndTime = 0;
            if (fZoomThread != null) {
                fZoomThread.cancel();
            }
            refresh();
        }
    }

    /**
     * FIXME Do I need this? It was in the control flow view
     *
     * @param time
     *            the time
     * @return the selected element
     */
    protected abstract int getSelectedThread(long time);

    /**
     * Handler for the synch signal
     *
     * @param signal
     *            The signal that's received
     */
    @TmfSignalHandler
    public void synchToTime(final TmfTimeSynchSignal signal) {
        if (signal.getSource() == this || fTrace == null) {
            return;
        }
        final long time = signal.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        /* FIXME Do I need this? It was in the control flow view */
        // final int selectedThread = getSelectedThread(time);

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                fTimeGraphCombo.getTimeGraphViewer().setSelectedTime(time, true);
                startZoomThread(fTimeGraphCombo.getTimeGraphViewer().getTime0(), fTimeGraphCombo.getTimeGraphViewer().getTime1());

                // if (selectedThread > 0) { What is this for exactly?
                // for (Object element :
                // fTimeGraphCombo.getTimeGraphViewer().getExpandedElements()) {
                // if (element instanceof TimegraphEntry) {
                // TimegraphEntry entry = (TimegraphEntry) element;
                // if (entry.getThreadId() == selectedThread) {
                // fTimeGraphCombo.setSelection(entry);
                // break;
                // }
                // }
                // }
                // }
            }
        });
    }

    /**
     * Handler for the range sync signal
     *
     * @param signal
     *            The signal that's received
     */
    @TmfSignalHandler
    public void synchToRange(final TmfRangeSynchSignal signal) {
        if (signal.getSource() == this || fTrace == null) {
            return;
        }
        if (signal.getCurrentRange().getIntersection(fTrace.getTimeRange()) == null) {
            return;
        }
        final long startTime = signal.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        final long endTime = signal.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        final long time = signal.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                fTimeGraphCombo.getTimeGraphViewer().setStartFinishTime(startTime, endTime);
                fTimeGraphCombo.getTimeGraphViewer().setSelectedTime(time, false);
                startZoomThread(startTime, endTime);
            }
        });
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    /**
     * Build the entries list to show in this bar chart
     *
     * Called from the BuildThread
     *
     * @param trace
     *            The trace being built
     * @param monitor
     *            The progress monitor object
     */
    protected abstract void buildEventList(final ITmfTrace trace, IProgressMonitor monitor);

    /**
     * Gets the list of event for an entry in a given timerange
     *
     * @param entry
     *            The entry to get events for
     * @param startTime
     *            Start of the time range
     * @param endTime
     *            End of the time range
     * @param resolution
     *            The resolution
     * @param monitor
     *            The progress monitor object
     * @return The list of events for the entry
     */
    protected abstract List<ITimeEvent> getEventList(BarChartEntry entry,
            long startTime, long endTime, long resolution,
            IProgressMonitor monitor);

    /**
     * Refresh the display
     */
    protected void refresh() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                ITimeGraphEntry[] entries = null;
                synchronized (fEntryListMap) {
                    fEntryList = fEntryListMap.get(fTrace);
                    if (fEntryList == null) {
                        fEntryList = new ArrayList<BarChartEntry>();
                    }
                    entries = fEntryList.toArray(new ITimeGraphEntry[0]);
                }
                Arrays.sort(entries, fBarChartEntryComparator);
                fTimeGraphCombo.setInput(entries);
                fTimeGraphCombo.getTimeGraphViewer().setTimeBounds(fStartTime, fEndTime);

                long timestamp = fTrace == null ? 0 : fTrace.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long startTime = fTrace == null ? 0 : fTrace.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long endTime = fTrace == null ? 0 : fTrace.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                startTime = Math.max(startTime, fStartTime);
                endTime = Math.min(endTime, fEndTime);
                fTimeGraphCombo.getTimeGraphViewer().setSelectedTime(timestamp, false);
                fTimeGraphCombo.getTimeGraphViewer().setStartFinishTime(startTime, endTime);

                for (TreeColumn column : fTimeGraphCombo.getTreeViewer().getTree().getColumns()) {
                    column.pack();
                }

                startZoomThread(startTime, endTime);
            }
        });
    }

    /**
     * Redraw the canvas
     */
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
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                fTimeGraphCombo.redraw();
                fTimeGraphCombo.update();
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
        fZoomThread = new ZoomThread(fEntryList, startTime, endTime, getName());
        fZoomThread.start();
    }

    private void makeActions() {
        fPreviousResourceAction = fTimeGraphCombo.getTimeGraphViewer().getPreviousItemAction();
        fPreviousResourceAction.setText(fPrevText);
        fPreviousResourceAction.setToolTipText(fPrevTooltip);
        fNextResourceAction = fTimeGraphCombo.getTimeGraphViewer().getNextItemAction();
        fNextResourceAction.setText(fNextText);
        fNextResourceAction.setToolTipText(fNextTooltip);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(fTimeGraphCombo.getShowFilterAction());
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getShowLegendAction());
        manager.add(new Separator());
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getResetScaleAction());
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getPreviousEventAction());
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getNextEventAction());
        manager.add(fPreviousResourceAction);
        manager.add(fNextResourceAction);
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getZoomInAction());
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getZoomOutAction());
        manager.add(new Separator());
    }
}
