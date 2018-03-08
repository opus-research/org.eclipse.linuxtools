/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis (mathieu.denis@polymtl.ca) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.viewers.TmfViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.ITmfColumnDataProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnData;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnDataProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfTreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * <b><u>TmfStatisticsViewer</u></b>
 *
 * The viewer used to display the statistics tree in the statistics view.
 */
public class TmfStatisticsViewer extends TmfViewer implements ITmfStatisticsViewer {
    /**
     * The actual tree viewer to display
     */
    protected TreeViewer fTreeViewer;
    /**
     * The statistics tree linked to this viewer
     */
    private AbsTmfStatisticsTree fStatisticsData;
    /**
     * The trace that is displayed by this viewer
     */
    private ITmfTrace<ITmfEvent> fTrace;
    /**
     * Refresh frequency
     */
    protected static final Long STATS_INPUT_CHANGED_REFRESH = 5000L;
    /**
     * Object to store the cursor while waiting for the experiment to load
     */
    private Cursor fWaitCursor = null;
    /**
     * View instance counter (for multiple statistic views)
     */
    private static int fCountInstance = 0;
    /**
     * Number of this instance. Used as an instance ID.
     */
    private int fInstanceNb;

    /**
     * Empty constructor. To be used in conjunction with {@link TmfStatisticsViewer#init(Composite, String, ITmfTrace)}
     */
    public TmfStatisticsViewer() {
        super();
    }

    /**
     * Create a basic statistics viewer. To be used in conjunction with
     * {@link TmfStatisticsViewer#init(Composite, String, ITmfTrace)}
     *
     * @param parent The parent composite that will hold the viewer
     * @param viewerName The name that will be assign to this component (viewer)
     * @param trace The trace that is displayed by this viewer
     * @see TmfComponent
     */
    public TmfStatisticsViewer(Composite parent, String viewerName, ITmfTrace<ITmfEvent> trace) {
        init(parent, viewerName, trace);
    }

