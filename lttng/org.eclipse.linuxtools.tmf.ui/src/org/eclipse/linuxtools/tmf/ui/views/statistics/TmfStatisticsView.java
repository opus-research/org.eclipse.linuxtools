/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Generalized version based on LTTng
 *   Bernd Hufmann - Updated to use trace reference in TmfEvent and streaming
 *   Mathieu Denis - New request added to update the statistics from the selected time range
 *   Mathieu Denis - Generalization of the view to instantiate a viewer specific to a trace type
 *
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentDisposedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.viewers.ITmfViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.TmfViewerFactory;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.TmfStatisticsViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeRootFactory;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.widgets.tabsview.TmfViewerFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * The generic Statistics View displays statistics for any kind of traces.
 *
 * It is implemented according to the MVC pattern. - The model is a
 * TmfStatisticsTreeNode built by the State Manager. - The view is built with a
 * TreeViewer. - The controller that keeps model and view synchronized is an
 * observer of the model.
 *
 * @version 2.0
 * @author Mathieu Denis
 */
public class TmfStatisticsView extends TmfView {

    /**
     * The ID correspond to the package in which this class is embedded.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.statistics"; //$NON-NLS-1$

    /**
     * The view name.
     */
    public static final String TMF_STATISTICS_VIEW = "StatisticsView"; //$NON-NLS-1$

    /**
     * The initial window span (in nanoseconds)
     *
     * @since 2.0
     */
    public static final long INITIAL_WINDOW_SPAN = (1L * 100 * 1000 * 1000); // .1sec

    /**
     * Timestamp scale (nanosecond)
     *
     * @since 2.0
     */
    public static final byte TIME_SCALE = -9;

    /**
     * Default PAGE_SIZE for background requests.
     */
    protected static final int PAGE_SIZE = 50000;

    /**
     * Refresh frequency.
     */
    protected final Long STATS_INPUT_CHANGED_REFRESH = 5000L;

    /**
     * Stores the request to the experiment
     */
    protected ITmfEventRequest fRequest = null;

    /**
     * Stores the ranged request to the experiment
     * @since 2.0
     */
    protected ITmfEventRequest fRequestRange = null;

    /**
     * The viewer that builds the columns to show the statistics.
     *
     * @since 2.0
     */
    protected TmfViewerFolder fStatsViewers;

    /**
     * Flag to force request the data from trace.
     */
    protected boolean fRequestData = false;

    /**
     * Stores a reference to the parent composite of this view.
     */
    private Composite fParent;

    /**
     * Stores a reference to the selected experiment.
     */
    private TmfExperiment fExperiment;

    /**
     * Constructor of a statistics view.
     *
     * @param viewName The name to give to the view.
     */
    public TmfStatisticsView(String viewName) {
        super(viewName);
    }

    /**
     * Default constructor.
     */
    public TmfStatisticsView() {
        this(TMF_STATISTICS_VIEW);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        fParent = parent;
        TmfExperiment currentExperiment = TmfExperiment.getCurrentExperiment();
        // Read current data if any available
        if (currentExperiment != null) {
            fRequestData = true;
            // Insert the statistics data into the tree
            TmfExperimentSelectedSignal signal = new TmfExperimentSelectedSignal(this, currentExperiment);
            experimentSelected(signal);
            return;
        }
        fStatsViewers = createStatisticsViewers();
        /*
         * Updates the experiment field only at the end because
         * experimentSelected signal verifies the old selected experiment to
         * avoid reloading the same trace.
         */
        fExperiment = currentExperiment;
    }

    /**
     * Handler called when an experiment is selected. Checks if the experiment
     * has changed and requests the selected experiment if it has not yet been
     * cached.
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal signal) {
        if (signal != null) {
            // Does not reload the same trace if already opened
            if (fExperiment == null
                    || signal.getExperiment().toString().compareTo(fExperiment.toString()) != 0) {
                /*
                 * Dispose the current viewer and adapt the new one to the trace
                 * type of the experiment selected
                 */
                if (fStatsViewers != null) {
                    fStatsViewers.dispose();
                }
                // Update the current experiment
                fExperiment = signal.getExperiment();
                fStatsViewers = createStatisticsViewers();
                fParent.layout();

                TmfStatisticsViewer statViewer;
                for (ITmfViewer viewer : fStatsViewers.getViewers()) {
                    statViewer = (TmfStatisticsViewer) viewer;
                    setInput(statViewer, fExperiment.getTraces());
                }