    /* (non-Javadoc)
     * @see
     * org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfStatisticsViewer#init(org.eclipse.swt.widgets.Composite,
     * java.lang.String, org.eclipse.linuxtools.tmf.core.trace.ITmfTrace) */
    @Override
    public void init(Composite parent, String viewerName, ITmfTrace<ITmfEvent> trace) {
        super.init(parent, viewerName);
        // Increment a counter to make sure the tree ID is unique.
        fCountInstance++;
        fInstanceNb = fCountInstance;
        fTrace = trace;

        final List<TmfBaseColumnData> columnDataList = getColumnDataProvider().getColumnData();

        fTreeViewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        fTreeViewer.setContentProvider(new TmfTreeContentProvider());
        fTreeViewer.getTree().setHeaderVisible(true);
        fTreeViewer.setUseHashlookup(true);

        for (final TmfBaseColumnData columnData : columnDataList) {
            final TreeViewerColumn treeColumn = new TreeViewerColumn(fTreeViewer, columnData.getAlignment());
            treeColumn.getColumn().setText(columnData.getHeader());
            treeColumn.getColumn().setWidth(columnData.getWidth());
            treeColumn.getColumn().setToolTipText(columnData.getTooltip());

            if (columnData.getComparator() != null) {
                treeColumn.getColumn().addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (fTreeViewer.getTree().getSortDirection() == SWT.UP
                                || fTreeViewer.getTree().getSortColumn() != treeColumn.getColumn()) {
                            fTreeViewer.setComparator(columnData.getComparator());
                            fTreeViewer.getTree().setSortDirection(SWT.DOWN);
                        } else {
                            fTreeViewer.setComparator(new ViewerComparator() {
                                @Override
                                public int compare(Viewer viewer, Object e1, Object e2) {
                                    return -1 * columnData.getComparator().compare(viewer, e1, e2);
                                }
                            });
                            fTreeViewer.getTree().setSortDirection(SWT.UP);
                        }
                        fTreeViewer.getTree().setSortColumn(treeColumn.getColumn());
                    }
                });
            }
            treeColumn.setLabelProvider(columnData.getLabelProvider());
        }

        // Handler that will draw the bar charts.
        fTreeViewer.getTree().addListener(SWT.EraseItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (columnDataList.get(event.index).getPercentageProvider() != null) {
                    TmfStatisticsTreeNode node = (TmfStatisticsTreeNode) event.item.getData();

                    double percentage = columnDataList.get(event.index).getPercentageProvider().getPercentage(node);
                    if (percentage == 0) {
                        return;
                    }

                    if ((event.detail & SWT.SELECTED) > 0) {
                        Color oldForeground = event.gc.getForeground();
                        event.gc.setForeground(event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
                        event.gc.fillRectangle(event.x, event.y, event.width, event.height);
                        event.gc.setForeground(oldForeground);
                        event.detail &= ~SWT.SELECTED;
                    }

                    int barWidth = (int) ((fTreeViewer.getTree().getColumn(1).getWidth() - 8) * percentage);
                    int oldAlpha = event.gc.getAlpha();
                    Color oldForeground = event.gc.getForeground();
                    Color oldBackground = event.gc.getBackground();
                    event.gc.setAlpha(64);
                    event.gc.setForeground(event.item.getDisplay().getSystemColor(SWT.COLOR_BLUE));
                    event.gc.setBackground(event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
                    event.gc.fillGradientRectangle(event.x, event.y, barWidth, event.height, true);
                    event.gc.drawRectangle(event.x, event.y, barWidth, event.height);
                    event.gc.setForeground(oldForeground);
                    event.gc.setBackground(oldBackground);
                    event.gc.setAlpha(oldAlpha);
                    event.detail &= ~SWT.BACKGROUND;
                }
            }
        });

        fTreeViewer.setComparator(columnDataList.get(0).getComparator());
        fTreeViewer.getTree().setSortColumn(fTreeViewer.getTree().getColumn(0));
        fTreeViewer.getTree().setSortDirection(SWT.DOWN);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfStatisticsViewer#refresh() */
    @Override
    public void refresh() {
        fTreeViewer.refresh();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.component.TmfComponent#dispose() */
    @Override
    public void dispose() {
        super.dispose();
        if (fWaitCursor != null) {
            fWaitCursor.dispose();
        }
    }

    /**
     * Focus on the statistics tree of the viewer
     */
    public void setFocus() {
        fTreeViewer.getTree().setFocus();
    }

    /* (non-Javadoc)
     * @see
     * org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfStatisticsViewer#setInput(org.eclipse.linuxtools.tmf.ui.
     * viewers.statistics.model.TmfStatisticsTreeNode) */
    @Override
    public void setInput(TmfStatisticsTreeNode input) {
        fTreeViewer.setInput(input);
    }

    /**
     * This method can be overridden to change the representation of the data in the columns.
     *
     * @return an object implementing ITmfBaseColumnDataProvider.
     */
    protected ITmfColumnDataProvider getColumnDataProvider() {
        return new TmfBaseColumnDataProvider();
    }

    /* Returns the primary control associated with this viewer.
     * @return the SWT control which displays this viewer's content */
    @Override
    public Control getControl() {
        return fTreeViewer.getControl();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfStatisticsViewer#getInput() */
    @Override
    public Object getInput() {
        return fTreeViewer.getInput();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfStatisticsViewer#getInputChangedRefresh()
     */
    @Override
    public long getInputChangedRefresh() {
        return STATS_INPUT_CHANGED_REFRESH;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfStatisticsViewer#getStatisticData() */
    @Override
    public AbsTmfStatisticsTree getStatisticData() {
        if (fStatisticsData == null) {
            fStatisticsData = new TmfBaseStatisticsTree();
        }
        return fStatisticsData;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfStatisticsViewer#getTrace() */
    @Override
    public ITmfTrace<ITmfEvent> getTrace() {
        return fTrace;
    }

    /**
     * Constructs the ID based on the experiment name and <code>fInstanceNb</code>
     *
     * @param name The name of the trace to show in the view
     * @return a view ID
     */
    @Override
    public String getTreeID(String name) {
        return name + fInstanceNb;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfStatisticsViewer#waitCursor(boolean) */
    @Override
    public void waitCursor(final boolean waitInd) {
        if ((fTreeViewer == null) || (fTreeViewer.getTree().isDisposed())) {
            return;
        }

        Display display = fTreeViewer.getControl().getDisplay();
        if (fWaitCursor == null) {
            fWaitCursor = new Cursor(display, SWT.CURSOR_WAIT);
        }

        // Perform the updates on the UI thread
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                if ((fTreeViewer != null) && (!fTreeViewer.getTree().isDisposed())) {
                    Cursor cursor = null; /* indicates default */
                    if (waitInd) {
                        cursor = fWaitCursor;
                    }
                    fTreeViewer.getControl().setCursor(cursor);
                }
            }
        });
    }
}