                if (fRequestData) {
                    TmfExperimentRangeUpdatedSignal updateSignal = new TmfExperimentRangeUpdatedSignal(null, fExperiment, fExperiment.getTimeRange());
                    TmfStatisticsViewer statsViewer;
                    for (ITmfViewer viewer : fStatsViewers.getViewers()) {
                        statsViewer = (TmfStatisticsViewer) viewer;
                        statsViewer.experimentRangeUpdated(updateSignal);
                    }
                    fRequestData = false;
                }
            }
        }
    }

    /**
     * Handles the signal about disposal of the current experiment.
     *
     * @param signal
     *            The disposed signal
     */
    @TmfSignalHandler
    public void experimentDisposed(TmfExperimentDisposedSignal signal) {
        // Nothing to do
    }

    /**
     * Initializes the viewer with the information received.
     *
     * @param statViewer
     *            The statistics viewer for which the input will be set
     * @param traces
     *            The list of the traces to add in the tree.
     * @since 2.0
     */
    public void setInput(TmfStatisticsViewer statViewer, ITmfTrace[] traces) {
        String treeID = statViewer.getTreeID();
        if (TmfStatisticsTreeRootFactory.containsTreeRoot(treeID)) {
            // The experiment root is already present
            TmfStatisticsTreeNode experimentTreeNode = TmfStatisticsTreeRootFactory.getStatTreeRoot(treeID);

            // check if there is partial data loaded in the experiment
            int numTraces = traces.length;
            int numNodeTraces = experimentTreeNode.getNbChildren();

            if (numTraces == numNodeTraces) {
                boolean same = true;
                /*
                 * Detect if the experiment contains the same traces as when
                 * previously selected
                 */
                for (int i = 0; i < numTraces; i++) {
                    String traceName = traces[i].getName();
                    if (!experimentTreeNode.containsChild(traceName)) {
                        same = false;
                        break;
                    }
                }

                if (same) {
                    // no need to reload data, all traces are already loaded
                    statViewer.setInput(experimentTreeNode);

                    return;
                }
                experimentTreeNode.reset();
            }
        } else {
            TmfStatisticsTreeRootFactory.addStatsTreeRoot(treeID, statViewer.getStatisticData());
        }

        TmfStatisticsTreeNode treeModelRoot = TmfStatisticsTreeRootFactory.getStatTreeRoot(treeID);

        // if the model has contents, clear to start over
        if (treeModelRoot.hasChildren()) {
            treeModelRoot.reset();
        }

        // set input to a clean data model
        statViewer.setInput(treeModelRoot);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.ui.views.TmfView#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        if (fStatsViewers != null) {
            fStatsViewers.dispose();
        }
        // clean the model
        TmfStatisticsTreeRootFactory.removeAll();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        if (fStatsViewers != null) {
            fStatsViewers.setFocus();
        }
    }

    /**
     * Creates the statistics viewers for all traces in the experiment. Each
     * viewers are placed in a different tab and the first one is selected
     * automatically.
     *
     * It uses the extension point that defines the statistics viewer to build
     * from the trace type. If there is no viewer defined, it will use the
     * default viewer for the statistics. If the experiment is empty, there will
     * still be a global statistics viewer created.
     *
     * @return a folder viewer containing all the appropriate viewers
     * @since 2.0
     */
    protected TmfViewerFolder createStatisticsViewers() {
        // Default style for the tabs that will be created
        int defaultStyle = SWT.NONE;

        // Create the list of statistics viewer.
        TmfViewerFolder tabsView = new TmfViewerFolder(fParent);
        // The folder composite that will contain the tabs
        Composite folder = tabsView.getParentFolder();

        // Instantiation of the global viewer
        TmfStatisticsViewer globalViewer = new TmfStatisticsViewer();
        if (fExperiment != null) {
            // Shows the name of the experiment in the global tab
            globalViewer.init( folder, Messages.TmfStatisticsView_GlobalTabName + " - " + fExperiment.getName(), fExperiment); //$NON-NLS-1$
            tabsView.addTab(globalViewer, Messages.TmfStatisticsView_GlobalTabName, defaultStyle);

            String traceName;
            IResource traceResource;
            // Create a statistics viewer for each traces
            for (ITmfTrace trace : fExperiment.getTraces()) {
                traceName = trace.getName();
                traceResource = trace.getResource();
                TmfStatisticsViewer viewer = TmfViewerFactory.getStatisticsViewer(traceResource);
                /*
                 * Adds a new viewer only if there is one defined for the
                 * selected trace type, since the global tab already contains
                 * all the basic event counts for the trace(s)
                 */
                if (viewer != null) {
                    viewer.init(folder, traceName, trace);
                    tabsView.addTab(viewer, viewer.getName(), defaultStyle);
                }
            }
        } else {
            // There is no experiment selected. Shows an empty global tab
            globalViewer.init(folder, Messages.TmfStatisticsView_GlobalTabName, fExperiment);
            tabsView.addTab(globalViewer, Messages.TmfStatisticsView_GlobalTabName, defaultStyle);
        }
        // Makes the global viewer visible
        tabsView.setSelection(0);
        return tabsView;
    }
}
